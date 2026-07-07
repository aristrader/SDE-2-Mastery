---
order: 10
search: false
---

# Spring Exception Handling Practice

## Why this matters
A KYC platform calls external document and identity APIs, runs validation pipelines, and enforces access control — all of which fail in different ways. A global exception handler is the single place that translates those failures into structured, machine-readable responses. Getting it wrong means clients see raw Spring error pages, swallowed upstream errors, or inconsistent status codes that break retries and alerting.

## Domain model

```java
// Use these types across the exercises
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

---

## Exercise 1: @RestControllerAdvice baseline (~10 min)

**Goal:** Stand up the advice class with three foundational handlers.

**`@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`.** `@ControllerAdvice` alone routes the return value through view resolution — useful for HTML MVC apps. `@RestControllerAdvice` always serialises to JSON/XML, which is what you want for a REST API. Never use `@ControllerAdvice` alone on a REST service.

**Task:** Create `GlobalExceptionHandler` annotated with `@RestControllerAdvice`. Add `@ExceptionHandler` methods for:
- `RuntimeException` → 500 Internal Server Error
- `EntityNotFoundException` → 404 Not Found
- `AccessDeniedException` → 403 Forbidden

Each handler must return `ResponseEntity<ApiError>`. Populate all five fields of `ApiError`; for `path`, inject `HttpServletRequest` and call `request.getRequestURI()`.

**Gotcha:** Handler resolution picks the most specific exception type. If `EntityNotFoundException` extends `RuntimeException`, the `EntityNotFoundException` handler wins — Spring does not call both. Method declaration order does not matter; order them most-specific-first for readability.

---

## Exercise 2: Structured error response (~5 min)

**Goal:** Ensure every handler returns `ApiError` — no raw strings, no Spring default error body.

**Task:** A design exercise, not a new class. Revisit the three handlers from Exercise 1 and verify:
- The `error` field carries the HTTP reason phrase (`"Not Found"`, `"Forbidden"`, `"Internal Server Error"`) — not the exception class name.
- The `message` field carries `exception.getMessage()`.
- `timestamp` is `Instant.now()` at the moment the handler runs, not a fixed value.

Write a private helper `buildError(HttpStatus status, String message, HttpServletRequest request)` that constructs the `ApiError` and returns a `ResponseEntity<ApiError>`. Refactor all three handlers to use it.

**Gotcha:** `HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()` gives you `"Internal Server Error"` — no magic strings.

---

## Exercise 3: Validation errors from @Valid (~15 min)

**Goal:** Surface every field-level constraint violation instead of a single vague 400.

**Task:** Add a handler for `MethodArgumentNotValidException`. Extract field errors from `ex.getBindingResult().getFieldErrors()` and build a response that includes each field name and its default message. Two design options — pick one and justify it:
- Option A: Add `List<String> fieldErrors` to `ApiError` (or a separate `ValidationApiError` record).
- Option B: Concatenate all violations into `message` as a single string.

Return HTTP 400. Do not let individual field errors silently drop.

**Gotcha:** `MethodArgumentNotValidException` extends `BindException` → `Exception` — not a `RuntimeException`. A catch-all that only catches `RuntimeException` lets this fall through to Spring's default error handling, returning a generic 400 body instead of your structured `ApiError`.

---

## Exercise 3b: ConstraintViolationException from @Validated service methods (~10 min)

**Goal:** Handle the second kind of validation exception — the one that fires on service-layer method parameters, not controller request bodies.

**Task:** Add a handler for `jakarta.validation.ConstraintViolationException`. Extract violations from `ex.getConstraintViolations()`, collect each `getPropertyPath()` + `getMessage()`, return HTTP 400.

**`MethodArgumentNotValidException` vs `ConstraintViolationException` — the key distinction:**

| | `MethodArgumentNotValidException` | `ConstraintViolationException` |
|---|---|---|
| Trigger | `@Valid` on a controller method parameter | `@Validated` on a Spring bean (service, repository) method parameter or return value |
| Parent type | `BindException` → `Exception` (not RuntimeException) | `RuntimeException` |
| Error access | `ex.getBindingResult().getFieldErrors()` | `ex.getConstraintViolations()` |
| HTTP status | 400 | 400 (or 422 for business-rule violations) |

Both can be active in the same app. You need a handler for each.

**`ResponseEntityExceptionHandler` as a base class:** Spring provides this as an optional base for your advice class. It pre-handles standard Spring MVC exceptions (`MethodArgumentNotValidException`, `HttpMessageNotReadableException`, etc.) with overridable defaults. If you extend it, override the protected method or call `super.handleMethodArgumentNotValid(...)` — adding a duplicate `@ExceptionHandler` for the same type causes an ambiguous handler error at startup.

---

## Exercise 4: Feign exception mapping (~10 min)

**Goal:** Translate upstream HTTP failures into your API's error vocabulary.

**Task:** Add handlers for `FeignException.NotFound` and `FeignException.ServiceUnavailable`:
- `FeignException.NotFound` → 404; extract the upstream body via `ex.contentUTF8()` and include it in `message`.
- `FeignException.ServiceUnavailable` → 503; set a `Retry-After: 30` header to hint the caller should back off.

Use `ResponseEntity` headers to set `Retry-After`.

**Gotcha:** `FeignException.NotFound` extends `FeignException` (a `RuntimeException`). Spring resolves to the most-specific type, so declaration order doesn't matter. But forget the specific handler and the `RuntimeException` catch-all silently swallows the 404 as a 500 — the upstream failure becomes invisible to your client.

---

## Exercise 5: Custom business exception with error code (~5 min)

**Goal:** Give API clients a machine-readable code they can switch on, not just an HTTP status.

**Task:** Add a handler for `KycVerificationException`. Map it to HTTP 422 Unprocessable Entity. The response body must include the `errorCode` enum value (as a string) so clients can switch `DOCUMENT_EXPIRED` against `COUNTRY_NOT_SUPPORTED`. Extend `ApiError` or use a subtype — your call.

**Gotcha:** 422 isn't a standard `HttpStatus` in older Spring versions — confirm `HttpStatus.UNPROCESSABLE_ENTITY` exists; otherwise use `ResponseEntity.status(422)` directly.

---

## Quick recall

**Q.** What is the difference between `@RestControllerAdvice` and `@ControllerAdvice`?
**A.** `@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`. It serialises the return value to JSON/XML. `@ControllerAdvice` alone routes through view resolution — only useful for HTML MVC apps.

**Q.** `@Valid` on a controller param throws X; `@Validated` on a service method throws Y. What are X and Y?
**A.** X = `MethodArgumentNotValidException` (a `BindException`). Y = `ConstraintViolationException` (a `RuntimeException`). They have different parent types and different APIs for extracting violations.

**Q.** Why doesn't `@ControllerAdvice` catch 401/403 errors from Spring Security?
**A.** Spring Security filters run before `DispatcherServlet`. By the time an auth failure is raised, the advice is not yet in the call stack. Use `AuthenticationEntryPoint` and `AccessDeniedHandler` for those.

**Q.** You extend `ResponseEntityExceptionHandler`. Where should you handle `MethodArgumentNotValidException`?
**A.** Override its existing `handleMethodArgumentNotValid` method — do not add a new `@ExceptionHandler` for it. Spring's base class already handles it; a duplicate `@ExceptionHandler` causes an ambiguous handler exception at startup.

**Q.** Two handlers exist: one for `RuntimeException`, one for `EntityNotFoundException extends RuntimeException`. Which runs when an `EntityNotFoundException` is thrown?
**A.** The `EntityNotFoundException` handler — Spring always picks the most specific matching type, regardless of method declaration order.

**Q.** How do you attach a `Retry-After` header to a `ResponseEntity` returned from an exception handler?
**A.** Build the response with `ResponseEntity.status(503).header(HttpHeaders.RETRY_AFTER, "30").body(apiError)`.

**Q.** Why return an error code enum in a 422 response instead of just the message string?
**A.** Clients can programmatically switch on a stable code (`DOCUMENT_EXPIRED`) without parsing human-readable messages, which can change across versions or locales.
