---
order: 40
---

# OAuth2 Resource Server & Client

---

## OAuth2 roles

OAuth2 defines four roles. Spring has separate starters for each server-side role.

| Role | Who it is | Spring Boot artifact |
|---|---|---|
| Resource Owner | The end user who owns the data | n/a (user's browser/app) |
| Authorization Server | Issues tokens after authenticating the user/service | Keycloak, Okta, Auth0, Spring Authorization Server |
| Resource Server | Your API — validates incoming tokens, serves protected data | `spring-boot-starter-oauth2-resource-server` |
| Client | Your app when it calls another protected API | `spring-boot-starter-oauth2-client` |

A single Spring Boot service is often both a Resource Server (receives tokens) and a Client (calls other services). In a KYC platform the verification service accepts tokens from the API gateway and itself calls the identity provider's API using a client-credentials token.

---

## Resource Server — validating incoming Bearer tokens

### Minimal configuration

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/internal/health").permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwkSetUri("https://auth-server/.well-known/jwks.json")));
    return http.build();
}
```

Spring fetches the JWKS on first startup, caches the public keys, and re-fetches automatically on an unknown `kid`. No secret sharing — the Auth Server keeps the private key; you only need the public keys.

### BearerTokenAuthenticationFilter

`BearerTokenAuthenticationFilter` sits at the front of the resource server filter chain. It:

1. Extracts the `Authorization: Bearer <token>` header (or a form/query parameter if configured).
2. Passes the raw token to the `AuthenticationManager`, which delegates to `JwtAuthenticationProvider` (JWT mode) or `OpaqueTokenAuthenticationProvider` (introspection mode).
3. On success, stores the resulting `JwtAuthenticationToken` in `SecurityContextHolder`.
4. On failure, delegates to `AuthenticationEntryPoint`, which returns 401 with a `WWW-Authenticate` header.

Don't subclass — configure via the `oauth2ResourceServer` DSL instead.

### What Spring does on every authenticated request

1. Extract the `Authorization: Bearer <token>` header
2. Base64url-decode the JWT into header / payload / signature parts
3. Read `kid` from the JWT header; look up the matching JWK in the cached JWKS
4. Verify the signature using the JWK public key (RS256 typically)
5. Validate `exp` — reject if token is expired
6. Validate `iss` — reject if issuer does not match configured issuer
7. Validate `aud` — reject if audience does not include this service's identifier (only if configured; see below)
8. Build a `JwtAuthenticationToken` and store it in `SecurityContextHolder`

Any failure at steps 4–7 produces a 401. The filter never calls the Auth Server again — everything validates locally using the cached public key. This is JWT's scalability advantage over opaque token introspection.

### JwtAuthenticationConverter — mapping claims to authorities

Spring's default converter reads the `scope` claim and produces authorities prefixed with `SCOPE_`. For a KYC service with role-based access, override this:

```java
@Bean
JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
    // read "roles" claim instead of "scope"
    converter.setAuthoritiesClaimName("roles");
    // add ROLE_ prefix so hasRole("ADMIN") works
    converter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
    return jwtConverter;
}
```

For richer logic (merging `roles` + `permissions` claims, tenant-specific authorities):

```java
jwtConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
    List<GrantedAuthority> authorities = new ArrayList<>();
    List<String> roles = jwt.getClaimAsStringList("roles");
    if (roles != null) {
        roles.stream()
             .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
             .forEach(authorities::add);
    }
    String tenantId = jwt.getClaim("tenant_id");
    if (tenantId != null) {
        authorities.add(new SimpleGrantedAuthority("TENANT_" + tenantId));
    }
    return authorities;
});
```

### Audience validation

Without audience validation, any valid JWT from your Auth Server can be replayed against any of your services. In a KYC platform with multiple microservices, a token for the document-upload service must not be accepted by the identity-verification service.

```java
@Bean
JwtDecoder jwtDecoder() {
    NimbusJwtDecoder decoder = NimbusJwtDecoder
        .withJwkSetUri("https://auth-server/.well-known/jwks.json")
        .build();

    OAuth2TokenValidator<Jwt> issuerValidator =
        JwtValidators.createDefaultWithIssuer("https://auth-server");

    OAuth2TokenValidator<Jwt> audienceValidator = token -> {
        List<String> audiences = token.getAudience();
        if (audiences != null && audiences.contains("identity-verification-service")) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(
            new OAuth2Error("invalid_token", "Token not issued for this service", null));
    };

    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator));

    return decoder;
}
```

Wire this bean into the security config via `.jwt(jwt -> jwt.decoder(jwtDecoder()))` instead of `.jwkSetUri(...)`.

---

## OAuth2 Client — calling another protected API

### Client credentials flow (machine-to-machine)

Used when your service calls a downstream API without a user session — typical for service-to-service calls.

`application.yml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          kyc-internal:
            authorization-grant-type: client_credentials
            client-id: ${CLIENT_ID}
            client-secret: ${CLIENT_SECRET}
            scope: kyc:read, kyc:write
        provider:
          kyc-internal:
            token-uri: https://auth-server/oauth2/token
