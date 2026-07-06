---
order: 10
---

# SecurityFilterChain

---

## The shift in Spring Security 5.7 / 6

Before 5.7, you extended `WebSecurityConfigurerAdapter` and overrode `configure(HttpSecurity)`. That class is **deprecated and removed in Spring Security 6**.

The modern approach: declare a `SecurityFilterChain` bean. No base class, no override — just a factory method.

```java
// OLD — deprecated
@Configuration
public class OldSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception { ... }
}

// NEW — Spring Security 5.7+ / 6
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated())
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
            .csrf(csrf -> csrf.disable())
            .build();
    }
}
```

**Why the change:** the adapter pattern made composing multiple chains hard and forced inheritance for customization. The bean approach is composable and testable.

---

## How Spring Security integrates with the servlet container

```
HTTP request
    │
    ▼
Servlet container filter chain
    │
    ├─► DelegatingFilterProxy  ← registered in web.xml / auto-registered by Spring Boot
    │       │
    │       │ delegates to
    │       ▼
    │   FilterChainProxy  ← the Spring Security entry point; a Spring bean
    │       │
    │       │ picks first matching SecurityFilterChain by request matcher
    │       ▼
    │   SecurityFilterChain (your @Bean)
    │       │  ← ordered list of security filters
    │       ▼
    │   [security filters execute in order]
    │
    ▼
DispatcherServlet → Controller
```

**DelegatingFilterProxy** bridges the servlet world (container-managed filters) and the Spring world (context-managed beans). It looks up `FilterChainProxy` by name from the Spring context and delegates to it.

**FilterChainProxy** holds all `SecurityFilterChain` beans, ordered by `@Order`. For each request it picks the first chain whose `requestMatcher` matches and runs that chain's filters.

---

## Standard filter order

The security filter chain runs these filters in order (simplified — actual chain has ~15 filters):

| Filter | Responsibility |
|---|---|
| `SecurityContextHolderFilter` (6+) / `SecurityContextPersistenceFilter` (pre-6) | Load `SecurityContext` from store at request start; clear or save it at end |
| `LogoutFilter` | Detect logout URL; clear security context and session |
| `UsernamePasswordAuthenticationFilter` | Handle form login; authenticate credentials; set SecurityContext |
| `BasicAuthenticationFilter` | Handle HTTP Basic header; authenticate |
| `BearerTokenAuthenticationFilter` | Handle `Authorization: Bearer <jwt>`; set SecurityContext (OAuth2 resource server) |
| `ExceptionTranslationFilter` | Catch `AuthenticationException` → 401; catch `AccessDeniedException` → 403 |
| `AuthorizationFilter` | The actual access control check; runs last in the chain |

Order matters: `ExceptionTranslationFilter` must wrap `AuthorizationFilter` to translate its exceptions into HTTP responses.

**Spring Security 6 filter rename:** `SecurityContextPersistenceFilter` was deprecated in 5.7 and removed in 6. Its replacement, `SecurityContextHolderFilter`, no longer auto-saves the context at request end — saving is pushed to the authentication filters that set the context. This avoids unnecessary session writes on every request.

---

## Full SecurityFilterChain DSL

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        // 1. Authorization rules
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers("/public/**").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())

        // 2. Session management
        .sessionManagement(sm -> sm
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // 3. OAuth2 JWT resource server
        .oauth2ResourceServer(o -> o
            .jwt(Customizer.withDefaults()))

        // 4. CSRF
        .csrf(csrf -> csrf.disable())

        // 5. CORS — supply a CorsConfigurationSource bean
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // 6. Exception handling
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(customAuthEntryPoint())
            .accessDeniedHandler(customAccessDeniedHandler()))

        .build();
}
```

### CorsConfigurationSource bean

CORS must be configured via a `CorsConfigurationSource` bean when using `SecurityFilterChain` — Spring Security's `CorsFilter` reads from this bean, not from `@CrossOrigin` annotations on controllers (those run after the security filter chain, too late for preflight `OPTIONS` requests).

```java
@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://app.example.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Tenant-Id"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

