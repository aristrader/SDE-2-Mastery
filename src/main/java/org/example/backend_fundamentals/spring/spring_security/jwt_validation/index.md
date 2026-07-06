---
order: 40
---

# JWT Validation, JWK Set, Custom Claims

---

## JWT structure

A JWT is three base64url-encoded JSON objects joined by dots:

```
eyJhbGciOiJSUzI1NiIsImtpZCI6ImtleS0xIn0   ← header
.eyJzdWIiOiJ1c2VyMTIzIiwiaXNzIjoiaHR0cHM...  ← payload
.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV...       ← signature
```

**Header** (algorithm + key ID):

```json
{
  "alg": "RS256",
  "kid": "key-2024-01"
}
```

**Payload** (claims):

```json
{
  "iss": "https://auth.company.com",
  "sub": "user-uuid-123",
  "aud": ["identity-verification-service"],
  "exp": 1716120000,
  "iat": 1716116400,
  "jti": "unique-jwt-id-456",
  "tenant_id": "tenant-sg-01",
  "roles": ["VERIFIER", "AUDITOR"]
}
```

**Signature:** `RS256(base64url(header) + "." + base64url(payload), privateKey)`. Auth Server signs; Resource Server verifies. The private key never leaves the Auth Server.

The payload is **not encrypted** — anyone intercepting a JWT can read its claims. Never put sensitive data (PII, passwords, raw document data) in the payload. In KYC, putting a user's national ID number in a claim is a security violation.

---

## Standard claims (RFC 7519)

| Claim | Full name | Purpose |
|---|---|---|
| `iss` | Issuer | URL of the Auth Server that minted this token |
| `sub` | Subject | Identifies the user or service (typically a stable UUID) |
| `aud` | Audience | Which service(s) this token is intended for — must match the Resource Server |
| `exp` | Expiration time | Unix epoch seconds after which the token is invalid |
| `iat` | Issued at | When the token was minted — used for age checks |
| `jti` | JWT ID | Unique ID per token — enables revocation lookup and replay detection |
| `nbf` | Not before | Token not valid before this time — rare but useful for scheduled access |

`exp` and `iss` are always validated by `NimbusJwtDecoder`. `aud` is validated only if you configure a validator explicitly (see `OAuth2ResourceServer.md`). `jti` is not validated by default — implement a revocation store if you need it.

---

## JWK Set

The JWK Set (JWKS) is a JSON document published at `/.well-known/jwks.json` by the Auth Server, containing the public keys used to verify token signatures.

```json
{
  "keys": [
    {
      "kty": "RSA",
      "kid": "key-2024-01",
      "use": "sig",
      "alg": "RS256",
      "n": "<base64url-encoded modulus>",
      "e": "AQAB"
    },
    {
      "kty": "RSA",
      "kid": "key-2024-02",
      "use": "sig",
      "alg": "RS256",
      "n": "<base64url-encoded modulus>",
      "e": "AQAB"
    }
  ]
}
```

Multiple keys coexist in the JWKS during rotation — the old key validates in-flight tokens while the new key signs freshly minted ones.

**How Spring selects the right key:** Reads `kid` from the JWT header, looks up the matching key in the JWKS cache. On miss, re-fetches the JWKS from the Auth Server (handles rotation transparently). Still no match after re-fetch → 401.

**JWKS caching:** `NimbusJwtDecoder` keeps the JWKS in memory and re-fetches on unknown `kid`, not on a fixed timer. Fine for most deployments.

---

## Validation steps Spring performs

Order matters — Spring follows this sequence on every request:

1. **Parse** — base64url-decode all three parts; JSON-parse header and payload
2. **Key selection** — read `kid` from header; find matching JWK in cache (re-fetch if unknown)
3. **Signature verification** — verify the RS256 (or EC) signature using the JWK public key
4. **`exp` check** — reject if `now > exp + clockSkew`
5. **`iss` check** — reject if issuer does not match configured value
6. **`aud` check** — reject if token's audience list does not contain this service's identifier (only if you register an audience validator)
7. **Custom validators** — any additional `OAuth2TokenValidator<Jwt>` you register in `DelegatingOAuth2TokenValidator`