```

Spring fetches a token automatically on first `authorize()` call, caches it, and refreshes before expiry. No manual token lifecycle management.

### OAuth2AuthorizedClientManager

The central abstraction. Handles: fetch token if none cached → refresh if expired → return current token.

```java
@Bean
OAuth2AuthorizedClientManager authorizedClientManager(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientRepository authorizedClientRepository) {

    OAuth2AuthorizedClientProvider provider =
        OAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials()
            .build();

    DefaultOAuth2AuthorizedClientManager manager =
        new DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository, authorizedClientRepository);
    manager.setAuthorizedClientProvider(provider);
    return manager;
}
```

### WebClient integration

The filter function `serverOAuth2AuthorizedClientExchangeFilterFunction` attaches the token to every outbound request:

```java
@Bean
WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
    ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
        new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
    oauth2.setDefaultClientRegistrationId("kyc-internal");

    return WebClient.builder()
        .filter(oauth2)
        .baseUrl("https://downstream-kyc-service")
        .build();
}
```

Every call through this `WebClient` has `Authorization: Bearer <token>` attached automatically. Refresh is transparent.

---

## JWT vs token introspection

| | JWT (local validation) | Token introspection (RFC 7662) |
|---|---|---|
| Network call per request | No — validates with cached public key | Yes — calls Auth Server `/introspect` endpoint |
| Latency | Zero (CPU only) | Adds a network round-trip |
| Revocation | Delayed — token valid until `exp` even if revoked | Immediate — Auth Server checks revocation on each call |
| Scalability | Excellent | Auth Server becomes a bottleneck at scale |
| Use when | Standard microservice setup; short-lived tokens (≤15 min) reduce revocation gap | High-security operations where immediate revocation is required (e.g., session invalidation on KYC failure) |

Spring supports introspection via `.oauth2ResourceServer(o -> o.opaqueToken(...))` with `introspection-uri` and client credentials configured. Both modes can coexist on different filter chains.

---

## Production gotchas

**JWKS cache invalidation:** Spring re-fetches JWKS on an unknown `kid`. During Auth Server key rotation there is a brief window where the new key is in use but not yet cached. Spring retries with a fresh JWKS fetch on signature verification failure, covering normal rotation automatically.

**Token leakage in logs:** Never log the raw `Authorization` header. Use a `OncePerRequestFilter` that redacts it, or configure logging to exclude that header.

**Client secret management:** Never check `client-secret` into `application.yml`. Use `${CLIENT_SECRET}` mapped from Kubernetes secrets or a secrets manager. Rotate on a schedule — compromised credentials give an attacker machine-level token minting ability.

**Clock skew between services:** `exp` validation uses the resource server's clock. If your pod's clock drifts > token TTL, every token looks expired. Ensure NTP sync; `NimbusJwtDecoder` accepts a `clockSkew` tolerance parameter for minor drift.

**Short token TTLs reduce blast radius:** A 15-minute access token means a leak is valid for at most 15 minutes. Don't use 24-hour tokens for client convenience — it shifts the security risk to your system.

---

## Quick recall

**Q. What is the difference between an OAuth2 Resource Server and an OAuth2 Client?**
A. Resource Server validates incoming Bearer tokens sent by callers. OAuth2 Client is your service acquiring and attaching tokens when it calls another API.

**Q. Why can a Resource Server validate JWTs without calling the Auth Server on every request?**
A. It fetches the JWKS (public keys) once and caches them; signature verification is pure cryptography, no network call needed.

**Q. What does JwtAuthenticationConverter do?**
A. Maps JWT claims (e.g., `roles`) to Spring `GrantedAuthority` objects so `hasRole()` / `hasAuthority()` checks work.

**Q. Why add audience validation and what happens without it?**
A. Without it, any valid JWT from your Auth Server can be replayed against any of your services. Audience validation ensures the token was issued specifically for this service.

**Q. What is the client credentials flow?**
A. Machine-to-machine auth: your service requests a token directly from the Auth Server using `client_id` + `client_secret`; no user involved. Spring manages fetch, cache, and refresh automatically.

**Q. JWT vs introspection — when would you choose introspection despite the overhead?**
A. When you need immediate revocation — e.g., invalidating a session after a KYC check fails or a user is flagged; JWT revocation only takes effect after `exp`.

**Q. What is `BearerTokenAuthenticationFilter` and what does it do?**
A. The Spring Security filter that extracts the Bearer token from the `Authorization` header and hands it to the `AuthenticationManager` for validation. On success it populates `SecurityContextHolder`; on failure it returns a 401 via `AuthenticationEntryPoint`.
