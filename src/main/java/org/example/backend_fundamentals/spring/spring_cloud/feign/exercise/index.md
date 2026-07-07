---
order: 10
search: false
---

# Feign / OpenFeign Practice

## Exercise: declare-feign-client - Declare the Feign client interface

### Goal
Turn the identity-service HTTP contract into a type-safe Java interface.

### Task
Create a `@FeignClient` interface named `IdentityServiceClient` pointing at `${identity.service.url}`. Add three methods:
- `getUserById(String id)` for GET `/users/{id}`
- `createUser(CreateUserRequest request)` for POST `/users`
- `getUserByEmail(String email)` for GET `/users`

`@PathVariable`, `@RequestParam`, and `@RequestBody` work the same as in Spring MVC. `name` is used for service discovery and as the client bean id; even with a hard-coded `url`, `name` is still required.

### Gotcha
Use `@GetMapping` / `@PostMapping` on methods — not `@RequestMapping` on the interface itself. `@RequestMapping` on the interface alongside `@FeignClient` causes Spring MVC to also register it as a controller in some Spring Boot versions.

## Exercise: request-interceptor-auth - Request interceptor for auth propagation

### Goal
Forward the calling request's `Authorization` header to every outbound Feign call.

### Task
Define a `@Bean` that returns a `RequestInterceptor`. Inside it, read the current request's `Authorization` header via `RequestContextHolder` / `HttpServletRequest`, and apply it to the Feign `RequestTemplate`. Guard against no active request.

### Gotcha
`RequestContextHolder` returns `null` outside a servlet request scope. Null-check before forwarding.

## Exercise: custom-errordecoder - Custom ErrorDecoder

### Goal
Replace the generic `FeignException` with domain exceptions your service can catch and handle cleanly.

### Task
Implement `feign.codec.ErrorDecoder`. Map:
- HTTP 404 → throw `UserNotFoundException`
- HTTP 400 → throw `ValidationException`
- HTTP 503 → throw `ServiceUnavailableException`
- Everything else → delegate to `ErrorDecoder.Default`
Register it as a `@Bean`.

### Gotcha
`response.status()` gives you the HTTP status code as an int. Include useful context where possible: user id from `methodKey` for 404s, and the upstream response body for 400 validation failures. `Response.body()` is a stream that Spring/Feign closes after `decode()` returns; read the bytes inside the method before returning or throwing.

## Exercise: timeout-configuration - Timeout configuration

### Goal
Prevent the identity-service calls from blocking a thread indefinitely.

### Task
Add the following to `application.yml` under `spring.cloud.openfeign.client.config.identity-service`:
- `connectTimeout: 2000`
- `readTimeout: 5000`

Then write a one-paragraph comment explaining: what happens to the calling thread while a Feign call is in-flight, why a missing readTimeout is dangerous under load, and which value you would tighten first for a user-facing endpoint vs. a batch job.

### Gotcha
Feign is synchronous — each in-flight call holds a thread from the web server's pool. A slow or hung upstream with no readTimeout exhausts the pool.

## Exercise: wiremock-test-outline - WireMock test outline

### Goal
Sketch a test that verifies your Feign client and ErrorDecoder work together without hitting a real server.

### Task
Write the outline (class shell + method stubs, no implementation needed) of a `@SpringBootTest` using `@AutoConfigureWireMock` or a manual `WireMockServer` that:
1. Starts a WireMock server on a random port
2. Stubs `GET /users/123` to return 200 with a JSON body
3. Stubs `GET /users/999` to return 404
4. Calls `IdentityServiceClient` for both ids
5. Asserts the 200 case returns a populated `UserResponse` and the 404 case throws `UserNotFoundException`

### Gotcha
The WireMock port must be injected into `identity.service.url` before the Feign client initializes.
