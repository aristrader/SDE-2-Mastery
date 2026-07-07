---
order: 10
search: false
---

# Spring Exception Handling Practice

## Domain model

```java
record ApiError(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path
) {}

class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) { super(message); }
}

enum KycErrorCode { DOCUMENT_EXPIRED, COUNTRY_NOT_SUPPORTED, IDENTITY_MISMATCH }

class KycVerificationException extends RuntimeException {
    private final KycErrorCode errorCode;
    public KycVerificationException(KycErrorCode code, String message) {
        super(message);
        this.errorCode = code;
    }
    public KycErrorCode getErrorCode() { return errorCode; }
}
```

## Exercise: restcontrolleradvice-baseline - @RestControllerAdvice baseline

### Goal
Stand up the advice class with three foundational handlers.

### Task
Create `GlobalExceptionHandler` annotated with `@RestControllerAdvice`. Add `@ExceptionHandler` methods for:
- `RuntimeException` → 500 Internal Server Error
- `EntityNotFoundException` → 404 Not Found
- `AccessDeniedException` → 403 Forbidden

Each handler must return `ResponseEntity<ApiError>`. Populate all five fields of `ApiError`; for `path`, inject `HttpServletRequest` and call `request.getRequestURI()`.

### Gotcha
Handler resolution picks the most specific exception type.

## Exercise: structured-error-response - Structured error response

### Goal
Ensure every handler returns `ApiError` — no raw strings, no Spring default error body.

### Task
Revisit the three handlers from Exercise 1 and verify:
- The `error` field carries the HTTP reason phrase (e.g., `"Not Found"`).
- The `message` field carries `exception.getMessage()`.
- `timestamp` is `Instant.now()`.

Write a private helper `buildError(HttpStatus status, String message, HttpServletRequest request)` that constructs the `ApiError` and returns a `ResponseEntity<ApiError>`. Refactor all three handlers to use it.

### Gotcha
`HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()` gives you `"Internal Server Error"` — no magic strings.

## Exercise: validation-errors-valid - Validation errors from @Valid

### Goal
Surface every field-level constraint violation instead of a single vague 400.

### Task
Add a handler for `MethodArgumentNotValidException`. Extract field errors from `ex.getBindingResult().getFieldErrors()` and build a response that includes each field name and its default message.
Return HTTP 400. Either add `List<String> fieldErrors` to `ApiError` (or a separate `ValidationApiError` record), or concatenate all violations into `message`; do not silently drop individual field errors.

### Gotcha
`MethodArgumentNotValidException` extends `BindException` → `Exception` — not a `RuntimeException`.

## Exercise: constraintviolationexception-validated - ConstraintViolationException from @Validated service methods

### Goal
Handle the second kind of validation exception — the one that fires on service-layer method parameters.

### Task
Add a handler for `jakarta.validation.ConstraintViolationException`. Extract violations from `ex.getConstraintViolations()`, collect each `getPropertyPath()` + `getMessage()`, return HTTP 400.

### Gotcha
Both `MethodArgumentNotValidException` and `ConstraintViolationException` can be active in the same app. You need a handler for each. `MethodArgumentNotValidException` exposes `getBindingResult().getFieldErrors()`; `ConstraintViolationException` exposes `getConstraintViolations()`.

## Exercise: feign-exception-mapping - Feign exception mapping

### Goal
Translate upstream HTTP failures into your API's error vocabulary.

### Task
Add handlers for `FeignException.NotFound` and `FeignException.ServiceUnavailable`:
- `FeignException.NotFound` → 404; extract the upstream body via `ex.contentUTF8()` and include it in `message`.
- `FeignException.ServiceUnavailable` → 503; set a `Retry-After: 30` header to hint the caller should back off.

### Gotcha
`FeignException.NotFound` extends `FeignException` (a `RuntimeException`). Forget the specific handler and the `RuntimeException` catch-all silently swallows the 404 as a 500.

## Exercise: custom-business-exception - Custom business exception with error code

### Goal
Give API clients a machine-readable code they can switch on, not just an HTTP status.

### Task
Add a handler for `KycVerificationException`. Map it to HTTP 422 Unprocessable Entity. The response body must include the `errorCode` enum value so clients can switch on it.

### Gotcha
422 isn't a standard `HttpStatus` in older Spring versions — confirm `HttpStatus.UNPROCESSABLE_ENTITY` exists; otherwise use `ResponseEntity.status(422)` directly.
