---
order: 10
search: false
---

# Exercises: OAuth2 Resource Server & Client

## Exercise: custom-jwt-claims-authorities - Multi-Tenant Custom Claims and Role Hierarchy Mapping

### Problem
In a multi-tenant compliance platform, an external OAuth2 Authorization Server (Keycloak / Auth0) issues JWT access tokens where roles and permissions are placed in custom, nested claims rather than a flat `scope` string. A decoded token payload contains:

```json
{
  "sub": "usr_94821",
  "iss": "https://auth.internal.corp",
  "aud": "compliance-service",
  "tenant_id": "tenant_apac_01",
  "realm_access": {
    "roles": ["COMPLIANCE_OFFICER", "AUDITOR"]
  },
  "entitlements": ["report:read", "case:write"]
}
```

By default, Spring Security's `JwtAuthenticationConverter` delegates to `JwtGrantedAuthoritiesConverter`, which only parses the `scope` or `scp` claim and maps each space-delimited string into a `GrantedAuthority` with a `SCOPE_` prefix. Under default configuration:
1. `realm_access.roles` is ignored entirely, so controller methods annotated with `@PreAuthorize("hasRole('COMPLIANCE_OFFICER')")` return `403 Forbidden`.
2. Direct API permissions in `entitlements` are ignored, preventing `hasAuthority('SCOPE_report:read')` checks.
3. Multi-tenant checks requiring the `tenant_id` claim fail.

### Requirements
1. Implement a custom converter class `CustomJwtAuthenticationConverter` implementing `Converter<Jwt, AbstractAuthenticationToken>` (or configure `JwtAuthenticationConverter`) that parses:
   - Each role from the nested `realm_access.roles` JSON map into an authority prefixed with `ROLE_` (e.g., `ROLE_COMPLIANCE_OFFICER`).
   - Each permission from `entitlements` into an authority prefixed with `SCOPE_` (e.g., `SCOPE_report:read`).
   - The `tenant_id` claim into an authority formatted as `TENANT_<tenant_id>` (e.g., `TENANT_tenant_apac_01`).
2. Construct and return a `JwtAuthenticationToken` containing the original `Jwt`, the merged set of `GrantedAuthority` objects, and the `sub` claim as the principal name.
3. Wire the converter into the `SecurityFilterChain` bean using Spring Security 6's functional DSL (`.oauth2ResourceServer(oauth2 -> ...)`).

---

## Exercise: audience-and-custom-claim-validator - Strict Audience and Token Lifetime Validation

### Problem
In a microservices mesh, multiple downstream services (`document-upload`, `compliance-service`, `billing-api`) trust tokens signed by the same central Authorization Server. Without explicit audience verification, a client with a token minted strictly for `document-upload` can replay that token against `compliance-service`, exposing sensitive compliance records.

Additionally, internal security policy requires that:
1. Every token must contain `"compliance-service"` within its `aud` (audience) claim list.
2. The token must include a custom claim `tenant_status` with the exact value `"ACTIVE"`.
3. Even if the standard expiration (`exp`) is still valid, tokens issued more than 60 minutes ago (based on the `iat` issued-at claim) must be rejected to enforce maximum session freshness.

### Requirements
1. Implement a custom `OAuth2TokenValidator<Jwt>` named `ComplianceTokenValidator` that validates:
   - The token contains `"compliance-service"` in its `aud` claim list.
   - The claim `tenant_status` equals `"ACTIVE"`.
   - The `iat` (issued-at) timestamp is within 60 minutes of `Instant.now()`.
   - If any condition fails, return an `OAuth2TokenValidatorResult.failure(...)` with an appropriate `OAuth2Error` code and description.
2. Combine this custom validator with Spring Security's default issuer validator (`JwtValidators.createDefaultWithIssuer("https://auth.internal.corp")`) using `DelegatingOAuth2TokenValidator`.
3. Provide a `@Bean` method for `JwtDecoder` using `NimbusJwtDecoder.withJwkSetUri(...)` that registers the composite validator and configures a 30-second clock skew tolerance.

---

## Exercise: oauth2-client-background-credentials - OAuth2 Client for Background Jobs and Outbound WebClient

### Problem
A compliance ingestion service runs a daily reconciliation job using Spring's `@Scheduled` annotation. The job runs in a background thread pool without an incoming HTTP request context. The job must call an external Sanctions API protected by OAuth2 using the `client_credentials` grant flow.

A developer wrote the following configuration:

```java
@Bean
OAuth2AuthorizedClientManager authorizedClientManager(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientRepository authorizedClientRepository) {

    DefaultOAuth2AuthorizedClientManager manager =
        new DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository, authorizedClientRepository);

    manager.setAuthorizedClientProvider(
        OAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials()
            .build());

    return manager;
}
```

When the scheduled task executes:
```java
@Scheduled(cron = "0 0 2 * * ?")
public void reconcileSanctions() {
    sanctionsWebClient.get()
        .uri("/sanctions/active")
        .retrieve()
        .bodyToMono(String.class)
        .block();
}
```
The execution immediately crashes with:
`java.lang.IllegalStateException: No HttpServletRequest available`

### Requirements
1. Diagnose the root cause of `IllegalStateException` when using `DefaultOAuth2AuthorizedClientManager` inside `@Scheduled` or `@Async` tasks.
2. Implement the correct `@Bean` definition for `OAuth2AuthorizedClientManager` using `AuthorizedClientServiceOAuth2AuthorizedClientManager` and `OAuth2AuthorizedClientService`.
3. Configure a thread-safe `WebClient` bean that integrates with this authorized client manager using `ServletOAuth2AuthorizedClientExchangeFilterFunction`, setting the default registration ID to `"sanctions-service"`.
4. Explain how Spring's authorized client manager handles token caching and automatic refresh before expiration without manual token lifecycle management code.
