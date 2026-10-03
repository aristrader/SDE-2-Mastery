---
order: 20
---

# Authorization in Spring Security

Authentication answers **who is calling?** Authorization answers **may that caller perform this action on this resource now?** A signed JWT establishes a trustworthy identity and claims; it does not, by itself, make a request allowed.

This page uses a multi-tenant document service as its running example. The same reasoning applies to payments, orders, and internal tools.

## Start with the request flow

1. An authentication filter validates credentials: for a bearer token, it verifies the JWT signature and expiry.
2. It creates an `Authentication` and stores it in the `SecurityContext` for this request.
3. Request-level rules decide whether the HTTP route is broadly allowed.
4. Method or domain rules decide whether this caller may perform this particular business operation.
5. Data access enforces the same tenant and ownership boundary when reading or writing records.

`SecurityContextHolder` is Spring Security's access point for the current `SecurityContext`; in a normal servlet request it is backed by the current execution context. The context contains an `Authentication`: principal (identity), granted authorities, authentication state, and, while relevant, credentials. It is not JWT-specific: form login, sessions, OAuth/OIDC, and JWT resource servers all populate this same abstraction.

## Choose the authorization model before the annotation

| Model | Good fit | Mechanism and trade-off |
| --- | --- | --- |
| **RBAC** (role-based access control) | Stable job responsibilities such as support agent, manager, administrator | Assign permissions to roles and roles to users. A hierarchy can let `ADMIN` inherit `MANAGER` permissions. It is easy to administer, but roles become brittle when every exception creates another role. |
| **ACL** (access control list) | Per-object sharing, such as “Asha can edit document 42” | Store grants against a resource. It is expressive, but listing/filtering millions of objects can require expensive joins and careful indexes. |
| **ABAC** (attribute-based access control) | Decisions that depend on subject, resource, action, and environment | Evaluate attributes such as caller tenant, document owner, requested action, time, or assurance level. It handles context well, but policies need tests and clear ownership. |

Use RBAC for broad capability, ACL for exceptional object sharing, and ABAC/domain logic for ownership, tenant, and state checks. Real systems commonly combine them.

## Roles, authorities, and two enforcement layers

A **role** is a broad responsibility; an **authority** is an exact capability. Spring's `hasRole("ADMIN")` convention checks for authority `ROLE_ADMIN`. `hasAuthority("document:delete")` checks that exact string. Map the convention deliberately when converting claims; a token containing `ADMIN` does not automatically satisfy `hasRole("ADMIN")`.

Request rules protect the HTTP boundary. Method rules protect business logic, including calls that do not come through the same controller. Enable method security explicitly; Spring Boot's security starter does not activate it automatically.

```java
@Configuration
@EnableMethodSecurity
class SecurityConfig {
    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/documents/**")
                            .hasAuthority("document:delete")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                .build();
    }
}

@Service
class DocumentService {
    @PreAuthorize("hasAuthority('document:delete')")
    void delete(Document document, Caller caller) {
        if (!document.tenantId().equals(caller.tenantId())
                || !document.ownerId().equals(caller.subject())) {
            throw new AccessDeniedException("not this caller's document");
        }
        // delete only after the tenant and ownership decision
    }
}
```

Use route rules for coarse, early rejection and method/domain rules for decisions needing method parameters, entity state, ownership, or a non-HTTP caller. `@PreAuthorize` reads the already-created `Authentication`; it does not parse a JWT. Avoid hiding a large policy language in one SpEL string: grant stable capabilities as authorities, and put complex, testable business decisions in a named authorization component.

## Stateless JWT authorization: what is verified, then what is decided

An identity provider handles login and issues tokens. A Spring resource server validates incoming tokens and enforces the application's policy; it does not need a database lookup for every valid JWT.

For asymmetric JWT signing, the resource server obtains public keys from a **JWKS** (JSON Web Key Set). The JWT header's `kid` identifies the candidate verification key, allowing key rotation. After signature, issuer, expiry, and any configured audience validation succeed, Spring maps claims/scopes into `GrantedAuthority` values and puts the resulting authentication in the context. Treat custom claims such as `tenant_id`, `organization_id`, or `client_id` as inputs to a decision—not as permission to skip resource checks.

**Normal delete path:** a caller sends `DELETE /documents/42` with a bearer token. The filter verifies it and installs the principal. The route requires `document:delete`. The service loads document 42 using the caller's tenant predicate, then checks ownership or an explicit share grant before deletion. A failure at any layer stops the action; log the decision without logging the token.

## Tenant isolation is an authorization invariant

A tenant is a customer organization, not an individual user. In shared SaaS, derive the tenant identity from the verified principal, then include it in every resource lookup:

```sql
SELECT * FROM document WHERE id = :documentId AND tenant_id = :callerTenant;
```

