---
order: 10
---

# Spring Exception Handling

Row 9 — 🔴 💼 | MP | 1.5 hrs

---

## @ExceptionHandler — controller-local

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.find(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    // Handles UserNotFoundException thrown from ANY method in THIS controller only
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex,
                                                         HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of(404, ex.getMessage(), req.getRequestURI()));
    }
}
```

Scope: `@ExceptionHandler` applies only to exceptions thrown by methods in the **same controller class**. Spring searches the controller first, then `@ControllerAdvice` beans.

You can declare multiple exception types in one handler:

```java
@ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
public ResponseEntity<ErrorResponse> handleBadInput(RuntimeException ex) { ... }
```

---

## @ControllerAdvice — global handler

`@ControllerAdvice` is a specialization of `@Component` that applies `@ExceptionHandler` (and `@InitBinder`, `@ModelAttribute`) globally — across all controllers.

```java
@RestControllerAdvice   // = @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(UserNotFoundException ex, HttpServletRequest req) {
        return ErrorResponse.of(404, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)   // catch-all fallback
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return ResponseEntity.internalServerError()
            .body(ErrorResponse.of(500, "Internal error", req.getRequestURI()));
    }
}
```

**@RestControllerAdvice = @ControllerAdvice + @ResponseBody.** Return values go through `HttpMessageConverter` — no view resolution. Always prefer this for REST APIs.

**Scope narrowing** — advice can be restricted:

```java
@ControllerAdvice(basePackages = "com.example.api")
@ControllerAdvice(assignableTypes = {UserController.class, OrderController.class})
@ControllerAdvice(annotations = RestController.class)
```

---

## Resolution order

```
Exception thrown in controller method
        │
        ▼
Spring checks controller class for matching @ExceptionHandler
        │ (none found)
        ▼
Spring checks @ControllerAdvice beans (ordered by @Order / Ordered)
  - most specific exception type wins within a single advice
  - between advice beans: lower @Order value = higher priority
        │
        ▼
If still unhandled: DispatcherServlet's default error handling
  → delegated to /error endpoint (BasicErrorController in Spring Boot)
```

Within a single `@ControllerAdvice`, the **most specific** exception type wins — `UserNotFoundException` beats `RuntimeException` beats `Exception`.

---

## Standard ErrorResponse DTO

Build a consistent error shape — interviewers and API consumers expect this:

```java
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;       // e.g. "Not Found"
    private String message;     // human-readable detail
    private String path;        // request URI
    private List<FieldError> errors;  // populated for validation failures

    public static ErrorResponse of(int status, String message, String path) {
        ErrorResponse r = new ErrorResponse();
        r.timestamp = Instant.now();
        r.status = status;
        r.error = HttpStatus.valueOf(status).getReasonPhrase();
        r.message = message;
        r.path = path;
        r.errors = Collections.emptyList();
        return r;
    }

    // inner DTO
    public record FieldError(String field, String rejectedValue, String message) {}
}
```

Keep the shape consistent across all exception handlers — clients should parse a single `ErrorResponse` type regardless of which error occurred.

---

## Handling MethodArgumentNotValidException

Thrown by Spring when `@Valid`/`@Validated` on `@RequestBody` fails. Contains a `BindingResult` with all field-level and object-level errors.

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex, HttpServletRequest req) {

    List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(fe -> new ErrorResponse.FieldError(
            fe.getField(),
            fe.getRejectedValue() != null ? fe.getRejectedValue().toString() : null,
            fe.getDefaultMessage()))
        .toList();

    ErrorResponse body = ErrorResponse.of(400, "Validation failed", req.getRequestURI());
    body.setErrors(fieldErrors);
    return ResponseEntity.badRequest().body(body);
}
```

**Related exceptions to handle:**
- `ConstraintViolationException` — thrown when `@Validated` triggers validation on `@RequestParam`/`@PathVariable` or on service-layer method params (not `@RequestBody`)
- `HttpMessageNotReadableException` — malformed JSON body; thrown before `@Valid` runs → 400
- `MissingServletRequestParameterException` — required `@RequestParam` absent → 400

---

## @ResponseStatus on exception classes

Maps a custom exception to an HTTP status without writing handler code:

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found: " + id);
    }
}
```

Spring's `ResponseStatusExceptionResolver` intercepts this and writes the status + reason phrase. **Downside:** no body control — Spring writes a plain Whitelabel error page. For structured JSON responses, prefer `@ExceptionHandler`.

`ResponseStatusException` is the programmatic equivalent:

```java
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id);
```

---

## ResponseEntityExceptionHandler — the base class approach

Spring provides `ResponseEntityExceptionHandler` in `spring-webmvc`. It pre-handles all standard Spring MVC exceptions (`MethodArgumentNotValidException`, `HttpMessageNotReadableException`, `NoHandlerFoundException`, `HttpRequestMethodNotSupportedException`, etc.) and returns `ResponseEntity` with the right status but an empty body.

**Extend it** to get those handlers for free, then override only what you need to customize:

```java
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        // build your custom ErrorResponse and return it
        List<ErrorResponse.FieldError> fieldErrors = ...;
        ErrorResponse body = ErrorResponse.of(400, "Validation failed", getPath(request));
        body.setErrors(fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    // your custom exception handlers go here
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex,
                                                         HttpServletRequest req) { ... }
}
```

**Why extend it:** consistent handling of Spring's own exceptions without reinventing the wheel. Without it, Spring Boot falls back to `BasicErrorController` for standard exceptions — producing inconsistent response shapes.

---

## Spring exception hierarchy — know what to catch where

```
Throwable
└─ Exception
   ├─ RuntimeException
   │  ├─ DataAccessException  (Spring — wraps JDBC/JPA exceptions; hierarchy lets you catch DB errors generically)
   │  │  ├─ DuplicateKeyException
   │  │  ├─ DataIntegrityViolationException
   │  │  └─ ...
   │  ├─ HttpClientErrorException  (RestTemplate/WebClient — wraps 4xx responses from downstream)
   │  └─ ResponseStatusException   (Spring MVC — programmatic status setting)
   └─ NestedServletException       (wraps exceptions from Servlet layer)
