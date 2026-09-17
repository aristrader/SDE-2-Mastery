---
order: 10
search: false
---

# Feign / OpenFeign Practice

## Scenario

The verification service calls an internal identity service. Keep the HTTP boundary in one typed client;
callers should not build URLs, headers, or status-code branches themselves. Assume Feign clients are enabled
in the application and that `CreateUserRequest`, `UserResponse`, and the domain exceptions already exist.

Complete the exercises in order. Each one adds one responsibility at the outbound boundary; do not add
retries, a circuit breaker, or a real downstream service unless the exercise asks for it.

## Exercise: declare-feign-client - Declare the Feign client interface

### Goal

Turn the identity-service HTTP contract into a type-safe Java interface.

### Task

Create `IdentityServiceClient` with `@FeignClient(name = "identity-service", url =
"${identity.service.url}")` and these methods:

- `getUserById(String id)` for `GET /users/{id}`
- `createUser(CreateUserRequest request)` for `POST /users`
- `getUserByEmail(String email)` for `GET /users?email=...`

Use `@GetMapping` or `@PostMapping` with explicit `@PathVariable`, `@RequestParam`, and `@RequestBody`
annotations. The method names are Java names; the mappings are the HTTP contract.

### Acceptance criteria

- The client is named `identity-service` and reads its base URL from the supplied property.
- Each method has the required HTTP method, path, and parameter location.
- The email lookup is a collection query, not a second path such as `/users/email/{email}`.

## Exercise: request-interceptor-auth - Propagate inbound authorization

### Goal

Forward the current request's `Authorization` header to every identity-service call made on its behalf.

### Task

Define a `RequestInterceptor` bean in a configuration supplied only to `IdentityServiceClient`. Read the
current servlet request through `RequestContextHolder`. If a non-blank `Authorization` header exists, add
it to the outbound `RequestTemplate`; if there is no servlet request, do nothing.

### Acceptance criteria

- The interceptor does not throw when the client is called from a scheduled job or message consumer.
- It forwards the original header value without logging it.
- It is scoped to this trusted internal client, not accidentally applied to unrelated external clients.

## Exercise: custom-errordecoder - Map upstream failures to domain failures

### Goal

Replace generic Feign exceptions with failures the calling service can handle deliberately.

### Task

Implement `feign.codec.ErrorDecoder` with these mappings:

- `404` to `UserNotFoundException`
- `400` to `ValidationException`
- `503` to `ServiceUnavailableException`
- every other non-2xx response to `ErrorDecoder.Default`

Register the decoder in the client-specific configuration. Read an upstream response body, if needed for a
safe validation message, inside `decode`; it is not a reusable response object after the method returns.

### Acceptance criteria

- The decoder returns an exception; Feign throws that returned exception for the caller.
- It handles a missing response body safely.
- It does not invent a user ID from `methodKey`; use structured request context or safe observability if
  that information is required.

## Exercise: timeout-configuration - Bound a blocking downstream call

### Goal

Make the latency budget for identity-service calls explicit.

### Task

Add this per-client configuration to `application.yml`:

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          identity-service:
            connectTimeout: 2000
            readTimeout: 5000
```

Then write a short explanation of what waits during a blocking Feign call, why an unbounded downstream
wait harms the caller under load, and which timeout you would revisit for an interactive request versus a
longer batch workflow.

### Acceptance criteria

- The property path is `spring.cloud.openfeign.client.config`.
- The explanation distinguishes connection establishment from waiting for the response.
- It treats `2s` and `5s` as a starting policy, not universal values.

## Exercise: wiremock-test-outline - Verify the client boundary without a real service

### Goal

Outline an integration test that proves HTTP mapping and error mapping work together.

### Task

Write the class shell and test method outlines for a Spring test that:

1. starts a `WireMockServer` on a dynamic port before the application context is built;
2. supplies its base URL to `identity.service.url` through `@DynamicPropertySource`;
3. stubs `GET /users/123` with a `200` JSON response;
4. stubs `GET /users/999` with `404`;
5. asserts a populated `UserResponse` for `123` and `UserNotFoundException` for `999`.

Do not implement a real identity service or add retry testing here.

### Acceptance criteria

- The dynamic URL is available before Feign resolves the client property.
- The test exercises `IdentityServiceClient`, not the decoder class in isolation.
- The 404 assertion proves the custom decoder is registered for this client.

## Quick recall

**Q. Why scope a Feign configuration to one client?**

It prevents an identity-specific interceptor or error policy from silently changing calls to other services.

**Q. What does a blocking client timeout protect?**

It bounds how long the caller's thread waits for a failing or slow downstream dependency.
