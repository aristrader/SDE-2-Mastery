---
order: 30
---

# CompletableFuture Failure And Timeouts

Async failure handling is mostly about business semantics: required failures should fail the workflow; optional failures may degrade explicitly.

## Failure Propagation

If a stage fails, later normal stages are skipped until recovery is attached.

```java
fetchUserAsync()
        .thenApply(this::toDto)
        .thenApply(this::format);
```

If `fetchUserAsync()` fails, neither normal transform runs.

## exceptionally, handle, whenComplete

| Method | Runs on | Purpose |
|---|---|---|
| `exceptionally()` | Failure only | Return fallback |
| `handle()` | Success or failure | Convert either outcome into a new value |
| `whenComplete()` | Success or failure | Observe/log/metrics without intentionally changing outcome |

Attach fallback where the dependency is optional:

```java
CompletableFuture<List<Recommendation>> recommendations =
        fetchRecommendationsAsync()
                .exceptionally(ex -> List.of());
```

Avoid one final fallback that hides required failures:

```java
CompletableFuture.allOf(userFuture, recommendationFuture)
        .exceptionally(ex -> null); // too broad
```

## Timeouts

`orTimeout()` turns slowness into failure:

```java
fetchUserAsync().orTimeout(1, TimeUnit.SECONDS);
```

`completeOnTimeout()` turns slowness into a fallback value:

```java
fetchRecommendationsAsync()
        .completeOnTimeout(List.of(), 300, TimeUnit.MILLISECONDS);
```

Timeout completion does not guarantee the underlying HTTP, JDBC, or blocking operation stopped. Design cancellation separately.

## Retry

Retries are not just syntax. For backend systems, retry only when the operation is safe to repeat or protected by idempotency keys.

Payment retries without idempotency can double-charge.

## Quick recall

**Q. Required user fetch failed. Should you return a fake user?**
A. Usually no. Preserve failure unless the business explicitly allows fallback.

**Q. Optional recommendation fetch failed. Where attach fallback?**
A. On the recommendation future itself.

**Q. `handle()` vs `whenComplete()`?**
A. `handle()` transforms the outcome; `whenComplete()` observes it.

**Q. Why not return `null` from `exceptionally()`?**
A. It hides the real failure and often causes later `NullPointerException`.

**Q. Why must timeout budgets fit the SLA?**
A. Three sequential two-second timeouts can make a request take six seconds.