```

**`DataAccessException`** — Spring's unified hierarchy translating vendor-specific SQL errors (`PSQLException`, `MySQLIntegrityConstraintViolationException`) into stable types. Catch `DuplicateKeyException` in your advice to return 409 Conflict without depending on a specific DB driver.

---

## Security exceptions — the @ControllerAdvice blindspot

Spring Security filters run **before** `DispatcherServlet`. Authentication/authorization failures throw in the filter chain — `@ControllerAdvice` never sees them.

| Scenario | Exception | Default handler |
|---|---|---|
| Unauthenticated request to secured resource | `AuthenticationException` | `AuthenticationEntryPoint` |
| Authenticated but insufficient permissions | `AccessDeniedException` | `AccessDeniedHandler` |

Configure in the `SecurityFilterChain`:

```java
http.exceptionHandling(ex -> ex
    .authenticationEntryPoint((req, res, authEx) -> {
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(objectMapper.writeValueAsString(
            ErrorResponse.of(401, "Unauthorized", req.getRequestURI())));
    })
    .accessDeniedHandler((req, res, accessEx) -> {
        res.setStatus(HttpStatus.FORBIDDEN.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(objectMapper.writeValueAsString(
            ErrorResponse.of(403, "Forbidden", req.getRequestURI())));
    })
);
```

This gives 401/403 responses the same `ErrorResponse` shape as your application errors.

---

## Problem Details — RFC 7807 (Spring Boot 3+)

Spring Boot 3 has native RFC 7807 Problem Details support. Enable with one property:

```yaml
spring.mvc.problemdetails.enabled=true
```

When enabled, Spring's built-in exception handlers (including `ResponseEntityExceptionHandler`) return `application/problem+json` with a standardised body:

```json
{
  "type": "https://example.com/errors/not-found",
  "title": "Not Found",
  "status": 404,
  "detail": "User not found: 42",
  "instance": "/users/42"
}
```

You can produce Problem Details manually with `ProblemDetail`:

```java
@ExceptionHandler(UserNotFoundException.class)
public ProblemDetail handleNotFound(UserNotFoundException ex, HttpServletRequest req) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setTitle("User Not Found");
    pd.setInstance(URI.create(req.getRequestURI()));
    return pd;
}
```

With Problem Details enabled, `ResponseEntityExceptionHandler` already produces `ProblemDetail` for all standard Spring MVC exceptions — override only when you need custom fields or a custom `type` URI.

---

## Quick recall

**Q. @ExceptionHandler in a controller vs @ControllerAdvice — scope difference?**
A. Controller-level handles exceptions from that controller only. `@ControllerAdvice` is global — applies to exceptions from any controller; Spring checks the controller first, then advice beans.

**Q. What is @RestControllerAdvice?**
A. `@ControllerAdvice` + `@ResponseBody` — return values from handler methods go through `HttpMessageConverter`, not view resolution. Always use this for REST APIs.

**Q. How does Spring pick which @ExceptionHandler to invoke when multiple match?**
A. Most specific exception type wins within a single advice. Between multiple `@ControllerAdvice` beans, lower `@Order` value = higher priority.

**Q. Why doesn't @ControllerAdvice catch 401/403 errors from Spring Security?**
A. Security filters run before `DispatcherServlet`. Those exceptions never reach the MVC layer. Handle them via `AuthenticationEntryPoint` and `AccessDeniedHandler` in `HttpSecurity.exceptionHandling()`.

**Q. What does extending ResponseEntityExceptionHandler give you?**
A. Pre-built handlers for all standard Spring MVC exceptions with correct HTTP status. Override specific methods to customize the response body to your `ErrorResponse` shape.

**Q. @ResponseStatus on an exception class vs @ExceptionHandler — trade-off?**
A. `@ResponseStatus` is simpler but gives no body control (plain error page). `@ExceptionHandler` provides full control over status, headers, and structured JSON body.

**Q. MethodArgumentNotValidException vs ConstraintViolationException — when does each fire?**
A. `MethodArgumentNotValidException` fires when `@Valid` fails on `@RequestBody`. `ConstraintViolationException` fires when `@Validated` triggers on `@RequestParam`/`@PathVariable` or service-layer method parameters.

**Q. What is RFC 7807 Problem Details and how do you enable it in Spring Boot 3?**
A. A standard JSON error format (`type`, `title`, `status`, `detail`, `instance`). Enable with `spring.mvc.problemdetails.enabled=true`; `ResponseEntityExceptionHandler` then produces `application/problem+json` automatically for all standard Spring MVC exceptions.


<ExerciseNav />
