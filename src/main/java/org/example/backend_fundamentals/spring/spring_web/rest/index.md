---
order: 40
---

# Spring REST

Row 8 — 🔴 💼 | MP | 1 hr 45 min

---

## @RestController internals

`@RestController` is a composed annotation:

```java
@Controller
@ResponseBody
public @interface RestController { ... }
```

`@ResponseBody` at the class level tells `RequestMappingHandlerAdapter` to pass every return value through `HttpMessageConverter` rather than to a `ViewResolver`. No ModelAndView involved.

**@Controller vs @RestController:** use `@Controller` when some methods render views and others return data (annotate data-returning methods individually with `@ResponseBody`). Use `@RestController` for pure REST endpoints — every method writes to the response body.

---

## @RequestBody

```java
@PostMapping("/users")
public ResponseEntity<User> create(@RequestBody @Valid CreateUserRequest req) { ... }
```

- Jackson's `MappingJackson2HttpMessageConverter` reads the HTTP body stream and deserializes it to `CreateUserRequest`
- `required = true` by default — if the body is absent or `Content-Type` is missing/wrong, Spring throws `HttpMessageNotReadableException` → 400
- Set `required = false` to allow an empty body (method parameter will be `null`)
- `@Valid` (or `@Validated`) on `@RequestBody` triggers JSR-380 Bean Validation after deserialization; failure raises `MethodArgumentNotValidException` → 400

**Deserialization gotcha:** Jackson ignores unknown properties by default (configurable via `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES`). A missing field keeps the default value — it does NOT fail unless you use `@JsonProperty(required = true)` or set `FAIL_ON_NULL_FOR_PRIMITIVES`.

---

## @PathVariable vs @RequestParam vs @RequestHeader

| Annotation | Extraction point | Type conversion | Required by default | Notes |
|---|---|---|---|---|
| `@PathVariable` | URI template segment `/users/{id}` | Yes — via `ConversionService` | Yes | Name inferred from param name if not specified |
| `@RequestParam` | Query string `?page=2` or form field | Yes | Yes (configurable) | `defaultValue` implicitly sets `required=false` |
| `@RequestHeader` | HTTP request header | Yes | Yes (configurable) | Header names are case-insensitive in HTTP but Spring matches them case-insensitively |

```java
@GetMapping("/users/{id}/orders")
public List<Order> getOrders(
    @PathVariable Long id,                              // /users/42/orders
    @RequestParam(defaultValue = "0") int page,        // ?page=2  (required=false implicitly)
    @RequestParam(required = false) String status,     // ?status=OPEN
    @RequestHeader("X-Correlation-Id") String corrId   // header
) { ... }
```

Type conversion uses Spring's `ConversionService`. Built-in converters handle `String → Long`, `String → UUID`, `String → Enum` (by name), etc. Register custom ones via `WebMvcConfigurer.addFormatters()`.

---

## Validation

### JSR-380 Bean Validation with @Valid

```java
public class CreateUserRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @Email
    @NotNull
    private String email;

    @Min(18)
    private int age;
}
```

`@Valid` triggers validation after deserialization. Failure throws `MethodArgumentNotValidException` — contains a `BindingResult` with all field errors.

### @Valid vs @Validated

| | `@Valid` | `@Validated` |
|---|---|---|
| Source | JSR-380 standard | Spring-specific |
| Group validation | No | Yes — `@Validated(CreateGroup.class)` |
| Cascaded validation | Yes — recurses into nested objects with `@Valid` on field | Yes |
| Method-level validation on service beans | No | Yes (via AOP proxy) |

**Method-level validation on services:** annotate the service class with `@Validated`; Spring wraps it in a proxy that validates constrained parameters and return values.

---

## ResponseEntity\<T\>

```java
// Return POJO directly — Spring infers 200 OK, picks converter from Accept header
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) { return userService.find(id); }

// ResponseEntity — full control over status + headers + body
@PostMapping
public ResponseEntity<User> createUser(@RequestBody @Valid CreateUserRequest req) {
    User created = userService.create(req);
    URI location = URI.create("/users/" + created.getId());
    return ResponseEntity
        .created(location)          // 201 Created + Location header
        .header("X-User-Id", String.valueOf(created.getId()))
        .body(created);
}

// No body
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();  // 204 No Content
}
```

**When to use `ResponseEntity` vs returning the object directly:**
- Return directly when 200 + default headers are always correct
- Use `ResponseEntity` when you need to: set a non-200 status (201, 202, 204), add response headers (Location, ETag, Cache-Control), conditionally return different status codes, or return an empty body with a specific status

---

## HATEOAS (Hypermedia as the Engine of Application State)

REST maturity level 3 (Richardson model). Responses include hypermedia links so clients discover actions without hardcoding URLs.

Spring HATEOAS (`spring-boot-starter-hateoas`) provides:
- `EntityModel<T>` — wraps a domain object with links
- `CollectionModel<T>` — wraps a collection with links
- `WebMvcLinkBuilder.linkTo(methodOn(...))` — builds type-safe links from controller methods

```java
@GetMapping("/{id}")
public EntityModel<User> getUser(@PathVariable Long id) {
    User user = userService.find(id);
    return EntityModel.of(user,
        linkTo(methodOn(UserController.class).getUser(id)).withSelfRel(),
        linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete")
    );
}
```

Response includes `_links`:
```json
{
  "id": 42, "name": "Alice",
  "_links": {
    "self":   { "href": "/users/42" },
    "delete": { "href": "/users/42" }
  }
}
```

