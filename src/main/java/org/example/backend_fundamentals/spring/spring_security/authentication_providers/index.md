---
order: 10
---

# Authentication Providers

---

## Authentication flow

```
HTTP request
    │
    ▼
UsernamePasswordAuthenticationFilter
    │  creates Authentication (unauthenticated, holds credentials)
    ▼
AuthenticationManager  (interface)
    │
    ▼
ProviderManager  (default impl — iterates a list of AuthenticationProviders)
    │  calls provider.supports(auth.getClass()) for each
    │  first provider that returns true → calls provider.authenticate(auth)
    ▼
AuthenticationProvider
    │  success → returns fully populated Authentication (principal, authorities, credentials cleared)
    │  failure → throws AuthenticationException
    ▼
SecurityContextHolder.getContext().setAuthentication(auth)
```

`ProviderManager` can have a **parent** `ProviderManager` — if no provider in the child list supports the token, it delegates to the parent. Useful when multiple `SecurityFilterChain` beans share a common parent (e.g., shared JWT provider + per-chain password provider).

---

## AuthenticationProvider contract

```java
public interface AuthenticationProvider {
    Authentication authenticate(Authentication authentication) throws AuthenticationException;
    boolean supports(Class<?> authentication);
}
```

- `supports()` is called first. Return `true` only for the `Authentication` subtype you handle — prevents intercepting the wrong token type.
- `authenticate()` returns a **fully populated** `Authentication` on success (principal set, authorities set, credentials usually cleared). Return `null` to signal "I can't handle this — try the next provider." Throw `AuthenticationException` to signal "I handled it and it failed."

| Built-in provider | Handles |
|---|---|
| `DaoAuthenticationProvider` | `UsernamePasswordAuthenticationToken` — username/password form login |
| `JwtAuthenticationProvider` (OAuth2) | `BearerTokenAuthenticationToken` — JWT resource server |
| `RememberMeAuthenticationProvider` | `RememberMeAuthenticationToken` |
| `AnonymousAuthenticationProvider` | `AnonymousAuthenticationToken` |

---

## DaoAuthenticationProvider + UserDetailsService

`DaoAuthenticationProvider` is the workhorse for username/password flows.

```
DaoAuthenticationProvider.authenticate(UsernamePasswordAuthenticationToken)
    │
    ├─ calls UserDetailsService.loadUserByUsername(username)
    │      └─ returns UserDetails (or throws UsernameNotFoundException)
    │
    ├─ calls PasswordEncoder.matches(rawPassword, storedHash)
    │      └─ mismatch → throws BadCredentialsException
    │
    └─ builds authenticated token: principal=UserDetails, credentials=null, authorities from UserDetails
```

`UserDetailsService` has a single method:

```java
UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
```

Spring handles all password encoding and comparison. `UserDetailsService` only loads the user record — do not compare passwords yourself.

---

## UserDetails contract — all 7 methods

```java
public interface UserDetails extends Serializable {
    String getUsername();
    String getPassword();           // stored hash, NOT plain text
    Collection<? extends GrantedAuthority> getAuthorities();
    boolean isEnabled();
    boolean isAccountNonExpired();
    boolean isAccountNonLocked();
    boolean isCredentialsNonExpired();
}
```

| Method | Returns false → Spring throws |
|---|---|
| `isEnabled()` | `DisabledException` |
| `isAccountNonExpired()` | `AccountExpiredException` |
| `isAccountNonLocked()` | `LockedException` |
| `isCredentialsNonExpired()` | `CredentialsExpiredException` |

All four exceptions extend `AuthenticationException`. Check order in `DaoAuthenticationProvider`: enabled → non-expired → non-locked → credentials non-expired → password match. A locked account is rejected before the password is checked.

**Interview trap:** `UsernameNotFoundException` is swallowed by `DaoAuthenticationProvider` and rethrown as `BadCredentialsException` by default (`hideUserNotFoundExceptions=true`). This prevents username enumeration — callers cannot distinguish "wrong user" from "wrong password."

---

## PasswordEncoder

Never store plain text passwords. The standard:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // default cost factor = 10
}
```

**BCrypt internals worth knowing:**
- Adaptive: cost factor (work factor) is tunable; increasing it slows brute-force proportionally.
- Salted: each hash includes a random salt, so two users with the same password get different hashes.
- The stored string embeds the algorithm + cost + salt: `$2a$10$<22-char-salt><31-char-hash>`

**DelegatingPasswordEncoder** — for migrating legacy users:

```java
PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
// stored hash format: {bcrypt}$2a$10$... or {sha256}... or {noop}plaintext
```

`DelegatingPasswordEncoder` reads the `{id}` prefix to pick the right encoder for comparison. On successful login it can re-encode to the preferred algorithm. New registrations get `{bcrypt}`.

---

## AnonymousAuthenticationFilter

`AnonymousAuthenticationFilter` runs after all "real" authentication filters. If the `SecurityContext` is still empty at that point (no one authenticated the request), it sets a default `AnonymousAuthenticationToken` so the rest of the chain always has a non-null `Authentication` to work with.

```
...
BearerTokenAuthenticationFilter   ← JWT check
    │ (no token found — context still empty)
    ▼