Never trust a caller-supplied `tenant_id` as the isolation boundary. If a request carries one for routing, compare it to the authenticated tenant before use. This prevents an insecure direct-object-reference style bug where a valid user guesses another tenant's ID. Database row-level security or tenant-scoped repositories can provide an additional guard, but do not replace service-level intent checks.

| Deployment choice | Authorization consequence |
| --- | --- |
| Multi-tenant SaaS | One service can serve many organizations; tenant filtering and cross-tenant tests are mandatory. |
| Single-tenant hosted deployment | Separate infrastructure/database reduces shared-data blast radius, but users still need authorization. |
| On-premises deployment | The customer runs the infrastructure; the application still needs roles, auditability, and secure integration boundaries. |
| B2B versus B2C | B2B often needs organization, delegation, and enterprise federation; B2C commonly emphasizes scale and self-service. Neither removes the ownership check. |

## Failure modes and recovery

| Problem | Naive failure | Mechanism and trade-off | Recovery |
| --- | --- | --- | --- |
| Route-only protection | A scheduled job or another adapter invokes a sensitive service directly. | Add method/domain checks; unannotated methods are not automatically protected. | Deny, audit, and add an automated authorization test for every entry point. |
| Role-prefix mismatch | A valid administrator gets a 403. | Normalize claims to `ROLE_*`, or use exact authorities consistently. | Inspect mapped authorities, correct the converter/configuration, then retest both allow and deny paths. |
| Tenant from request input | A user supplies another tenant ID. | Derive tenant from the verified principal and scope queries. | Reject mismatch, investigate attempted access, and backfill tenant predicates where missing. |
| Stale or insufficient authentication | A long-lived token is used for a high-risk change. | Require recent authentication or MFA assurance (`auth_time`/`amr` where supported). It adds user friction. | Return a clear re-authentication challenge; issue a higher-assurance token before retrying. |
| Context lost in async work | A background task has no current principal. | Pass an explicit actor/tenant command or use Spring's security-context delegation deliberately. | Do not silently run as a system administrator; reject or re-establish a bounded service identity. |

Pessimistically denying a request is safer than guessing. Keep transactions short and do authorization before irreversible writes; `@PostAuthorize` can be useful for reads but is a poor primary guard for a write that has already happened.

## Defense in depth and operations

No single check is enough:

1. The resource server validates token integrity and creates a standard caller context.
2. Request rules reject clearly invalid routes early.
3. Service/domain policy verifies capability, state, ownership, and tenant.
4. Tenant-scoped persistence prevents accidental broad reads or writes.
5. Audit events make sensitive allow/deny decisions investigable.

For an audit event, record timestamp, subject, tenant/client identifier, action, resource identifier, decision, reason/rule, and request correlation ID. MDC can enrich ordinary logs with safe correlation fields, but security audit events should still have a defined schema and retention/access policy. Never log passwords, OTPs, raw JWTs, refresh tokens, private keys, or unnecessary sensitive personal data.

## Common misconceptions

- **“`@Transactional` or a JWT makes the operation safe.”** Transactions give atomicity and JWTs establish claims; neither decides ownership or tenant access.
- **“The identity provider authorizes my application's business rules.”** It can issue roles/scopes. The resource server still owns its domain policy.
- **“Interceptors authenticate JWTs.”** In a Spring MVC application, authentication occurs earlier in the Spring Security filter chain. An interceptor can consume established context for logging or adaptation.
- **“SecurityContextHolder is only for JWT.”** It is authentication-mechanism independent.
- **“One URL rule contains all authorization.”** URL rules cannot reliably evaluate entity ownership, request state, or non-HTTP calls.

## Deferred scope

This guide does not teach implementing an OAuth authorization server, SAML assertion processing, policy-engine deployment, or reactive/WebFlux security. Add those when the application needs them; they are not prerequisites for explaining servlet-side authorization clearly.

## References

- [Spring Security method security](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html) — request versus method authorization, `@EnableMethodSecurity`, and `@PreAuthorize` behavior.
- [Spring Security JWT resource server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html) — JWT validation and claim-to-authority mapping.
- [OWASP Authorization Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html) — deny-by-default, per-request validation, and authorization testing guidance.
- [NIST SP 800-162](https://csrc.nist.gov/pubs/sp/800/162/final) — ABAC terminology and attribute categories.

## Quick recall

- Authenticate first; authorize every sensitive action and resource.
- Roles are broad; authorities are exact. `hasRole("X")` conventionally checks `ROLE_X`.
- Route checks are early and coarse; service checks protect business decisions.
- A verified tenant claim scopes queries; a request parameter never defines the boundary.
- Log decisions and correlation fields, never credentials or raw tokens.
