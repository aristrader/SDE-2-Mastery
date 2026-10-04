---
order: 20
search: false
---

# Spring Exception Handling Solutions

## Solution: restcontrolleradvice-baseline - @RestControllerAdvice baseline
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntime(RuntimeException ex, HttpServletRequest req) {
        ApiError error = new ApiError(
            Instant.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            ex.getMessage(),
            req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        ApiError error = new ApiError(
            Instant.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        ApiError error = new ApiError(
            Instant.now(),
            HttpStatus.FORBIDDEN.value(),
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            ex.getMessage(),
            req.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}
```

## Solution: structured-error-response - Structured error response
```java
// Reusable helper method inside GlobalExceptionHandler
private ResponseEntity<ApiError> buildError(HttpStatus status, String message, HttpServletRequest request) {
    ApiError error = new ApiError(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI()
    );
    return ResponseEntity.status(status).body(error);
}

// Refactored handlers using the buildError helper:
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<ApiError> handleRuntime(RuntimeException ex, HttpServletRequest req) {
    return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req);
}

@ExceptionHandler(EntityNotFoundException.class)
public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex, HttpServletRequest req) {
    return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), req);
}

@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
    return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), req);
}
```

## Solution: validation-errors-valid - Validation errors from @Valid
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
        .collect(Collectors.joining(", "));
    return buildError(HttpStatus.BAD_REQUEST, message, req);
}
```

## Solution: constraintviolationexception-validated - ConstraintViolationException from @Validated service methods
```java
@ExceptionHandler(ConstraintViolationException.class)
public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
    String message = ex.getConstraintViolations().stream()
        .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
        .collect(Collectors.joining(", "));
    return buildError(HttpStatus.BAD_REQUEST, message, req);
}
```

## Solution: feign-exception-mapping - Feign exception mapping
```java
@ExceptionHandler(FeignException.NotFound.class)
public ResponseEntity<ApiError> handleFeignNotFound(FeignException.NotFound ex, HttpServletRequest req) {
    return buildError(HttpStatus.NOT_FOUND, "Upstream Not Found: " + ex.contentUTF8(), req);
}

@ExceptionHandler(FeignException.ServiceUnavailable.class)
public ResponseEntity<ApiError> handleFeignUnavailable(FeignException.ServiceUnavailable ex, HttpServletRequest req) {
    ResponseEntity<ApiError> error = buildError(HttpStatus.SERVICE_UNAVAILABLE, "Upstream Unavailable", req);
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .header(HttpHeaders.RETRY_AFTER, "30")
        .body(error.getBody());
}
```

## Solution: custom-business-exception - Custom business exception with error code
```java
record KycApiError(
    Instant timestamp,
    int status,
    String error,
    KycErrorCode errorCode,
    String message,
    String path
) {}

@ExceptionHandler(KycVerificationException.class)
public ResponseEntity<KycApiError> handleKyc(KycVerificationException ex, HttpServletRequest req) {
    KycApiError body = new KycApiError(
        Instant.now(),
        HttpStatus.UNPROCESSABLE_ENTITY.value(),
        HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
        ex.getErrorCode(),
        ex.getMessage(),
        req.getRequestURI()
    );
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
}
```
