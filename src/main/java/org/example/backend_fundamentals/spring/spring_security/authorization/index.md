---
order: 20
---

# Authorization

---

## Two authorization layers

Spring Security applies authorization at two independent points:

| Layer | Where | Annotation / Config | When evaluated |
|---|---|---|---|
| HTTP security | Filter chain, before servlet | `requestMatchers(...).hasRole(...)` in `SecurityFilterChain` | Before request hits controller |
| Method security | AOP proxy, inside service | `@PreAuthorize`, `@PostAuthorize` | On method call |

Use both. HTTP security is a coarse gate — unauthenticated requests rejected early, no Spring context overhead. Method security is fine-grained and survives refactors: a URL change cannot accidentally open a resource because the annotation lives on the method.

---

## Enabling method security

```java
@Configuration
@EnableMethodSecurity   // Spring Security 6+ — replaces @EnableGlobalMethodSecurity
public class SecurityConfig { }
```

`@EnableMethodSecurity` activates `@PreAuthorize`, `@PostAuthorize`, `@PreFilter`, `@PostFilter`, and `@Secured` via AOP. Without it, the annotations are silently ignored — a common misconfiguration bug.

**Migration note:** `@EnableGlobalMethodSecurity(prePostEnabled=true)` was the Spring Security 5 equivalent. Drop it in Spring Boot 3.x projects.

---

## @PreAuthorize

Evaluated **before** the method executes. If the SpEL expression is false, `AccessDeniedException` is thrown immediately — the method body never runs.

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) { ... }

@PreAuthorize("hasAuthority('order:write')")
public Order createOrder(OrderRequest req) { ... }

// check method param against current user
@PreAuthorize("#username == authentication.name")
public UserProfile getProfile(String username) { ... }

// delegate to a Spring bean — complex rules without polluting annotations
@PreAuthorize("@authorizationService.canAccessOrder(authentication, #orderId)")
public Order getOrder(Long orderId) { ... }
```

The `#paramName` syntax binds to method parameter names. Requires `-parameters` compiler flag or `@P` annotation if parameter names are stripped.

---

## @PostAuthorize

Evaluated **after** the method executes. The method runs, result is computed, then the expression is checked. If false, `AccessDeniedException` is thrown and the result is discarded.

```java
@PostAuthorize("returnObject.ownerId == authentication.principal.id")
public Document getDocument(Long docId) { ... }
```

Use case: fetch-then-check ownership. The method loads the object from DB; `@PostAuthorize` verifies the loaded object belongs to the current user — avoids a separate ownership query before the fetch.

**Caution:** the method runs and may have side effects (DB writes, external calls) even when `@PostAuthorize` rejects. Use `@PreAuthorize` whenever you can determine authorization without the return value.

---

## @PreFilter / @PostFilter

Filter elements of a collection argument or return value.

```java
// filter input list — only elements satisfying expression are passed to method
@PreFilter("filterObject.ownerId == authentication.principal.id")
public void processOrders(List<Order> orders) { ... }

// filter return list — only elements satisfying expression are returned
@PostFilter("filterObject.ownerId == authentication.principal.id")
public List<Order> getAllOrders() { ... }
```

Rarely used in practice. Problems:
- Method receives/returns only filtered elements — no distinction between "not found" and "access denied."
- For `@PostFilter`, the full collection is loaded from DB first, then filtered in memory — defeats the point at scale.

**Prefer DB-level filtering:** pass the current user's ID into the query (`WHERE owner_id = :userId`). Cheaper, correct at pagination boundaries, and doesn't load unauthorized data into the heap.

---

## SpEL expressions reference

| Expression | Meaning |
|---|---|
| `hasRole('ADMIN')` | Authority `ROLE_ADMIN` in SecurityContext |
| `hasAuthority('user:write')` | Exact authority string `user:write` |
| `hasAnyRole('ADMIN', 'MOD')` | OR across roles (each gets `ROLE_` prepended) |
| `hasAnyAuthority('user:read', 'admin:read')` | OR across exact authority strings |
| `isAuthenticated()` | Not anonymous |
| `isAnonymous()` | Anonymous (no credentials) |
| `permitAll()` | Always true |
| `denyAll()` | Always false |
| `authentication.name` | `Authentication.getName()` (usually username) |
| `authentication.principal` | The `UserDetails` object |
| `#paramName` | Method parameter value |
| `returnObject` | Return value (only in `@PostAuthorize`) |
| `filterObject` | Current element (only in `@Pre/PostFilter`) |
| `@beanName.method(...)` | Call a Spring bean — for complex rules |

---

## Role vs Authority — design guidance

Spring treats a **role** as just an authority with a `ROLE_` prefix. The distinction is purely semantic:

