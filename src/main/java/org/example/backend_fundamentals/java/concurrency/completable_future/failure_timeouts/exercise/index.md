---
order: 10
search: false
---

# CompletableFuture Failure And Timeout Practice

## Exercise: failure-handling - Failure Handling

### Goal
Choose `exceptionally()`, `handle()`, and `whenComplete()` correctly.

### Task
Build one required user future and one optional recommendations future. Log the required failure without changing it; recover the optional failure to an empty list; build an explicit partial result with warnings.

### Checks
- Use `whenComplete()` for observability.
- Use `exceptionally()` for simple optional fallback.
- Use `handle()` when converting failure into a value plus warning.

## Exercise: required-optional-dashboard - Required And Optional Dashboard

### Goal
Preserve required failures while degrading optional branches.

### Task
Build a dashboard where user and orders are required, recommendations are optional.

### Checks
- User/order failure fails the dashboard.
- Recommendation failure becomes an empty list plus optional warning.

## Exercise: timeout-budget - Timeout Budget

### Goal
Distinguish per-service timeout from overall timeout.

### Task
Apply `orTimeout()` to required calls, `completeOnTimeout()` to optional calls, and one final `orTimeout()` for the dashboard.

### Checks
- Explain why timeout does not guarantee underlying I/O cancellation.
- Explain why three sequential two-second timeouts violate a two-second request SLA.

## Exercise: retry-once - Retry Once

### Goal
Compose a retry without blocking.

### Task
Write `<T> CompletableFuture<T> retryOnce(Supplier<CompletableFuture<T>> operation)`.

### Checks
- Retry only after first failure.
- Propagate the second failure.
- Explain why real retries need idempotency, backoff, and budgets.

## Exercise: partial-result-warnings - Partial Result Warnings

### Goal
Avoid silently swallowing optional dependency failure.

### Task
Create `PartialResult<T>(T value, String warning)` and use `handle()` to convert optional service failure into fallback plus warning.

### Checks
- Required futures still fail.
- Optional futures produce explicit degraded metadata.
