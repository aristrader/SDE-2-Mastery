# Feign Client — Coding Exercises

## Why this matters
Your KYC platform calls external identity and document verification APIs on every onboarding request. Feign turns those HTTP contracts into type-safe Java interfaces, eliminating RestTemplate boilerplate. Getting the error decoder and timeout config right is what separates a resilient integration from one that leaks threads or swallows upstream failures.

## Exercise 1: Declare the Feign client interface (~10 min)

**Goal:** Turn the identity-service HTTP contract into a type-safe Java interface.

**Task:** Create a `@FeignClient` interface named `IdentityServiceClient` pointing at `${identity.service.url}`. Add three methods:

```java
@FeignClient(name = "identity-service", url = "${identity.service.url}")
public interface IdentityServiceClient {
    @GetMapping("/users/{id}")
    UserResponse getUserById(@PathVariable("id") String id);

    @PostMapping("/users")
    UserResponse createUser(@RequestBody CreateUserRequest request);

    @GetMapping("/users")
    UserResponse getUserByEmail(@RequestParam("email") String email);
}
```

`@PathVariable`, `@RequestParam`, and `@RequestBody` work the same as in Spring MVC — Feign reuses the Spring Web annotations. `name` is used for service discovery (Eureka/Consul); even with a hard-coded `url`, `name` is still required as the client bean id.

Use `@GetMapping` / `@PostMapping` — not `@RequestMapping` on the interface itself.

**Gotcha:** `@RequestMapping` on the interface alongside `@FeignClient` causes Spring MVC to also register it as a controller in some Spring Boot versions. Stick to method-level mappings.

---

## Exercise 2: Request interceptor for auth propagation (~10 min)

**Goal:** Forward the calling request's `Authorization` header to every outbound Feign call.

**Task:** Define a `@Bean` that returns a `RequestInterceptor`. Inside it, read the current request's `Authorization` header via `RequestContextHolder` / `HttpServletRequest`, and apply it to the Feign `RequestTemplate` with `template.header("Authorization", value)`. Guard against no active request (batch jobs, async threads).

**Gotcha:** `RequestContextHolder` returns `null` outside a servlet request scope. Null-check before forwarding — otherwise a background thread calling a Feign client throws `NullPointerException` at runtime.

---

## Exercise 3: Custom ErrorDecoder (~15 min)

**Goal:** Replace the generic `FeignException` with domain exceptions your service can catch and handle cleanly.

**Task:** Implement `feign.codec.ErrorDecoder`. The interface has one method:

```java
public class IdentityErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        // response.status() gives you the HTTP status code as an int
        return switch (response.status()) {
            case 404 -> new UserNotFoundException("...");
            case 400 -> new ValidationException("...");
            case 503 -> new ServiceUnavailableException("...");
            default  -> defaultDecoder.decode(methodKey, response);
        };
    }
}
```

Map:
- HTTP 404 → throw `UserNotFoundException` (include the user id from the request if you can extract it from `methodKey`)
- HTTP 400 → throw `ValidationException` with the upstream response body as context
- HTTP 503 → throw `ServiceUnavailableException`
- Everything else → delegate to `ErrorDecoder.Default`

Register it as a `@Bean`.

**Gotcha:** `Response.body()` is a stream that Spring/Feign closes after `decode()` returns. Read the bytes inside the method — `EntityUtils`-style — before returning or throwing. Reading later (e.g., in a catch block up the stack) fails because the stream is already closed.

---

## Exercise 4: Timeout configuration (~5 min)

**Goal:** Prevent the identity-service calls from blocking a thread indefinitely.

**Task:** Add the following to `application.yml` under `spring.cloud.openfeign.client.config.identity-service`:
- `connectTimeout: 2000`
- `readTimeout: 5000`

Then write a one-paragraph comment explaining: what happens to the calling thread while a Feign call is in-flight, why a missing readTimeout is dangerous under load, and which value you'd tighten first for a user-facing endpoint vs. a batch job.

