---
order: 20
search: false
---

# Solutions: OAuth2 Resource Server & Client

## Solution: custom-jwt-claims-authorities - Multi-Tenant Custom Claims and Role Hierarchy Mapping

### 1. Root Cause & Authority Resolution Mechanics
Spring Security's default `JwtAuthenticationConverter` delegates authority extraction to `JwtGrantedAuthoritiesConverter`. By default, it searches only for a `scope` or `scp` claim and maps each space-separated string to `SCOPE_<value>`. When an Authorization Server issues tokens with nested role trees (e.g., Keycloak's `realm_access.roles`) and custom claims (such as `entitlements` and `tenant_id`), the default converter yields an empty authority collection, causing `@PreAuthorize("hasRole(...)")` checks to fail with 403 Forbidden.

### 2. Custom Authentication Converter Implementation
We implement `Converter<Jwt, AbstractAuthenticationToken>` to extract nested roles, convert entitlements to scopes, and map multi-tenant markers into standard Spring `GrantedAuthority` objects:

```java
package org.example.backend_fundamentals.spring.spring_security.o_auth2_resource_server.solution;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        String principalClaimValue = jwt.getClaimAsString(JwtClaimNames.SUB);
        return new JwtAuthenticationToken(jwt, authorities, principalClaimValue);
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 1. Extract nested realm_access.roles -> ROLE_<role>
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof List<?> rawRoles) {
            for (Object role : rawRoles) {
                if (role instanceof String roleName) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
                }
            }
        }

        // 2. Extract entitlements -> SCOPE_<permission>
        List<String> entitlements = jwt.getClaimAsStringList("entitlements");
        if (entitlements != null) {
            for (String entitlement : entitlements) {
                authorities.add(new SimpleGrantedAuthority("SCOPE_" + entitlement));
            }
        }

        // 3. Extract tenant_id -> TENANT_<tenant_id>
        String tenantId = jwt.getClaimAsString("tenant_id");
        if (tenantId != null && !tenantId.isBlank()) {
            authorities.add(new SimpleGrantedAuthority("TENANT_" + tenantId));
        }

        return Collections.unmodifiableList(authorities);
    }
}
```