Steps 3–7 all produce 401 on failure. Step 3 failing means the token was tampered with or signed by an unknown party. Step 4 means the token is stale. Steps 5–6 mean it wasn't issued for this context.

---

## RS256 vs HS256

| | RS256 (asymmetric) | HS256 (symmetric) |
|---|---|---|
| Signing key | Auth Server's RSA private key | Shared secret known to all parties |
| Verification key | RSA public key (safe to distribute via JWKS) | Same shared secret |
| Secret sharing required | No | Yes — all verifiers must know the secret |
| Key rotation | Auth Server rotates privately; Resource Servers pick up new key via JWKS | Secret must be rotated and redistributed to every verifier |
| Use case | Any multi-service or public scenario | Internal tokens between two tightly coupled services that can safely share a secret |

**Default in production:** always RS256 (or EC equivalent ES256). HS256 creates a shared-secret distribution problem: every verifier must possess the secret, so compromise of any one service leaks the signing key for all tokens.

---

## Clock skew

JWT `exp` is a Unix epoch integer compared against the resource server's clock. In distributed systems, clocks are never perfectly synchronized.

`NimbusJwtDecoder` applies a default **60-second clock skew tolerance**: a token is accepted up to 60 seconds past `exp`, accommodating minor drift between issuer and resource server.

To adjust:

```java
NimbusJwtDecoder decoder = NimbusJwtDecoder
    .withJwkSetUri("https://auth-server/.well-known/jwks.json")
    .build();
decoder.setClockSkew(Duration.ofSeconds(30));
```

Don't set this large (e.g., 5 minutes) — it effectively extends the token's valid window beyond its stated `exp`.

---

## Custom JwtDecoder bean

Register a `JwtDecoder` bean to add validators beyond the defaults. Spring auto-detects it; no explicit `.decoder(...)` call needed if the bean is in the application context.

```java
@Bean
JwtDecoder jwtDecoder() {
    NimbusJwtDecoder decoder = NimbusJwtDecoder
        .withJwkSetUri("https://auth-server/.well-known/jwks.json")
        .build();

    // Default validators: exp + iss
    OAuth2TokenValidator<Jwt> defaults =
        JwtValidators.createDefaultWithIssuer("https://auth-server");

    // Custom: audience check
    OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
        if (jwt.getAudience().contains("my-service")) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(
            new OAuth2Error("invalid_token", "Wrong audience", null));
    };

    // Custom: reject tokens with no tenant_id claim
    OAuth2TokenValidator<Jwt> tenantValidator = jwt -> {
        if (jwt.getClaim("tenant_id") != null) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(
            new OAuth2Error("invalid_token", "Missing tenant_id", null));
    };

    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(defaults, audienceValidator, tenantValidator));

    return decoder;
}
```

`DelegatingOAuth2TokenValidator` runs all validators and collects failures — no short-circuit on first failure. Any failing validator rejects the request with 401.

When you declare a `JwtDecoder` bean, do NOT also set `jwk-set-uri` in `application.yml` for the same filter chain — Spring will either ignore the property or create a conflicting second decoder. Pick one.

---

## Custom claim extraction

### In JwtAuthenticationConverter (globally, for all requests)

```java
@Bean
JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) return Collections.emptyList();
        return roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList());
    });
    return converter;
}
```

### In a controller (per-request, for business logic)

Use `@AuthenticationPrincipal Jwt jwt` to get the JWT object directly:

```java
@GetMapping("/verification/{id}")
public VerificationResult getVerification(
        @PathVariable String id,
        @AuthenticationPrincipal Jwt jwt) {

    String userId   = jwt.getSubject();              // "sub" claim
    String tenantId = jwt.getClaim("tenant_id");     // custom claim
    List<String> roles = jwt.getClaimAsStringList("roles");

    // enforce tenant isolation — user can only see their own tenant's data
    verificationService.assertTenantAccess(tenantId, id);
    return verificationService.findById(id);
}
```