**Gotcha:** Feign is synchronous — each in-flight call holds a thread from the web server's pool. A slow or hung upstream with no readTimeout exhausts the pool under moderate load, causing cascading 503s even for endpoints that don't touch identity-service.

---

## Feign configuration — scoping and advanced knobs

### `@Configuration` on a Feign config class scopes it globally

A Feign config class annotated `@Configuration` gets picked up by component scan and applied to **every** Feign client — not just the one you intended. To scope it to a single client, omit `@Configuration` and pass it via `configuration = MyFeignConfig.class` in the `@FeignClient` annotation.

```java
// Correct: no @Configuration — applies only to IdentityServiceClient
public class IdentityFeignConfig {
    @Bean
    public ErrorDecoder errorDecoder() { return new IdentityErrorDecoder(); }
}

@FeignClient(name = "identity-service", url = "${identity.service.url}",
             configuration = IdentityFeignConfig.class)
public interface IdentityServiceClient { ... }
```

### Retry default — be careful with non-idempotent methods

Feign's default `Retryer` is `Retryer.NEVER_RETRY`. If you register a `Retryer.Default` bean, be cautious with non-idempotent calls (`POST`, `PATCH`) — a retry after a timeout can create duplicate records.

---

## Exercise 5: WireMock test outline (~5 min)

**Goal:** Sketch a test that verifies your Feign client and ErrorDecoder work together without hitting a real server.

**Task:** Write the outline (class shell + method stubs, no implementation needed) of a `@SpringBootTest` that:
1. Starts a WireMock server on a random port
2. Stubs `GET /users/123` to return 200 with a JSON body
3. Stubs `GET /users/999` to return 404
4. Calls `IdentityServiceClient` for both ids
5. Asserts the 200 case returns a populated `UserResponse` and the 404 case throws `UserNotFoundException`

Use `@AutoConfigureWireMock` (Spring Cloud Contract WireMock) or `WireMockServer` directly — your choice. Show the structure only.

**Gotcha:** The WireMock port must be injected into `identity.service.url` before the Feign client initializes. `@AutoConfigureWireMock(port = 0)` handles this automatically; a manual `WireMockServer` needs the property set in a `@DynamicPropertySource` method.

---

## Quick recall

**Q.** Why use `@GetMapping` on Feign methods instead of `@RequestMapping` on the interface?
**A.** `@RequestMapping` on the interface causes Spring MVC to register the Feign client as a controller in some Spring Boot versions, leading to ambiguous mapping errors at startup.

**Q.** What does `ErrorDecoder.decode()` receive, and what is the key constraint when reading the response body?
**A.** It receives a `feign.Response` with a streaming body. The stream is closed after `decode()` returns, so you must read the bytes inside the method — not lazily in a catch block higher up.

**Q.** What happens to a thread pool when Feign has no read timeout and the upstream hangs?
**A.** Every in-flight call holds a thread. Without a read timeout, threads pile up waiting indefinitely, exhausting the pool and causing cascading failures across all endpoints.

**Q.** How do you forward the caller's auth token to a downstream Feign call?
**A.** Implement `RequestInterceptor`, read the `Authorization` header from `RequestContextHolder`, and apply it to the `RequestTemplate`. Guard for null when outside a servlet scope.

**Q.** How is the `identity-service` Feign client timeout scoped — does it apply globally or per-client?
**A.** Per-client. `spring.cloud.openfeign.client.config.identity-service.*` applies only to that named client; `spring.cloud.openfeign.client.config.default.*` applies globally to all clients.

**Q.** You write a Feign config class with `@Configuration`. What unintended effect does that have?
**A.** Component scan picks it up and applies it to all Feign clients in the app. Omit `@Configuration` and pass the class via `@FeignClient(configuration = ...)` to scope it to one client.

**Q.** What is Feign's default retry behaviour, and when should you be careful adding retries?
**A.** Default is `Retryer.NEVER_RETRY`. Add a `Retryer.Default` bean to enable retries. Avoid retrying non-idempotent methods (`POST`, `PATCH`) — a retry after a timeout can create duplicate records.
