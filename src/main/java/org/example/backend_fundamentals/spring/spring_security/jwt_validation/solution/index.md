---
order: 20
search: false
---

# Solutions: JWT Validation, JWK Set, Custom Claims

Detailed architectural solutions and code implementations for custom Spring Security JWT decoders, multi-claim validators, Keycloak nested role extraction, and JWKS key rotation / revocation defenses.

---

## Solution: jwt-custom-decoder-multi-claim-validator - Multi-Claim Validator and Clock Skew Chaining in NimbusJwtDecoder

### 1. Root Cause Analysis: Validator Masking and Duplication
Spring Security's `JwtValidators.createDefaultWithIssuer(String issuer)` is a convenience factory that constructs a composite validator:

```java
public static OAuth2TokenValidator<Jwt> createDefaultWithIssuer(String issuer) {
    return new DelegatingOAuth2TokenValidator<>(
        new JwtTimestampValidator(), // hardcoded 60-second default clock skew
        new JwtIssuerValidator(issuer)
    );
}
```

When a developer chains a custom `JwtTimestampValidator` (e.g. 90 seconds) with `JwtValidators.createDefaultWithIssuer(issuer)` inside `DelegatingOAuth2TokenValidator`:
1. `DelegatingOAuth2TokenValidator` iterates over all delegates and requires every validator to return `OAuth2TokenValidatorResult.success()`.
2. For a token received 75 seconds after `exp`:
   - The custom validator (`skew = 90s`) succeeds (`75s <= 90s`).
   - The hidden `JwtTimestampValidator` inside `createDefaultWithIssuer` (`skew = 60s`) **fails** (`75s > 60s`).
   - Because one validator failed, the request is rejected with `401 Unauthorized`. The custom configuration was silently masked.

**The Fix**: When configuring custom timestamp tolerance, instantiate `new JwtIssuerValidator(issuer)` directly and combine it with your customized `new JwtTimestampValidator(duration)`.

### 2. Custom AudienceValidator Implementation
Create a reusable `OAuth2TokenValidator<Jwt>` to enforce strict audience matching:

```java
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final String requiredAudience;
    private final OAuth2Error error = new OAuth2Error(
        OAuth2ErrorCodes.INVALID_TOKEN,
        "The required audience is missing",
        "https://tools.ietf.org/html/rfc6750#section-3.1"
    );

    public AudienceValidator(String requiredAudience) {
        this.requiredAudience = Objects.requireNonNull(requiredAudience, "requiredAudience cannot be null");
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        List<String> audience = jwt.getAudience();
        if (audience != null && audience.contains(requiredAudience)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(error);
    }
}
```

### 3. Custom TenantIdValidator Implementation
Verify that the custom claim exists and contains non-empty text:

```java
public class TenantIdValidator implements OAuth2TokenValidator<Jwt> {

    private final OAuth2Error error = new OAuth2Error(
        OAuth2ErrorCodes.INVALID_TOKEN,
        "Missing or empty tenant_id claim",
        null
    );

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String tenantId = jwt.getClaimAsString("tenant_id");
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(error);
    }
}
```

### 4. Production JwtDecoder Bean Configuration
Assemble all validators into `DelegatingOAuth2TokenValidator` and attach to `NimbusJwtDecoder`:

```java
@Configuration
public class JwtSecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${app.security.jwt.issuer:https://auth.company.com}")
    private String expectedIssuer;

    @Value("${app.security.jwt.audience:identity-verification-service}")
    private String expectedAudience;

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // 1. Strict 30-second clock skew tolerance
        OAuth2TokenValidator<Jwt> timestampValidator = new JwtTimestampValidator(Duration.ofSeconds(30));

        // 2. Strict issuer validator (instantiated directly to prevent duplicate timestamp validators)
        OAuth2TokenValidator<Jwt> issuerValidator = new JwtIssuerValidator(expectedIssuer);

        // 3. Strict audience validator
        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(expectedAudience);

        // 4. Mandatory tenant claim validator
        OAuth2TokenValidator<Jwt> tenantValidator = new TenantIdValidator();

        // 5. Compose all validators into a single delegating validator
        OAuth2TokenValidator<Jwt> delegatingValidator = new DelegatingOAuth2TokenValidator<>(
            timestampValidator,
            issuerValidator,
            audienceValidator,
            tenantValidator
        );

        decoder.setJwtValidator(delegatingValidator);
        return decoder;
    }
}
```