Alternatively via `SecurityContextHolder` (useful in service layers where the `Jwt` is not easily injected):

```java
JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder
    .getContext().getAuthentication();
Jwt jwt = (Jwt) auth.getCredentials();
String tenantId = jwt.getClaim("tenant_id");
```

`Jwt.getClaim(String)` returns `Object` — actual type depends on the JSON value (String, List, Boolean, Long). Use typed accessors (`getClaimAsString`, `getClaimAsStringList`, `getClaimAsBoolean`) to avoid casting errors.

---

## Token expiry handling

When a JWT is expired, `NimbusJwtDecoder` throws `JwtException`. `ExceptionTranslationFilter` catches it and returns a **401 Unauthorized** with `WWW-Authenticate: Bearer error="invalid_token"`.

**Never pass an expired token between services.** In a service-to-service call chain:
- The calling service must check expiry before forwarding
- Better: use `OAuth2AuthorizedClientManager` (from `OAuth2ResourceServer.md`) which handles refresh automatically

**User-facing token expiry:** Return 401 with a `reason` field so clients know to re-authenticate. Don't return 403 — that implies authorization failure, not authentication.

**`jti`-based revocation (advanced):** Store revoked `jti` values in Redis with TTL matching token `exp`. After signature/expiry validation, check if `jti` is in the revocation set. Immediate revocation without introspection's per-request Auth Server call, at the cost of a Redis lookup per request.

---

## Common mistakes

| Mistake | Consequence | Fix |
|---|---|---|
| Not validating `aud` | A token for Service A is accepted by Service B | Add audience validator to `DelegatingOAuth2TokenValidator` |
| Long token TTL (hours/days) | Leaked token remains valid for the full duration | Use 15-minute access tokens; use refresh tokens for session continuity |
| Logging the raw JWT | Token theft from log aggregation systems | Redact the `Authorization` header before logging |
| Trusting `alg` claim from the token | "alg:none" attack — token claims to be unsigned | `NimbusJwtDecoder` ignores the token's `alg` claim and uses the JWKS-specified algorithm; do not build a custom decoder that trusts the token's `alg` |
| Sensitive data in JWT payload | PII leakage (payload is base64url, not encrypted) | Store only identifiers (UUIDs) in claims; look up sensitive data server-side |

---

## Quick recall

**Q. What are the three parts of a JWT and what does each contain?**
A. Header (algorithm + `kid`), payload (standard and custom claims), signature (cryptographic proof). All three are base64url-encoded, not encrypted.

**Q. How does Spring pick the right public key to verify a JWT?**
A. Reads `kid` from the JWT header, finds the matching JWK in the cached JWKS. If `kid` is unknown, re-fetches the JWKS from the Auth Server.

**Q. How do you add custom validation logic (e.g., audience or tenant check) to JWT processing in Spring?**
A. Declare a `JwtDecoder` bean that builds a `NimbusJwtDecoder` and calls `decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaults, customValidator))`. Spring auto-detects the bean; any failing validator returns a 401.

**Q. Why prefer RS256 over HS256 in a microservices architecture?**
A. RS256 needs no secret sharing — only the Auth Server holds the private key. HS256 requires every verifier to know the shared secret, making key rotation and secret containment much harder.

**Q. How do you access a custom claim like `tenant_id` inside a Spring MVC controller?**
A. Inject `@AuthenticationPrincipal Jwt jwt` and call `jwt.getClaim("tenant_id")` or `jwt.getClaimAsString("tenant_id")`.

**Q. What is the `alg:none` attack and how does Spring prevent it?**
A. A tampered token sets `"alg": "none"` to claim it needs no signature. `NimbusJwtDecoder` ignores the token's own `alg` claim and only uses algorithms declared in the JWKS, making this attack ineffective.

**Q. What happens if a JWT is expired when it reaches your Resource Server?**
A. `NimbusJwtDecoder` throws `JwtException`; `ExceptionTranslationFilter` converts it to a 401 response. The calling client must re-authenticate; never forward an expired token downstream.

