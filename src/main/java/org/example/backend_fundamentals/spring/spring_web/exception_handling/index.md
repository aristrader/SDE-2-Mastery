---
order: 40
---

# Spring Exception Handling

Study-plan priority — 🔴 💼 | MP | 1.5 hrs

REST APIs should not leak raw stack traces or random Spring error bodies. The standard interview answer is: use `@RestControllerAdvice` with specific `@ExceptionHandler` methods, extend `ResponseEntityExceptionHandler` to safeguard built-in MVC errors, and return one consistent error shape (custom DTO or RFC 9457 `ProblemDetail`).

---

## Architectural exception flow

When an exception occurs during request processing, its handling depends strictly on whether it was thrown inside or outside the `DispatcherServlet` boundary:

```mermaid
flowchart TD
    Req[Incoming HTTP Request] --> SecFilters[Spring Security Filter Chain]
    SecFilters -->|Filter Auth Failure| SecHandler[AuthenticationEntryPoint / AccessDeniedHandler]
    SecFilters -->|Authenticated & Allowed| DS[DispatcherServlet]

    DS --> Controller[Controller / Service Execution]
    Controller -->|Throws Exception| Resolver[HandlerExceptionResolver Chain]

    Resolver --> Check1{Controller-local<br/>@ExceptionHandler?}
    Check1 -->|Match found| ExecLocal[Execute Controller Handler]
    Check1 -->|No match| Check2{Global<br/>@RestControllerAdvice?}

    Check2 -->|Match found| ExecGlobal[Execute Advice Handler<br/>Ordered by @Order & Exception Specificity]
    Check2 -->|No match| Check3{ResponseEntityExceptionHandler<br/>Standard MVC Exception?}

    Check3 -->|Match found| ExecStd[Return RFC 9457 / Standard Response<br/>e.g., 400, 405, 415]
    Check3 -->|No match| Check4{@ResponseStatus /<br/>ResponseStatusException?}

    Check4 -->|Match found| ExecStatus[ResponseStatusExceptionResolver]
    Check4 -->|No match| BootError[Default Boot /error Dispatch<br/>BasicErrorController / ErrorAttributes]
```

---

## Controller-local handler

`@ExceptionHandler` inside a controller handles exceptions thrown by handler methods in that specific controller only.

```java
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return userService.find(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(UserNotFoundException ex,
                                                   HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError.of(404, ex.getMessage(), request.getRequestURI()));
    }
}
```

Controller-local handlers take precedence over global advice handlers. They are useful for controller-specific recovery logic, but scattered handlers cause inconsistent error responses across microservices. For standard REST APIs, centralized global handling is preferred.

---

## Global handler with @RestControllerAdvice

Use `@RestControllerAdvice` to define centralized exception handling across all controllers.

