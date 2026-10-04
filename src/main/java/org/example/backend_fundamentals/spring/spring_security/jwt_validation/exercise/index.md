---
order: 10
search: false
---

# Exercises: JWT Validation, JWK Set, Custom Claims

Practice configuring production-grade JWT validation in Spring Security, resolving clock skew and validator chaining collisions, mapping nested claim structures to GrantedAuthorities, and implementing JWKS key rotation and token blacklist defenses.

---

## Exercise: jwt-custom-decoder-multi-claim-validator - Multi-Claim Validator and Clock Skew Chaining in NimbusJwtDecoder

### Problem Statement
In a multi-tenant KYC identity verification platform, incoming requests carry Bearer JWTs minted by an enterprise OpenID Connect identity provider (`https://auth.company.com`).
The microservice enforces four critical validation constraints:
1. **Clock Skew Tuning**: Inter-region network drift between AWS regions requires a strict 30-second clock skew tolerance (`JwtTimestampValidator(Duration.ofSeconds(30))`) instead of the permissive default 60-second window.
2. **Strict Audience Verification**: The token's `aud` claim must contain `identity-verification-service`. Tokens minted for other microservices (such as `payment-service` or `crm-service`) must be rejected with `401 Unauthorized`.
3. **Mandatory Custom Tenant Claim**: Every token must contain a non-blank custom claim `"tenant_id"` (e.g., `"tenant_id": "tenant-apac-01"`). If `tenant_id` is missing or empty, the token must be rejected.
4. **Issuer Validation**: The `iss` claim must match `https://auth.company.com`.

A developer attempts to wire a custom `JwtDecoder` bean:

<!-- starter-code -->
```java
@Configuration
public class SecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withJwkSetUri("https://auth.company.com/.well-known/jwks.json")
            .build();

        OAuth2TokenValidator<Jwt> skewValidator = new JwtTimestampValidator(Duration.ofSeconds(30));
        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefaultWithIssuer("https://auth.company.com");

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(skewValidator, defaultValidators));
        return decoder;
    }
}
```

During disaster recovery and chaos testing:
1. When the infrastructure team simulates an extreme clock drift incident and tests relaxing the skew to 90 seconds (`Duration.ofSeconds(90)`), tokens sent at 75 seconds past `exp` are unexpectedly rejected with `401 Unauthorized`.
2. The developer is unsure how to implement custom validators for `aud` and `tenant_id` that integrate cleanly into Spring Security's `OAuth2TokenValidatorResult` contract with proper error descriptions.
3. The team needs to know how `DelegatingOAuth2TokenValidator` handles multiple failures: does it stop at the first failing validator, or does it evaluate all of them?

### Requirements
1. Identify why combining `new JwtTimestampValidator(...)` with `JwtValidators.createDefaultWithIssuer(...)` causes subtle validator collisions or masking when customizing duration.
2. Implement a custom `OAuth2TokenValidator<Jwt>` for audience matching that validates that `jwt.getAudience()` is non-null and contains `identity-verification-service`, returning `OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "The required audience is missing", "https://tools.ietf.org/html/rfc6750#section-3.1"))` on failure.
3. Implement a custom `OAuth2TokenValidator<Jwt>` for mandatory `tenant_id` presence that verifies `"tenant_id"` exists as a non-blank String, returning `OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Missing or empty tenant_id claim", null))` on failure.
4. Construct the refactored `@Bean public JwtDecoder jwtDecoder()` using `DelegatingOAuth2TokenValidator` combining the custom timestamp validator, `JwtIssuerValidator`, `AudienceValidator`, and `TenantValidator`.
5. Explain how `DelegatingOAuth2TokenValidator` aggregates errors from multiple failing validators into `OAuth2TokenValidatorResult`.

---

## Exercise: jwt-granted-authorities-custom-claims - Extract Nested Claims and Map Roles to Granted Authorities

### Problem Statement
An enterprise migrates to Keycloak / OAuth2 OIDC as its central identity provider. Keycloak structures user roles hierarchically in the JWT payload:

```json
{
  "sub": "usr_78912",
  "iss": "https://auth.company.com",
  "tenant_id": "tenant-apac-01",
  "scope": "openid email profile verification.read",
  "realm_access": {
    "roles": ["VERIFIER", "AUDITOR"]
  },
  "resource_access": {
    "identity-verification-service": {
      "roles": ["DOCUMENT_REVIEWER", "BIOMETRIC_APPROVER"]
    }
  }
}
```

In the Spring Boot Resource Server:
1. Controllers are secured with method security:
   ```java
   @PreAuthorize("hasRole('VERIFIER') and hasAuthority('SCOPE_verification.read')")
   @GetMapping("/verifications/{id}")
   public VerificationRecord getVerification(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
       // business logic
   }
   ```
