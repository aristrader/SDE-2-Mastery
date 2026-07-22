---
order: 30
---

# CompletableFuture Failure And Timeouts

Async failure handling is mostly about business semantics.

First classify the dependency:

```text
Required dependency failed -> fail the workflow
Optional dependency failed -> degrade explicitly
Slow dependency -> timeout according to the request budget
Retried dependency -> safe only if the operation is idempotent
```

## Failure propagation

If a stage fails, later normal stages are skipped until a recovery stage handles the failure.

```java
CompletableFuture<String> future =
        fetchUserAsync()
                .thenApply(this::toDto)
                .thenApply(this::format);
```

If `fetchUserAsync()` fails, neither `toDto` nor `format` runs. The returned future completes exceptionally.

This is good for required work. Required failure should remain visible.

## `exceptionally`

`exceptionally()` runs only on failure and returns a fallback value.

```java
CompletableFuture<List<Recommendation>> recommendations =
        fetchRecommendationsAsync(userId)
                .exceptionally(ex -> List.of());
```

This is correct if recommendations are optional.

Bad use:

```java
CompletableFuture<User> user =
        fetchRequiredUserAsync(userId)
                .exceptionally(ex -> fakeUser());
```

If the user is required, a fake fallback hides the real outage and may corrupt downstream behavior.

## `handle`

`handle()` runs on success or failure and converts either outcome into a new value.

```java
CompletableFuture<Response> response =
        fetchUserAsync(userId)
                .handle((user, ex) -> {
                    if (ex != null) {
                        return Response.error("user unavailable");
                    }
                    return Response.ok(user);
                });
```

Use it when the output type should explicitly represent success or failure.

## `whenComplete`

`whenComplete()` observes the outcome but does not intentionally recover.

```java
CompletableFuture<User> user =
        fetchUserAsync(userId)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("user fetch failed", ex);
                    }
                });
```

If the previous stage failed, the returned future still fails. Think of `whenComplete()` as async `finally` for logging, metrics, cleanup, or tracing.

## Method comparison

| Method | Runs when | Can recover? | Typical use |
|---|---|---:|---|
| `exceptionally(fn)` | Failure only | Yes | Optional dependency fallback |
| `handle(fn)` | Success or failure | Yes | Convert outcome to response/result object |
| `whenComplete(fn)` | Success or failure | No, side effect only | Log, metric, cleanup |

## Timeout methods

`orTimeout()` turns slowness into failure:

```java
CompletableFuture<User> user =
        fetchUserAsync(userId)
                .orTimeout(1, TimeUnit.SECONDS);
```

`completeOnTimeout()` turns slowness into a fallback value:

```java
CompletableFuture<List<Recommendation>> recommendations =
        fetchRecommendationsAsync(userId)
                .completeOnTimeout(List.of(), 300, TimeUnit.MILLISECONDS);
```

Use `orTimeout()` for required dependencies. Use `completeOnTimeout()` for optional dependencies where fallback is valid.

Important: completing the `CompletableFuture` on timeout does not guarantee the underlying HTTP, JDBC, or blocking call stopped. Configure timeouts on the underlying client too.

## Timeout budgets

Timeouts must fit the caller's SLA.

Bad:

```text
HTTP request SLA = 2 seconds
user timeout = 2 seconds
orders timeout = 2 seconds
recommendations timeout = 2 seconds
sequential chain can now take 6 seconds
```

Better:

- start independent calls in parallel
- give required calls a budget inside the total SLA
- give optional calls shorter budgets
- fail or degrade deliberately

## Retry

Retries are not just syntax. They are a correctness decision.

Retry only when:

- the failure is likely transient
- the operation is safe to repeat
- there is a retry limit
- there is backoff/jitter
- the total time still fits the SLA

Payment example: retrying without an idempotency key can double-charge. The async API does not solve that business problem.

## Common traps

**One broad fallback at the end**

```java
dashboardFuture.exceptionally(ex -> emptyDashboard());
```

This hides whether the required user call failed or only optional recommendations failed. Attach fallback at the optional branch instead.

**Returning `null` from recovery**

```java
.exceptionally(ex -> null)
```

This usually converts a clear async failure into a later `NullPointerException`.

**Logging with `exceptionally()` then reusing fallback accidentally**

Use `whenComplete()` for logging if you do not intend to recover.

## Quick recall

**Q. Required user fetch failed. Should you return a fake user?**
A. Usually no. Required dependency failure should fail the workflow.

**Q. Optional recommendation fetch failed. Where attach fallback?**
A. On the recommendation future itself, not as one broad final fallback.

**Q. `exceptionally()` vs `handle()`?**
A. `exceptionally()` runs only on failure and returns fallback. `handle()` runs on success or failure and maps either to a new result.

**Q. `handle()` vs `whenComplete()`?**
A. `handle()` changes the outcome. `whenComplete()` observes/logs and passes the original outcome through.

**Q. Does `orTimeout()` stop the underlying HTTP call?**
A. Not necessarily. Configure the underlying client timeout too.

**Q. Why must retries be idempotent?**
A. Repeating a side effect like payment or order creation can duplicate it unless protected by idempotency.