AnonymousAuthenticationFilter      ← sets AnonymousAuthenticationToken
    │  principal = "anonymousUser"
    │  authority = "ROLE_ANONYMOUS"
    ▼
ExceptionTranslationFilter + AuthorizationFilter
    └─ if endpoint requires auth → AccessDeniedException → 401
```

**Why it matters:** downstream code and SpEL expressions like `isAnonymous()` / `isAuthenticated()` can always call `SecurityContextHolder.getContext().getAuthentication()` without a null check. Without this filter, unauthenticated requests would leave a null `Authentication` and force defensive null-checks everywhere downstream.

`AnonymousAuthenticationProvider` is the matching provider; it validates the key on the token (a shared secret string, default `"key"`) to confirm the token was created by the same filter.

---

## Custom AuthenticationProvider

Use when credentials are not username/password: API key, OTP, biometric token, SAML assertion.

```java
@Component
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {

    private final ApiKeyService apiKeyService;

    @Override
    public boolean supports(Class<?> authentication) {
        return ApiKeyAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String key = (String) authentication.getCredentials();
        UserDetails user = apiKeyService.findByKey(key)
            .orElseThrow(() -> new BadCredentialsException("Invalid API key"));
        return new ApiKeyAuthenticationToken(user, null, user.getAuthorities());
    }
}
```

Wire it in:

```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}

// or manually:
@Bean
public ProviderManager providerManager(ApiKeyAuthenticationProvider apiKeyProvider,
                                       DaoAuthenticationProvider daoProvider) {
    return new ProviderManager(List.of(apiKeyProvider, daoProvider));
}
```

Throw `BadCredentialsException` on auth failure. Do NOT return `null` on failure — `null` means "I didn't handle it, try next provider."

---

## SecurityContext and Authentication lifecycle

```
Request arrives
    └─ SecurityContextPersistenceFilter loads context from SecurityContextRepository
           (session-based: HttpSessionSecurityContextRepository
            stateless: NullSecurityContextRepository for JWT APIs)

    └─ authentication succeeds → Authentication stored in SecurityContextHolder

    └─ SecurityContextHolder.getContext().getAuthentication()
           .getPrincipal()    → UserDetails (or JWT claims object)
           .getCredentials()  → null (cleared after auth to avoid holding passwords in memory)
           .getAuthorities()  → Collection<GrantedAuthority>

Request ends
    └─ SecurityContextPersistenceFilter saves context (session) or clears it (stateless)
    └─ SecurityContextHolder.clearContext()
```

**Authentication object structure:**

| Field | Content after successful auth |
|---|---|
| `principal` | `UserDetails` instance (or JWT claims for OAuth2 resource server) |
| `credentials` | `null` — cleared by `DaoAuthenticationProvider` after auth succeeds |
| `authorities` | roles/permissions — `ROLE_ADMIN`, `user:read`, etc. |
| `isAuthenticated()` | `true` |

**Thread-local strategy (default):** `SecurityContextHolder` stores the context in a `ThreadLocal`. For async code (`@Async`, reactive) you must propagate it explicitly or switch to `MODE_INHERITABLETHREADLOCAL`.

---

## Interview gotchas

- `ProviderManager` returns the result of the **first** provider that does not return `null` and does not throw. Order matters.
- A provider that throws `AuthenticationException` stops the chain — the exception propagates. Returning `null` lets the chain continue.
- `UserDetailsService` should never perform password comparison. That's `PasswordEncoder`'s job inside the provider.
- Credentials are cleared from the `Authentication` after success to avoid holding raw passwords in the `SecurityContext`. `null` credentials in a `@PostAuthorize` expression is expected.
- `hideUserNotFoundExceptions=true` (default) converts `UsernameNotFoundException` → `BadCredentialsException`. Set `false` only in internal admin tooling where user enumeration is acceptable.

---

## Quick recall

**Q. What is the role of `ProviderManager`?**
A. It iterates a list of `AuthenticationProvider` beans, calling `supports()` then `authenticate()` on each until one succeeds or all fail.

**Q. What does returning `null` from `authenticate()` mean vs throwing `BadCredentialsException`?**
A. `null` = "I can't handle this token, try the next provider." Exception = "I handled it and auth failed" — stops the chain.

**Q. Why does `DaoAuthenticationProvider` rethrow `UsernameNotFoundException` as `BadCredentialsException`?**
A. To prevent username enumeration — callers cannot distinguish a missing user from a wrong password.

**Q. What happens when `UserDetails.isAccountNonLocked()` returns false?**
A. `DaoAuthenticationProvider` throws `LockedException` before even checking the password.

**Q. What is `DelegatingPasswordEncoder` for?**
A. Migrating legacy password hashes — reads a `{id}` prefix on stored hashes to pick the right decoder; re-encodes to the preferred algorithm on next successful login.

**Q. How do you add a custom auth mechanism (e.g., API key) without touching the username/password flow?**
A. Implement `AuthenticationProvider` with `supports()` targeting your custom token type, and register it alongside `DaoAuthenticationProvider` in a `ProviderManager`.

**Q. What does AnonymousAuthenticationFilter do and why is it useful?**
A. If no prior filter authenticated the request, it sets an AnonymousAuthenticationToken so the SecurityContext is never null — downstream code and SpEL expressions like isAnonymous() always have a non-null Authentication to check.


<ExerciseNav />
