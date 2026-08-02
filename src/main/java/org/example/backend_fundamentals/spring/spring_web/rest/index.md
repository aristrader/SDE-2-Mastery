---
order: 20
---

# Spring REST

Row 8 — 🔴 💼 | MP | 1 hr 45 min

Spring REST is Spring MVC used for APIs: controller methods read HTTP input and return data, usually JSON.

For interviews, focus on request mapping, request bodies, validation, status codes, and when `ResponseEntity` is needed.

---

## @RestController

`@RestController` means:

```java
@Controller
@ResponseBody
public @interface RestController { }
```

So every method return value is written to the HTTP response body instead of being treated as a view name.

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return userService.get(id);
    }
}
```

Use `@Controller` for MVC pages/views. Use `@RestController` for JSON APIs.

---

## Reading request input

```java
@GetMapping("/users/{id}/orders")
public List<OrderResponse> orders(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) String status,
        @RequestHeader("X-Request-Id") String requestId) {
    return orderService.findOrders(id, page, status);
}
```

| Annotation | Reads from | Example |
|---|---|---|
| `@PathVariable` | URL path | `/users/{id}` |
| `@RequestParam` | query string | `?page=2` |
| `@RequestHeader` | header | `X-Request-Id` |
| `@RequestBody` | request body | JSON payload |

Spring converts strings to common Java types such as `Long`, `UUID`, enums, and numbers.

---

## @RequestBody and validation

```java
public record CreateUserRequest(
        @NotBlank String name,
        @Email String email,
        @Min(18) int age) {
}
```

```java
@PostMapping("/users")
public ResponseEntity<UserResponse> create(
        @RequestBody @Valid CreateUserRequest request) {

    UserResponse created = userService.create(request);
    URI location = URI.create("/users/" + created.id());
    return ResponseEntity.created(location).body(created);
}
```

What happens:

1. Jackson converts JSON into `CreateUserRequest`.
2. `@Valid` runs Bean Validation.
3. If validation fails, Spring throws `MethodArgumentNotValidException`.
4. Your exception handler should return a clean `400` or `422` response.

`@Validated` is Spring-specific and mainly matters for validation groups or method-level validation on services. For normal request DTO validation, `@Valid` is enough.

---

## ResponseEntity

Return the object directly when `200 OK` is enough:

```java
@GetMapping("/{id}")
public UserResponse get(@PathVariable Long id) {
    return userService.get(id);
}
```

Use `ResponseEntity` when you need status or headers:

```java
@PostMapping
public ResponseEntity<UserResponse> create(@RequestBody @Valid CreateUserRequest request) {
    UserResponse created = userService.create(request);
    return ResponseEntity
        .created(URI.create("/users/" + created.id()))
        .body(created);
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
}
```

Interview rule:

> Return DTO directly for simple `200`. Use `ResponseEntity` for `201`, `204`, headers, or conditional statuses.

---

## HTTP status codes

| Status | Typical use |
|---|---|
| `200 OK` | successful read/update with response body |
| `201 Created` | resource created; include `Location` header |
| `204 No Content` | success with no body, often DELETE |
| `400 Bad Request` | invalid JSON, missing parameter, type mismatch |
| `404 Not Found` | resource does not exist |
| `409 Conflict` | duplicate resource, state/version conflict |
| `415 Unsupported Media Type` | request `Content-Type` is not accepted |

For validation failures, many teams use `400`. Some APIs use `422` to mean “JSON was valid, but the business/validation rules failed.” In interviews, explain the distinction and follow the API convention.

---

## HTTP methods

| Method | Meaning | Idempotent? |
|---|---|---|
| `GET` | read | yes |
| `POST` | create or submit command | no |
| `PUT` | full replace | yes |
| `PATCH` | partial update | not guaranteed |
| `DELETE` | delete | yes |

Key distinction:

- `PUT` sends the full replacement resource.
- `PATCH` sends only the changed fields.

---

## consumes and produces

```java
@PostMapping(
    value = "/users",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
public UserResponse create(@RequestBody @Valid CreateUserRequest request) {
    return userService.create(request);
}
```

- `consumes` restricts request body type. Mismatch can return `415`.
- `produces` restricts response body type. Mismatch can return `406`.

Use these when the API contract must be strict.

---

## CORS

CORS controls whether browsers allow frontend JavaScript from one origin to call your backend.

For one controller:

```java
@CrossOrigin(origins = "https://app.example.com")
@RestController
public class UserController {
}
```

For APIs, global config is usually cleaner:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://app.example.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

Important interview gotcha: if Spring Security is enabled, CORS must be allowed in the security filter chain too, because security runs before Spring MVC.

---

## Quick recall

**Q. What does `@RestController` add over `@Controller`?**  
A. It adds `@ResponseBody`, so return values are written as response body data.

**Q. `@PathVariable` vs `@RequestParam`?**  
A. Path variable comes from the URL path. Request param comes from query string/form params.

**Q. When is `ResponseEntity` needed?**  
A. When you need status, headers, an empty body, or conditional response handling.

**Q. `@Valid` vs `@Validated`?**  
A. `@Valid` is standard request DTO validation. `@Validated` is Spring-specific and useful for groups or method validation.

**Q. PUT vs PATCH?**  
A. PUT is full replacement and idempotent. PATCH is partial update and may or may not be idempotent.

**Q. Why configure CORS in Spring Security too?**  
A. Security filters run before MVC, so preflight requests can be blocked before MVC CORS config is reached.