### 5. Error Aggregation Semantics
`DelegatingOAuth2TokenValidator` does not short-circuit on the first error. It evaluates all registered delegates and combines any `OAuth2Error` instances into a single `OAuth2TokenValidatorResult.failure(Collection<OAuth2Error>)`.
When `NimbusJwtDecoder.decode()` receives a failure result, it wraps all accumulated errors into a `JwtValidationException`, enabling centralized logging and auditing of all policy violations (e.g. token is both expired and missing audience).

---

## Solution: jwt-granted-authorities-custom-claims - Extract Nested Claims and Map Roles to Granted Authorities

### 1. Root Cause Analysis: Default Scope Mapping
Spring Security's default `JwtAuthenticationConverter` delegates authorities mapping to `JwtGrantedAuthoritiesConverter`. By default:
- It only checks for claims named `"scope"` or `"scp"`.
- It splits space-delimited strings or iterates strings in a collection and prefixes them with `"SCOPE_"`.
- It completely ignores structured nested JSON claims such as Keycloak's `realm_access.roles` or `resource_access.<client-id>.roles`.
- Consequently, `@PreAuthorize("hasRole('VERIFIER')")` looks for an authority named `"ROLE_VERIFIER"`, finds none, and rejects the request with `403 Forbidden`.

### 2. Custom KeycloakGrantedAuthoritiesConverter Implementation
Implement a type-safe converter that extracts both standard OAuth2 scopes and nested roles:

```java
public class KeycloakGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String resourceClientId;
    private final JwtGrantedAuthoritiesConverter defaultScopesConverter = new JwtGrantedAuthoritiesConverter();

    public KeycloakGrantedAuthoritiesConverter(String resourceClientId) {
        this.resourceClientId = resourceClientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Preserve standard OAuth2 scopes (SCOPE_openid, SCOPE_verification.read, etc.)
        Collection<GrantedAuthority> scopes = defaultScopesConverter.convert(jwt);
        if (scopes != null) {
            authorities.addAll(scopes);
        }

        // 2. Extract Keycloak Realm Roles: realm_access.roles -> ROLE_<ROLE_NAME>
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof Collection<?> roles) {
                roles.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .forEach(authorities::add);
            }
        }

        // 3. Extract Keycloak Resource / Client Roles: resource_access.<client-id>.roles -> ROLE_<ROLE_NAME>
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            Object clientObj = resourceAccess.get(resourceClientId);
            if (clientObj instanceof Map<?, ?> clientMap) {
                Object rolesObj = clientMap.get("roles");
                if (rolesObj instanceof Collection<?> roles) {
                    roles.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .forEach(authorities::add);
                }
            }
        }

        return Collections.unmodifiableSet(authorities);
    }
}
```

