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
}
```

## Solution: structured-error-response - Structured error response
```java
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
    return ResponseEntity.status(503).header(HttpHeaders.RETRY_AFTER, "30").body(error.getBody());
}
```

## Solution: custom-business-exception - Custom business exception with error code
```java
@ExceptionHandler(KycVerificationException.class)
public ResponseEntity<Map<String, Object>> handleKyc(KycVerificationException ex, HttpServletRequest req) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", Instant.now());
    body.put("status", 422);
    body.put("errorCode", ex.getErrorCode().name());
    body.put("message", ex.getMessage());
    body.put("path", req.getRequestURI());
    return ResponseEntity.status(422).body(body);
}
```