2. In production, even when the token contains both the `VERIFIER` realm role and the `verification.read` scope, every request fails with `403 Forbidden`.
3. Upon inspecting `SecurityContextHolder.getContext().getAuthentication().getAuthorities()`, the security team discovers the authorities list contains only `SCOPE_openid`, `SCOPE_email`, `SCOPE_profile`, and `SCOPE_verification.read`. The roles `VERIFIER`, `AUDITOR`, `DOCUMENT_REVIEWER`, and `BIOMETRIC_APPROVER` are completely missing.

### Requirements
1. Explain the internal mechanics of Spring Security's default `JwtAuthenticationConverter` and `JwtGrantedAuthoritiesConverter`. Why are `realm_access` and `resource_access` roles ignored out of the box?
2. Implement a production-grade custom `KeycloakGrantedAuthoritiesConverter` implementing `Converter<Jwt, Collection<GrantedAuthority>>`:
   - Extracts standard OAuth2 scopes from the `"scope"` or `"scp"` claim and prefixes them with `"SCOPE_"`.
   - Safely parses `"realm_access.roles"`, prefixes each role with `"ROLE_"`, and converts them into `SimpleGrantedAuthority`.
   - Safely parses `"resource_access.<client-id>.roles"` for `"identity-verification-service"`, prefixes them with `"ROLE_"`, and adds them to the authorities collection.
   - Enforces strict null safety and type safety: if `realm_access` or `resource_access` is missing, null, or contains unexpected JSON types, it must degrade gracefully to an empty set without throwing `ClassCastException` or `NullPointerException`.
3. Configure `SecurityFilterChain` in Spring Boot 3 / Spring Security 6 to register a `JwtAuthenticationConverter` that uses your custom authorities converter.
4. Demonstrate how controller endpoints and downstream service beans cleanly access the verified `tenant_id` claim and user subject from the injected `Jwt` principal.

---

## Exercise: jwt-jwks-rotation-and-revocation-defense - JWKS Dynamic Key Rotation and High-Throughput JTI Revocation Defense

### Problem Statement
A high-throughput banking API processes 50,000 requests/sec. JWTs are signed asymmetrically with RS256 by an Auth Server publishing its public keys at `https://auth.company.com/.well-known/jwks.json`.
During a security vulnerability audit and chaos experiment:
1. **Key Rotation**: The Auth Server rotates its RSA key pair. New tokens arrive signed with `kid: "key-2024-04"`.
   The team needs to guarantee that in-flight requests signed with the previous key (`key-2024-03`) continue to be validated until they expire, while fresh requests signed with `key-2024-04` are immediately recognized without restarting the Resource Server.
2. **Denial-of-Service / Stampede via `kid`**: A penetration tester sends thousands of requests containing forged JWTs with random non-existent `kid` headers (`kid: "random-uuid-1"`, `kid: "random-uuid-2"`). The team notices that if naive caching logic is used, each unknown `kid` triggers a synchronous HTTP GET to `/.well-known/jwks.json`, overwhelming the Auth Server.
3. **Emergency Revocation (Stateless Paradox)**: An employee's laptop is stolen. Their active access token has 14 minutes remaining before `exp`. Because JWT validation is stateless, the token remains valid across all resource servers until `exp`. The banking security policy mandates immediate revocation capabilities for compromised tokens.
4. **Algorithm Confusion**: An attacker sends a forged JWT with header `{"alg": "HS256", "kid": "key-2024-03"}`, signed using the Auth Server's RSA *public key* as the HMAC secret key.

### Requirements
1. Detail the internal JWKS caching lifecycle in `NimbusJwtDecoder`:
   - Explain how `DefaultJWTProcessor` and `RemoteJWKSet` cache the JWKS in memory.
   - Explain what happens on a `kid` cache miss and how `RemoteJWKSet` avoids stampedes to the Auth Server.
   - Explain why `NimbusJwtDecoder` built via `.withJwkSetUri(...)` is inherently immune to `alg: none` and algorithm confusion attacks.
2. Implement an immediate revocation validator `BlacklistTokenValidator` implementing `OAuth2TokenValidator<Jwt>`:
   - Extracts the `jti` (JWT ID) claim from the token.
   - Queries a `RevocationStore` (backed by Redis or an in-memory cache).
   - If `jti` is present in the blacklist, returns `OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token has been revoked", null))`.
   - Rejects tokens that lack a `jti` claim when strict revocation tracking is enforced.
3. Formulate the operational strategy for blacklist storage in Redis:
   - What key schema should be used?
   - What TTL should be assigned to the Redis blacklist key, and why does this prevent indefinite memory growth?
