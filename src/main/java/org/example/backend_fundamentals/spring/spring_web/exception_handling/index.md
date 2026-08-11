---
order: 40
---

# Spring Exception Handling

Study-plan priority — 🔴 💼 | MP | 1.5 hrs

REST APIs should not leak raw stack traces or random Spring error bodies. The usual interview answer is: use `@RestControllerAdvice` with specific `@ExceptionHandler` methods and return one consistent error shape.

---

## Controller-local handler

`@ExceptionHandler` inside a controller handles exceptions from that controller only.

```java
@RestController
@RequestMapping("/users")
public class UserController {

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

Useful for very local behavior. For APIs, global handling is usually better.

---

## Global handler

Use `@RestControllerAdvice` for REST APIs.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> notFound(UserNotFoundException ex,
                                             HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError.of(404, ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> fallback(Exception ex,
                                             HttpServletRequest request) {
        return ResponseEntity.internalServerError()
            .body(ApiError.of(500, "Internal error", request.getRequestURI()));
    }
}
```

`@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`.

Use specific handlers before the generic fallback:

- `UserNotFoundException` → `404`
- duplicate/state conflict → `409`
- validation failure → `400` or `422`
- unknown exception → `500`

---

## Error response shape

Keep one consistent response shape.

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
            List.of());
    }

    public record FieldError(String field, String message) {
    }
}
```

Interview point: clients should not need to parse different error formats for different failures.

---

## Validation errors

`@Valid` on `@RequestBody` failures usually throw `MethodArgumentNotValidException`.

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

Related failures worth knowing:

- malformed JSON → `HttpMessageNotReadableException`
- missing required query parameter → `MissingServletRequestParameterException`
- method/path/query validation with `@Validated` → `ConstraintViolationException`

---

## Handler selection

Spring chooses the most specific matching handler.

```java
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ApiError> notFound(UserNotFoundException ex) { ... }

@ExceptionHandler(RuntimeException.class)
public ResponseEntity<ApiError> runtime(RuntimeException ex) { ... }
```

If `UserNotFoundException extends RuntimeException`, the `UserNotFoundException` handler wins.

Spring checks:

1. handler methods on the controller itself
2. global `@ControllerAdvice` / `@RestControllerAdvice`
3. Spring Boot default `/error` handling

---

## @ResponseStatus

You can put status directly on an exception:

```java
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found: " + id);
    }
}
```

This is simple, but gives less control over the response body. For structured REST errors, prefer `@ExceptionHandler`.

You may also throw:

```java
throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
```

Useful for simple cases, less ideal for large APIs where you want consistent error bodies.

---

## Security exceptions

`@ControllerAdvice` does not catch Spring Security authentication/authorization failures.

Reason: Spring Security filters run before `DispatcherServlet`.

| Failure | HTTP status | Handle with |
|---|---|---|
| not logged in | `401 Unauthorized` | `AuthenticationEntryPoint` |
| logged in but forbidden | `403 Forbidden` | `AccessDeniedHandler` |

MVC exception handlers handle controller exceptions. Security exceptions happen earlier in the filter chain, so configure them in Spring Security.

---

## Quick recall

**Q. `@ExceptionHandler` inside controller vs `@RestControllerAdvice`?**  
A. Controller handler is local. `@RestControllerAdvice` is global for REST APIs.

**Q. Why use one `ApiError` shape?**  
A. Clients get predictable errors for validation, not found, conflict, and server failures.

**Q. Which handler wins: specific exception or `RuntimeException`?**  
A. The most specific matching exception handler wins.

**Q. Why does advice not catch 401/403 from Spring Security?**  
A. Security filters run before Spring MVC. Use `AuthenticationEntryPoint` and `AccessDeniedHandler`.

**Q. `@ResponseStatus` vs `@ExceptionHandler`?**  
A. `@ResponseStatus` is simple status mapping. `@ExceptionHandler` gives full control over body, headers, and status.

**Q. What exception does `@Valid @RequestBody` commonly throw?**  
A. `MethodArgumentNotValidException`.