### 3. SecurityFilterChain Configuration
Wire the converter into the resource server filter chain:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.security.client-id:identity-verification-service}")
    private String resourceClientId;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
            new KeycloakGrantedAuthoritiesConverter(resourceClientId)
        );
        return converter;
    }
}
```

### 4. Controller Access to Principal and Custom Claims
Use `@AuthenticationPrincipal Jwt` and Spring Security expressions:

```java
@RestController
@RequestMapping("/verifications")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('VERIFIER') and hasAuthority('SCOPE_verification.read')")
    public VerificationRecord getVerification(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {

        String userId   = jwt.getSubject();
        String tenantId = jwt.getClaimAsString("tenant_id");

        if (tenantId == null || tenantId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing tenant claim");
        }

        return verificationService.getVerificationForTenant(id, tenantId, userId);
    }
}
```

---

## Solution: jwt-jwks-rotation-and-revocation-defense - JWKS Dynamic Key Rotation and High-Throughput JTI Revocation Defense

### 1. JWKS Caching, Key Rotation, and Algorithm Defense Mechanics
`NimbusJwtDecoder` delegates cryptographic processing to Nimbus JOSE's `DefaultJWTProcessor` configured with a `RemoteJWKSet`:

1. **In-Memory Caching & Rotation**:
   - `RemoteJWKSet` caches the downloaded JWK Set in memory (`JWKSetCache`).
   - When a token arrives with `kid: "key-2024-04"`, Nimbus inspects the local cache.
   - If `kid` is missing, `RemoteJWKSet` initiates an HTTP GET request to `/.well-known/jwks.json`.
   - The Auth Server returns the updated JWKS containing both the old `key-2024-03` and the new `key-2024-04`.
   - The cache updates. Both in-flight tokens signed with `key-2024-03` and new tokens signed with `key-2024-04` validate without service disruption.
2. **Stampede / DoS Protection**:
   - If an attacker sends arbitrary forged `kid`s, `DefaultJWKSetCache` enforces a minimum refresh rate limit (by default, 5 minutes or configured timeout).
   - If a refresh request occurred within that window, Nimbus rejects subsequent requests with unknown `kid`s locally without calling the Auth Server, preventing Auth Server exhaustion.
3. **Immunity to `alg: none` and Algorithm Confusion**:
   - `NimbusJwtDecoder.withJwkSetUri(...)` configures a `JWSVerificationKeySelector` bound to the expected algorithm types declared in the JWKS (e.g., `RS256`, `ES256`).
   - Tokens specifying `"alg": "none"` are rejected as unsigned plain objects.
   - Tokens specifying `"alg": "HS256"` signed with the RSA public key are rejected because the key selector will not supply an RSA key for symmetric HMAC verification.

### 2. Immediate Revocation Validator (`BlacklistTokenValidator`)
Implement an `OAuth2TokenValidator<Jwt>` that checks the `jti` (JWT ID) against a distributed revocation store:

```java
public interface RevocationStore {
    boolean isRevoked(String jti);
    void revoke(String jti, Duration ttl);
}

public class BlacklistTokenValidator implements OAuth2TokenValidator<Jwt> {

    private final RevocationStore revocationStore;

    private static final OAuth2Error REVOKED_TOKEN_ERROR = new OAuth2Error(
        OAuth2ErrorCodes.INVALID_TOKEN,
        "Token has been revoked",
        null
    );

    private static final OAuth2Error MISSING_JTI_ERROR = new OAuth2Error(
        OAuth2ErrorCodes.INVALID_TOKEN,
        "Missing mandatory jti claim for revocation check",
        null
    );

    public BlacklistTokenValidator(RevocationStore revocationStore) {
        this.revocationStore = revocationStore;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String jti = jwt.getId(); // standard "jti" claim
        if (jti == null || jti.isBlank()) {
            return OAuth2TokenValidatorResult.failure(MISSING_JTI_ERROR);
        }

        if (revocationStore.isRevoked(jti)) {
            return OAuth2TokenValidatorResult.failure(REVOKED_TOKEN_ERROR);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
```

Attach to `NimbusJwtDecoder` alongside standard validators:

```java
OAuth2TokenValidator<Jwt> fullValidator = new DelegatingOAuth2TokenValidator<>(
    new JwtTimestampValidator(Duration.ofSeconds(30)),
    new JwtIssuerValidator(issuer),
    new BlacklistTokenValidator(revocationStore)
);
decoder.setJwtValidator(fullValidator);
```

### 3. Redis Blacklist Storage Strategy and Memory Sizing
When an administrator or security policy revokes a token:

```java
public void revokeToken(Jwt jwt) {
    String jti = jwt.getId();
    Instant exp = jwt.getExpiresAt();
    Duration remainingLifetime = Duration.between(Instant.now(), exp);

    if (!remainingLifetime.isNegative() && !remainingLifetime.isZero()) {
        // Key: "revoked:jti:" + jti, Value: "1", TTL: remaining lifetime
        redisTemplate.opsForValue().set("revoked:jti:" + jti, "1", remainingLifetime);
    }
}
```

**Why Redis Memory Stays Finite**:
- Once `now > exp`, `JwtTimestampValidator` rejects the token purely on expiration time.
- The resource server never needs to consult the blacklist for expired tokens.
- Setting the Redis key TTL exactly equal to the token's remaining valid duration ensures that every revoked entry is automatically purged the moment the token expires. Memory consumption is strictly bounded by active revocations during the token's lifetime window.
