---
order: 60
---

# Spring Security

Overview of Spring Security.

Spring Security is the standard framework for securing Java enterprise applications. It decouples authentication (verifying identity) from authorization (verifying permissions) across HTTP requests and method invocations using a chain of servlet filters and interceptors.

## Prerequisites

- Servlet Filter architecture (`Filter`, `FilterChain`, `doFilter`).
- Authentication mechanisms: HTTP Basic, session cookies, and bearer tokens.
- Cryptographic foundations: hashing (bcrypt, Argon2), symmetric/asymmetric keys, and JSON Web Tokens (JWT).

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `security_filter_chain` | Servlet filter delegation (`DelegatingFilterProxy`, `FilterChainProxy`), modern `SecurityFilterChain` bean DSL, and request matching. |
| 2 | `authentication_providers` | Authentication architecture: `AuthenticationManager`, `ProviderManager`, `AuthenticationProvider`, `UserDetailsService`, and `PasswordEncoder`. |
| 3 | `authorization` | Request-level authorization rules (`requestMatchers`), method security (`@PreAuthorize`, `@PostAuthorize`), and SpEL expressions. |
| 4 | `jwt_validation` | Stateless token validation: JWT parsing, signature verification, JWK Set resolution, custom claims extraction, and token expiration. |
| 5 | `o_auth2_resource_server` | OAuth2 Resource Server configuration: decoding bearer tokens via `JwtDecoder` or opaque token introspection and mapping scopes to authorities. |

## Next action

Begin with `security_filter_chain` to understand how HTTP requests pass through security filters before configuring specific authentication providers or JWT validation routines.

## Quick recall

**Q. What connects the servlet container's filter chain to Spring Security?**
A. `DelegatingFilterProxy` acts as a servlet filter in the web container that delegates incoming requests to the `FilterChainProxy` bean managed by Spring's ApplicationContext.

**Q. What is stored in `SecurityContextHolder`?**
A. A `SecurityContext` containing the currently authenticated `Authentication` object (principal, credentials, and granted authorities), stored by default in a `ThreadLocal`.

**Q. How does `AuthenticationProvider` differ from `UserDetailsService`?**
A. `UserDetailsService` only loads user details by username from a store. `AuthenticationProvider` performs the actual authentication check (e.g., password matching, token verification).

**Q. Why must stateless REST APIs disable CSRF protection in Spring Security?**
A. CSRF relies on browser-managed session cookies being automatically attached to cross-origin requests; token-based APIs storing credentials in explicit Bearer headers are immune to browser cookie reflection.