### 3. SecurityFilterChain Registration
In Spring Security 6, configure the custom converter using the `oauth2ResourceServer` DSL:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomJwtAuthenticationConverter customConverter) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/reports/**").hasAuthority("SCOPE_report:read")
                .requestMatchers("/compliance/**").hasRole("COMPLIANCE_OFFICER")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(customConverter)));

        return http.build();
    }
}
```

### 4. Senior Interview Gotcha
- `hasRole('ADMIN')` automatically looks for `ROLE_ADMIN` in the `GrantedAuthority` collection. If the converter prefixes the role with `ROLE_ROLE_ADMIN` or forgets the prefix, `hasRole` fails.
- `hasAuthority('SCOPE_report:read')` performs an exact string comparison against the authority name.

---

## Solution: audience-and-custom-claim-validator - Strict Audience and Token Lifetime Validation

### 1. Token Replay Threat Analysis
In an architecture with multiple microservices sharing an Authorization Server, access tokens carry valid digital signatures verifiable against the common JWKS. Without audience (`aud`) verification, an access token granted to a client for service A (`document-upload`) can be stolen or forwarded to invoke service B (`compliance-service`). Enforcing that `aud` contains the target service's resource identifier bounds the blast radius.

### 2. Custom Token Validator Implementation
We implement `OAuth2TokenValidator<Jwt>` to validate audience, active tenant status, and max issuance age:

```java
package org.example.backend_fundamentals.spring.spring_security.o_auth2_resource_server.solution;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class ComplianceTokenValidator implements OAuth2TokenValidator<Jwt> {

    private final String expectedAudience;
    private final Duration maxTokenAge;

    public ComplianceTokenValidator(String expectedAudience, Duration maxTokenAge) {
        this.expectedAudience = expectedAudience;
        this.maxTokenAge = maxTokenAge;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        // 1. Audience validation
        List<String> audience = token.getAudience();
        if (audience == null || !audience.contains(expectedAudience)) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                OAuth2ErrorCodes.INVALID_TOKEN,
                "Token audience does not include expected resource: " + expectedAudience,
                "https://tools.ietf.org/html/rfc6750#section-3.1"
            ));
        }

        // 2. Tenant status check
        String tenantStatus = token.getClaimAsString("tenant_status");
        if (!"ACTIVE".equalsIgnoreCase(tenantStatus)) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                OAuth2ErrorCodes.INVALID_TOKEN,
                "Tenant is not ACTIVE for this token",
                null
            ));
        }

        // 3. Maximum token age from issued-at (iat)
        Instant issuedAt = token.getIssuedAt();
        if (issuedAt == null || Instant.now().isAfter(issuedAt.plus(maxTokenAge))) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                OAuth2ErrorCodes.INVALID_TOKEN,
                "Token exceeds maximum allowable age from iat (" + maxTokenAge.toMinutes() + " minutes)",
                null
            ));
        }

        return OAuth2TokenValidatorResult.success();
    }
}
```

### 3. NimbusJwtDecoder Configuration with Delegating Validator
We construct a composite validator chaining standard issuer checks, a 30-second clock skew timestamp validator, and our custom rules:

```java
@Configuration
public class JwtDecoderConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:https://auth.internal.corp}")
    private String issuerUri;

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> defaultIssuerValidator =
            JwtValidators.createDefaultWithIssuer(issuerUri);

        OAuth2TokenValidator<Jwt> timestampValidator =
            new JwtTimestampValidator(Duration.ofSeconds(30));

        OAuth2TokenValidator<Jwt> complianceValidator =
            new ComplianceTokenValidator("compliance-service", Duration.ofMinutes(60));

        // Combine using DelegatingOAuth2TokenValidator
        OAuth2TokenValidator<Jwt> delegatingValidator =
            new DelegatingOAuth2TokenValidator<>(defaultIssuerValidator, timestampValidator, complianceValidator);

        decoder.setJwtValidator(delegatingValidator);
        return decoder;
    }
}
```

### 4. Verification Flow
When an incoming request passes signature verification, `NimbusJwtDecoder` executes every validator registered in the `DelegatingOAuth2TokenValidator`. If any validator returns `OAuth2TokenValidatorResult.failure(...)`, `JwtAuthenticationProvider` throws `JwtValidationException`, prompting `BearerTokenAuthenticationFilter` to invoke `AuthenticationEntryPoint`, returning HTTP 401 with a `WWW-Authenticate: Bearer error="invalid_token"` header.

---

## Solution: oauth2-client-background-credentials - OAuth2 Client for Background Jobs and Outbound WebClient

### 1. Root Cause Analysis
`DefaultOAuth2AuthorizedClientManager` uses `OAuth2AuthorizedClientRepository` to store authorized clients. In a standard Servlet web environment, `OAuth2AuthorizedClientRepository` defaults to `HttpSessionOAuth2AuthorizedClientRepository` or an attribute repository tied to `RequestContextHolder.currentRequestAttributes()`.
When a task executes in a `@Scheduled` cron or `@Async` thread pool, there is no active HTTP request or session on the thread. Invoking `DefaultOAuth2AuthorizedClientManager` in this environment fails with `IllegalStateException: No HttpServletRequest available`.

### 2. Service-Based AuthorizedClientManager Configuration
To run OAuth2 client flows outside an HTTP request context, Spring Security provides `AuthorizedClientServiceOAuth2AuthorizedClientManager`. This implementation uses `OAuth2AuthorizedClientService` (which stores clients globally in memory or in a database) and takes an `OAuth2AuthorizeRequest` without requiring an `HttpServletRequest`:

```java
package org.example.backend_fundamentals.spring.spring_security.o_auth2_resource_server.solution;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SanctionsOAuth2ClientConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
            new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    @Bean
    public WebClient sanctionsWebClient(
            OAuth2AuthorizedClientManager authorizedClientManager,
            WebClient.Builder webClientBuilder) {

        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        // Pre-configure the default client registration ID for all outbound calls
        oauth2Filter.setDefaultClientRegistrationId("sanctions-service");

        return webClientBuilder
            .baseUrl("https://sanctions.api.internal")
            .filter(oauth2Filter)
            .build();
    }
}
```

### 3. application.yml Configuration
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          sanctions-service:
            authorization-grant-type: client_credentials
            client-id: ${SANCTIONS_CLIENT_ID}
            client-secret: ${SANCTIONS_CLIENT_SECRET}
            scope: sanctions:read
        provider:
          sanctions-service:
            token-uri: https://auth.internal.corp/oauth2/token
```

### 4. Scheduled Worker Implementation & Automatic Refresh
```java
@Service
public class SanctionsReconciliationService {

    private final WebClient sanctionsWebClient;

    public SanctionsReconciliationService(WebClient sanctionsWebClient) {
        this.sanctionsWebClient = sanctionsWebClient;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void reconcileSanctions() {
        String response = sanctionsWebClient.get()
            .uri("/v1/sanctions/active")
            .retrieve()
            .bodyToMono(String.class)
            .block();

        // Process sanctions payload
    }
}
```

**Token Lifecycle Mechanics:**
- On the first outbound request, `ServletOAuth2AuthorizedClientExchangeFilterFunction` invokes `authorizedClientManager.authorize(...)`.
- The manager queries `OAuth2AuthorizedClientService`. If no token exists, `ClientCredentialsOAuth2AuthorizedClientProvider` makes an outbound HTTP POST to the configured `token-uri` using basic auth (`client-id` + `client-secret`) and grant type `client_credentials`.
- The acquired access token and its expiration time are cached in `OAuth2AuthorizedClientService`.
- On subsequent requests, the manager checks `token.getExpiresAt()`. If the token is within its expiration window (default 1 minute buffer), the provider transparently requests a new token.
- No application code needs to manage token state, expiry timestamps, or Authorization header formatting.