`@RestControllerAdvice` is a meta-annotation composed of `@ControllerAdvice` + `@ResponseBody`. Return values are serialized directly to the HTTP response body via configured `HttpMessageConverter`s (typically Jackson).

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(UserNotFoundException ex,
                                                   HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError.of(404, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleConflict(IllegalStateException ex,
                                                   HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiError.of(409, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleFallback(Exception ex,
                                                   HttpServletRequest request) {
        return ResponseEntity.internalServerError()
            .body(ApiError.of(500, "Internal error", request.getRequestURI()));
    }
}
```

Map specific domain exceptions before generic fallbacks:

- `UserNotFoundException` → `404 Not Found`
- duplicate/state conflict (`IllegalStateException`, optimistic locking) → `409 Conflict`
- validation failure (`MethodArgumentNotValidException`) → `400 Bad Request` or `422 Unprocessable Entity`
- unknown exception → `500 Internal Server Error`

---

## Extending ResponseEntityExceptionHandler

A common production pitfall occurs when declaring a generic `@ExceptionHandler(Exception.class)` fallback without extending `ResponseEntityExceptionHandler`.

### The catch-all trap
Spring MVC raises built-in exceptions for protocol and routing failures:
- Sending `POST` to a `GET`-only endpoint raises `HttpRequestMethodNotSupportedException` (expected: HTTP 405 Method Not Allowed).
- Sending XML when only JSON is consumed raises `HttpMediaTypeNotSupportedException` (expected: HTTP 415 Unsupported Media Type).
- Malformed JSON payloads raise `HttpMessageNotReadableException` (expected: HTTP 400 Bad Request).

If a custom `@RestControllerAdvice` defines `@ExceptionHandler(Exception.class)` and does not extend `ResponseEntityExceptionHandler`, that catch-all method intercepts all standard MVC exceptions and converts them into misleading **500 Internal Server Error** responses!

### The standard solution
Extend `ResponseEntityExceptionHandler` and override specific hook methods:

```java
@RestControllerAdvice
public class CentralizedExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(UserNotFoundException ex,
                                                   HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError.of(404, ex.getMessage(), request.getRequestURI()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<ApiError.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> new ApiError.FieldError(err.getField(), err.getDefaultMessage()))
            .toList();

        ApiError body = new ApiError(
            Instant.now(),
            status.value(),
            HttpStatus.valueOf(status.value()).getReasonPhrase(),
            "Validation failed",
            request.getDescription(false).replace("uri=", ""),
            fieldErrors
        );

        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleFallback(Exception ex,
                                                   HttpServletRequest request) {
        return ResponseEntity.internalServerError()
            .body(ApiError.of(500, "Internal error", request.getRequestURI()));
    }
}
```

`ResponseEntityExceptionHandler` handles all standard Spring MVC exceptions out of the box, preserving RFC-compliant status codes and headers (such as `Allow` on 405).

---

## Error response shapes: ApiError vs RFC 9457 ProblemDetail

API clients should not parse divergent error formats for different failures. Two common patterns exist:

### 1. Bespoke ApiError record

```java
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors) {

    public static ApiError of(int status, String message, String path) {
        HttpStatus httpStatus = HttpStatus.valueOf(status);
        return new ApiError(
            Instant.now(),
            status,
            httpStatus.getReasonPhrase(),
            message,
            path,
            List.of()
        );
    }

    public record FieldError(String field, String message) {}
}
```

### 2. RFC 9457 / RFC 7807 ProblemDetail (Spring 6 / Boot 3)

Spring Framework 6 introduced `org.springframework.http.ProblemDetail`, implementing the IETF specification for HTTP problem details.

Standard RFC 9457 fields:
- `type`: URI identifying the problem type (defaults to `about:blank`).
- `title`: Short, human-readable summary of the problem type.
- `status`: HTTP status code.
- `detail`: Human-readable explanation specific to this occurrence.
- `instance`: URI reference that identifies the specific occurrence of the problem.
- `properties`: Generic map for custom extensions (error codes, validation errors, timestamps).

```java
@ExceptionHandler(UserNotFoundException.class)
public ProblemDetail handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problem.setTitle("User Not Found");
    problem.setType(URI.create("https://api.example.com/errors/user-not-found"));
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("timestamp", Instant.now());
    problem.setProperty("errorCode", "USER_NOT_FOUND");
    return problem;
}
```

To enable RFC 9457 responses automatically for all built-in Spring MVC exceptions, configure:

```properties
spring.mvc.problemdetails.enabled=true
```

| Feature | Bespoke ApiError DTO | RFC 9457 ProblemDetail |
|---|---|---|
| **Specification** | Team-specific convention | IETF RFC 7807 / RFC 9457 standard |
| **Spring Support** | Requires custom serialization | Built-in native support in Spring 6+ |
| **Custom Fields** | Added as record/class fields | Placed in `properties` map via `setProperty()` |
| **Client Interop** | Proprietary SDK integration | Generic HTTP API client compliance |

---

## Validation errors

Validation triggers different exceptions depending on where annotations are evaluated:

### Request body validation: @Valid
When `@Valid` is placed on `@RequestBody`, validation failures throw `MethodArgumentNotValidException`. Extract field errors through `ex.getBindingResult().getFieldErrors()`.

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex,
                                           HttpServletRequest request) {
    List<ApiError.FieldError> fieldErrors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> new ApiError.FieldError(
            error.getField(),
            error.getDefaultMessage()))
        .toList();

    ApiError body = new ApiError(
        Instant.now(),
        400,
        "Bad Request",
        "Validation failed",
        request.getRequestURI(),
        fieldErrors);

    return ResponseEntity.badRequest().body(body);
}
```

### Parameter & Service-level validation: @Validated
When validation annotations (`@NotNull`, `@Min`) are placed on `@PathVariable`, `@RequestParam`, or service interface methods (enabled by class-level `@Validated`), Spring executes method validation via AOP, throwing `jakarta.validation.ConstraintViolationException`.

```java
@ExceptionHandler(ConstraintViolationException.class)
public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                          HttpServletRequest request) {
    String message = ex.getConstraintViolations().stream()
        .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
        .collect(Collectors.joining(", "));

    return ResponseEntity.badRequest().body(ApiError.of(400, message, request.getRequestURI()));
}
```

### Related web exceptions
- Malformed JSON syntax → `HttpMessageNotReadableException` (HTTP 400).
- Missing required query parameter → `MissingServletRequestParameterException` (HTTP 400).
- Missing path variable or type mismatch → `MethodArgumentTypeMismatchException` (HTTP 400).

---

## Handler selection and resolution mechanics

`ExceptionHandlerExceptionResolver` executes the exception resolution algorithm:

1. **Hierarchy depth / exception specificity**:
   Spring calculates the inheritance distance between the thrown exception and the parameter types declared on `@ExceptionHandler` methods. The handler with the shortest distance wins.

   If `UserNotFoundException extends RuntimeException`, and both handlers exist:
   ```java
   @ExceptionHandler(UserNotFoundException.class) // distance = 0 (Wins)
   @ExceptionHandler(RuntimeException.class)      // distance = 1
   ```

2. **Resolution order**:
   - Step 1: Controller-local `@ExceptionHandler` methods.
   - Step 2: Global `@RestControllerAdvice` / `@ControllerAdvice` beans (sorted by Spring `@Order` / `Ordered` precedence).
   - Step 3: `ResponseEntityExceptionHandler` or `ResponseStatusExceptionResolver`.
   - Step 4: Fall through to servlet container / Spring Boot `/error` (`BasicErrorController`).

3. **The ambiguity trap**:
   If an exception matches two `@ExceptionHandler` methods at the same inheritance distance in the same advice class, Spring throws `IllegalStateException: Ambiguous @ExceptionHandler method mapped for...` at startup or runtime.

   Similarly, if two `@ControllerAdvice` classes define identical exception handlers without explicit `@Order`, the execution order is undefined and depends on bean registration sequence. Always annotate multiple advice classes with `@Order`.

---

## @ResponseStatus and ResponseStatusException

### @ResponseStatus on exception class
```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found: " + id);
    }
}
```

`ResponseStatusExceptionResolver` inspects the thrown exception for `@ResponseStatus` and sets the HTTP status via `HttpServletResponse.sendError()`.
- Pro: Extremely simple for quick prototypes.
- Con: Hardcodes HTTP status onto domain exceptions; bypasses structured JSON body generation unless handled by Boot's `/error` controller.

### ResponseStatusException (programmatic)
```java
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
```
- Pro: Dynamic status and reason without creating dedicated exception classes.
- Con: Encourages business logic to depend directly on Spring web HTTP abstractions.

---

## Security exceptions vs MVC advice

A common interview question asks why `@RestControllerAdvice` fails to catch `401 Unauthorized` or `403 Forbidden` produced by Spring Security.

### The architectural boundary
Spring Security is a servlet `Filter` chain (`FilterChainProxy`) executed **before** the request reaches `DispatcherServlet`.

Because `@ControllerAdvice` is an interceptor inside `DispatcherServlet`, exceptions thrown during filter execution never reach Spring MVC's exception resolver:

| Failure Location | HTTP Status | Mechanism | Handling Component |
|---|---|---|---|
| Unauthenticated in Filter | `401 Unauthorized` | Security Filter Chain | `AuthenticationEntryPoint` |
| Forbidden in Filter | `403 Forbidden` | Security Filter Chain | `AccessDeniedHandler` |
| Method Security (`@PreAuthorize`) | `403 Forbidden` | Controller/Service execution | `@ExceptionHandler(AccessDeniedException.class)` or `AccessDeniedHandler` |

### Method security exception trap
When `@PreAuthorize("hasRole('ADMIN')")` fails, Spring Security throws `org.springframework.security.access.AccessDeniedException` inside the controller invocation.

Because this occurs inside `DispatcherServlet`:
- If you declare `@ExceptionHandler(AccessDeniedException.class)` in your `@RestControllerAdvice`, your advice catches it and formats the response.
- **The catch-all danger**: If your `@RestControllerAdvice` contains a generic `@ExceptionHandler(Exception.class)` fallback without handling `AccessDeniedException`, it catches the security violation and converts a legitimate 403 Forbidden into an HTTP **500 Internal Server Error**.
- **Bridging filter exceptions to `@ControllerAdvice`**: To route filter errors (e.g., JWT validation failure in `OncePerRequestFilter`) into `@RestControllerAdvice`, inject `HandlerExceptionResolver` into the filter and invoke:
  ```java
  handlerExceptionResolver.resolveException(request, response, null, ex);
  ```

---

## Quick recall

**Q. What is the difference between controller-local `@ExceptionHandler` and `@RestControllerAdvice`?**
A. Controller-local handles only exceptions from that controller; `@RestControllerAdvice` is a global component applying to all controllers with implicit `@ResponseBody`.

**Q. Why does a generic `@ExceptionHandler(Exception.class)` fallback create bugs if not extending `ResponseEntityExceptionHandler`?**
A. It intercepts standard Spring MVC exceptions (such as `HttpRequestMethodNotSupportedException` and `HttpMessageNotReadableException`), returning 500 instead of proper 405/400 status codes and headers.

**Q. How does Spring resolve handler precedence between `UserNotFoundException` and `RuntimeException`?**
A. `ExceptionHandlerExceptionResolver` selects the handler with the shortest inheritance distance to the thrown exception; `UserNotFoundException` wins.

**Q. What causes `IllegalStateException: Ambiguous @ExceptionHandler method mapped`?**
A. Declaring multiple handler methods that match a given exception at the exact same inheritance distance in the same advice class.

**Q. Why doesn't `@RestControllerAdvice` catch authentication errors from Spring Security filters?**
A. Security filters execute before `DispatcherServlet`; exceptions in filters bypass MVC exception resolvers unless delegated via `HandlerExceptionResolver`.

**Q. How does RFC 9457 `ProblemDetail` differ from a bespoke `ApiError` record?**
A. `ProblemDetail` is an IETF standard with native Spring 6+ framework support (`type`, `title`, `status`, `detail`, `instance`, `properties`), whereas `ApiError` is a proprietary custom DTO.

**Q. What exception is thrown by `@Valid` on `@RequestBody` versus `@Validated` on method parameters?**
A. `@Valid` on request bodies throws `MethodArgumentNotValidException`; `@Validated` on method parameters throws `ConstraintViolationException`.

**Q. What happens if `@PreAuthorize` fails and advice has `@ExceptionHandler(Exception.class)` but no `AccessDeniedException` handler?**
A. The advice catches `AccessDeniedException` as a generic `Exception` and returns 500 Internal Server Error instead of 403 Forbidden.