**Why Security needs it:** the preflight `OPTIONS` request arrives before any authentication. Without CORS configured at the security layer, Spring Security may reject the preflight with a 401/403 before the browser sends the actual request.

---

## Session management

| Policy | Behaviour | Use for |
|---|---|---|
| `STATELESS` | Never create or use an HTTP session | REST APIs, JWT/OAuth2 |
| `IF_REQUIRED` | Create session only if needed (default) | Traditional web apps |
| `ALWAYS` | Always create a session | Legacy apps |
| `NEVER` | Never create a session but use one if it exists | Unusual — partial migration |

**STATELESS removes the need for CSRF protection** — CSRF attacks exploit sessions/cookies; with no session, nothing to hijack.

---

## CSRF

**When to disable:** stateless APIs that authenticate via JWT Bearer tokens. Browsers never automatically send a JWT header — CSRF attacks can't forge one.

**When to keep enabled:** any app using cookie-based sessions (traditional web, Thymeleaf). The default `CsrfFilter` generates a token and validates it on state-changing requests.

```java
// Stateless REST API — disable
.csrf(csrf -> csrf.disable())

// Cookie-based web app — keep default (just omit the line)
// Or configure the token repo explicitly:
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
```

---

## Multiple SecurityFilterChain beans

Use `@Order` to prioritize chains. The first chain whose `requestMatcher` matches the request wins — later chains are skipped.

```java
// Chain 1: actuator endpoints — no auth
@Bean
@Order(1)
SecurityFilterChain actuatorChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher("/actuator/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .build();
}

// Chain 2: API endpoints — JWT
@Bean
@Order(2)
SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
    return http
        .securityMatcher("/api/**")
        .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .build();
}

// Chain 3: catch-all
@Bean
@Order(3)
SecurityFilterChain defaultChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth.anyRequest().denyAll())
        .build();
}
```

**Gotcha:** omitting `securityMatcher()` makes the chain match all requests. The lowest `@Order` number wins (highest priority). Spring Security's own default chain has `@Order(2147483647)` — your beans always take precedence.

---

## Custom entry points and access denied handlers

```java
// 401 — not authenticated
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"error\":\"Unauthorized\"}");
    }
}

// 403 — authenticated but not authorized
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("{\"error\":\"Forbidden\"}");
    }
}
```

Without a custom `AuthenticationEntryPoint`, Spring Security redirects unauthenticated REST clients to a login page — wrong for JSON APIs.

---

## Quick recall

**Q. Why was WebSecurityConfigurerAdapter deprecated?**
A. It required inheritance and made it hard to compose multiple chains; the @Bean SecurityFilterChain approach is composable and testable.

**Q. What is DelegatingFilterProxy and why does it exist?**
A. A servlet filter that bridges the container-managed filter chain and the Spring application context; it looks up FilterChainProxy by name and delegates to it.

**Q. How does FilterChainProxy pick which SecurityFilterChain to use?**
A. It iterates chains in @Order order and picks the first whose requestMatcher matches the request; remaining chains are skipped.

**Q. Why disable CSRF for stateless REST APIs?**
A. CSRF exploits sessions/cookies; JWT Bearer tokens are not sent automatically by browsers, so there is no attack vector — the protection is unnecessary overhead.

**Q. What is the difference between AuthenticationEntryPoint and AccessDeniedHandler?**
A. AuthenticationEntryPoint handles 401 (not authenticated); AccessDeniedHandler handles 403 (authenticated but lacks permission).

**Q. What filter does the actual authorization check?**
A. AuthorizationFilter (formerly FilterSecurityInterceptor); it runs last so all authentication filters run first to populate the SecurityContext.

**Q. STATELESS session policy — what does it change beyond not creating sessions?**
A. SecurityContextPersistenceFilter won't load/save context from a session store; each request must re-authenticate (e.g., re-validate JWT) — no sticky session state.