Most REST APIs stop at level 2 (verbs + status codes). HATEOAS is worth knowing for interviews; rare in production Spring Boot apps unless building a public hypermedia API.

---

## HTTP status semantics

Correct status codes are a common interview topic.

| Status | When to use |
|---|---|
| 200 OK | Successful GET/PUT/PATCH with a body |
| 201 Created | Successful POST that created a resource — include `Location` header pointing to the new resource |
| 204 No Content | Successful DELETE or PUT/PATCH with no body to return |
| 400 Bad Request | Malformed request syntax, invalid JSON, type mismatch |
| 404 Not Found | Resource does not exist |
| 409 Conflict | Request conflicts with current state — e.g., duplicate email on register, optimistic lock conflict |
| 415 Unsupported Media Type | `Content-Type` doesn't match controller's `consumes` |
| 422 Unprocessable Entity | Request is well-formed but semantically invalid — e.g., Bean Validation failure (`MethodArgumentNotValidException`). Preferred over 400 when the body parsed correctly but business rules failed. |

**409 vs 422:** 409 = state conflict (already exists, version mismatch). 422 = validation failure (parsed fine, semantically wrong). Many APIs use 400 for both — 422 is the more precise choice for validation errors.

---

## HTTP method semantics

| Method | Safe | Idempotent | Typical use |
|---|---|---|---|
| GET | Yes | Yes | Fetch resource — must not change state |
| HEAD | Yes | Yes | Like GET but no body — check existence, get headers |
| POST | No | No | Create resource, submit data, trigger action |
| PUT | No | Yes | Full replace — same request N times = same state |
| PATCH | No | No* | Partial update — *can be made idempotent with conditional updates |
| DELETE | No | Yes | Delete — second call on deleted resource should return 404 or 204 |

**Safe** = no observable side effects. **Idempotent** = N identical requests = same server state as 1.

PUT sends the full resource; an omitted field is replaced with null/default. PATCH sends only changed fields — the server must merge correctly. Matters for API design (and is a common interview question).

---

## Shorthand mapping annotations

All are composed on `@RequestMapping`:

```java
@GetMapping("/users")        // @RequestMapping(method = GET)
@PostMapping("/users")       // @RequestMapping(method = POST)
@PutMapping("/users/{id}")   // @RequestMapping(method = PUT)
@PatchMapping("/users/{id}") // @RequestMapping(method = PATCH)
@DeleteMapping("/users/{id}")// @RequestMapping(method = DELETE)
```

---

## produces / consumes pinning

```java
@RestController
@RequestMapping(
    value = "/api/v1",
    produces = MediaType.APPLICATION_JSON_VALUE   // all methods produce JSON
)
public class UserController {

    @PostMapping(
        value = "/users",
        consumes = MediaType.APPLICATION_JSON_VALUE  // only accept JSON body
    )
    public User create(@RequestBody CreateUserRequest req) { ... }
}
```

- `produces`: if `Accept` header doesn't match → 406 Not Acceptable
- `consumes`: if `Content-Type` doesn't match → 415 Unsupported Media Type
- Useful to prevent accidental `text/xml` bodies from reaching your controller

---

## @CrossOrigin and global CORS

**Per-controller (narrow):**

```java
@CrossOrigin(origins = "https://app.example.com", maxAge = 3600)
@RestController
public class UserController { ... }
```

**Global (preferred for consistency):**

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://app.example.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

**Critical gotcha:** `@CrossOrigin` / `WebMvcConfigurer` CORS only applies to requests reaching `DispatcherServlet`. Spring Security's CORS filter runs earlier. If you use Spring Security, configure CORS there too (or first); otherwise Spring Security's pre-flight OPTIONS check will 403 before your CORS config runs.

```java
// In SecurityFilterChain:
http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
```

---

## Quick recall

**Q. What does @RestController add over @Controller?**
A. It meta-annotates `@ResponseBody` on the class — every method's return value goes through `HttpMessageConverter` to the response body; no view resolution. Missing or wrong `Content-Type` on the request body triggers `HttpMessageNotReadableException` → 400.

**Q. @Valid vs @Validated — when does @Validated matter?**
A. When you need validation groups (different rules per use case) or method-level validation on `@Service` beans via AOP proxy.

**Q. PUT vs PATCH — key difference?**
A. PUT is a full replace (missing fields → null/default). PATCH is a partial update (only changed fields). PUT is idempotent by definition; PATCH is not guaranteed to be.

**Q. When is ResponseEntity necessary instead of returning the POJO directly?**
A. When you need a non-200 status (201, 204), custom headers (Location, ETag), or a conditionally empty body.

**Q. How does @PathVariable type conversion work?**
A. Spring's `ConversionService` converts the String path segment to the parameter type (`Long`, `UUID`, `Enum`, etc.). Register custom converters via `WebMvcConfigurer.addFormatters()`.

**Q. @CrossOrigin vs WebMvcConfigurer CORS vs Spring Security CORS — which wins?**
A. Spring Security filter chain runs before `DispatcherServlet`. If security is present, configure CORS in `HttpSecurity.cors()` first; `WebMvcConfigurer`/`@CrossOrigin` alone won't be reached for preflight if security blocks it.

**Q. 409 Conflict vs 422 Unprocessable Entity — when to use each?**
A. 409 = state conflict (duplicate resource, optimistic lock failure). 422 = well-formed body that fails semantic/business validation (`MethodArgumentNotValidException`). Many APIs use 400 for both; 422 is the precise choice for validation errors.


<ExerciseNav />