| | Role | Authority |
|---|---|---|
| Example | `ROLE_ADMIN`, `ROLE_USER` | `user:read`, `order:write`, `report:export` |
| `hasRole('ADMIN')` | Checks for `ROLE_ADMIN` (adds prefix automatically) | — |
| `hasAuthority('user:read')` | — | Exact match, no prefix added |
| Granularity | Coarse — group of permissions | Fine-grained — single operation |
| Typical use | JWT `roles` claim, group assignment | Permission check at method level |

**Design pattern for KYC / multi-tenant systems:** assign roles for broad grouping (`ROLE_KYC_AGENT`, `ROLE_COMPLIANCE`), but check fine-grained authorities at method boundaries (`kyc:approve`, `pii:view`). Roles are easy to assign; authorities are easy to audit.

---

## SecurityContextHolder — programmatic access

`SecurityContextHolder` stores the `Authentication` in a `ThreadLocal` (default mode). You can read it directly in service code:

```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();
Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

// cast to your UserDetails implementation for richer data
if (auth.getPrincipal() instanceof UserDetails userDetails) {
    Long userId = ((CustomUserDetails) userDetails).getId();
}
```

This is what SpEL expressions like `authentication.name` and `authentication.principal` resolve to behind the scenes. In tests, set it with `SecurityContextHolder.getContext().setAuthentication(auth)` before exercising the method.

---

## How authorization works internally

```
Annotated method call
    │
    ▼
MethodInterceptor (AuthorizationManagerBeforeMethodInterceptor for @PreAuthorize)
    │
    ├─ retrieves Authentication from SecurityContextHolder
    ├─ evaluates SpEL expression against MethodInvocation + Authentication
    │
    ├─ expression true  → proceeds; method runs
    └─ expression false → throws AccessDeniedException
                             │
                             ▼
                      ExceptionTranslationFilter
                             │
                      ├─ if not authenticated → redirect to login (401)
                      └─ if authenticated but unauthorized → 403 response
```

The AOP proxy only wraps beans managed by Spring. Calling an annotated method on `this` (self-invocation) bypasses the proxy — `@PreAuthorize` is silently ignored, same as `@Transactional`.

---

## HTTP security vs method security — interaction

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/admin/**").hasRole("ADMIN")  // HTTP layer gate
        .requestMatchers("/api/**").authenticated()
        .anyRequest().permitAll()
    );
    return http.build();
}

// Even if HTTP layer allows it, method security is a second check:
@Service
public class ReportService {
    @PreAuthorize("hasAuthority('report:export')")   // method layer
    public byte[] exportReport(Long reportId) { ... }
}
```

A request to `/api/reports/export` passes the HTTP layer (`authenticated()`), hits the controller, which calls `reportService.exportReport()`, and the AOP proxy checks `hasAuthority('report:export')`. Two independent gates.

---

## Common pitfalls

| Pitfall | Fix |
|---|---|
| `@EnableMethodSecurity` missing | Annotations silently ignored — always add to a `@Configuration` class |
| Self-invocation (`this.method()`) | `@PreAuthorize` bypassed — inject the bean into itself or extract to a separate bean |
| `hasRole('ROLE_ADMIN')` | Double `ROLE_` prefix bug — use `hasRole('ADMIN')` (Spring adds `ROLE_`) |
| `hasAuthority('ADMIN')` expecting role | `hasAuthority` does exact match — won't find `ROLE_ADMIN` |
| `@PostFilter` at scale | Full result set loaded into memory before filtering — filter in the query |
| Parameter names stripped | `#paramName` fails — add `-parameters` compiler flag or `@P("name")` on parameters |

---

## Quick recall

**Q. `@EnableMethodSecurity` — what happens if you forget it?**
A. `@PreAuthorize` and friends are silently ignored; no error thrown, all methods callable by any authenticated user.

**Q. `hasRole('ADMIN')` vs `hasAuthority('ADMIN')` — what's the difference?**
A. `hasRole` prepends `ROLE_` automatically, so it checks for `ROLE_ADMIN`. `hasAuthority` is an exact string match — it would NOT find `ROLE_ADMIN`.

**Q. When does `@PostAuthorize` cause a problem that `@PreAuthorize` wouldn't?**
A. The method runs before the check — side effects (writes, external calls) are not rolled back if `@PostAuthorize` rejects.

**Q. Why avoid `@PostFilter` on large collections?**
A. The full unfiltered collection is loaded into memory from the DB, then filtered in Java — wasteful and broken at pagination boundaries. Filter in the DB query instead.

**Q. How does `AccessDeniedException` become a 403?**
A. `ExceptionTranslationFilter` catches it; if the user is authenticated it sends 403; if not authenticated it redirects to the login endpoint (401/redirect).

**Q. Self-invocation and `@PreAuthorize` — what happens?**
A. The AOP proxy is bypassed — `@PreAuthorize` is not evaluated. Same root cause as `@Transactional` self-invocation.

**Q. Roles vs authorities — which do you use for fine-grained KYC permissions?**
A. Authorities (`kyc:approve`, `pii:view`). Roles are coarse groupings; authorities are the actual permission checks at method boundaries.
