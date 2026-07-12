---
order: 20
search: false
---

# CompletableFuture Failure And Timeout Solutions

## Solution: failure-handling - Failure Handling

```java
CompletableFuture<User> userFuture =
        fetchUserAsync().whenComplete((user, ex) -> {
            if (ex != null) {
                log.warn("user fetch failed", ex);
            }
        });

CompletableFuture<List<Recommendation>> recommendations =
        fetchRecommendationsAsync().exceptionally(ex -> List.of());

CompletableFuture<PartialResult<List<Recommendation>>> withWarning =
        fetchRecommendationsAsync().handle((value, ex) -> {
            if (ex != null) {
                return new PartialResult<>(List.of(), "recommendations unavailable");
            }
            return new PartialResult<>(value, null);
        });
```

`whenComplete()` observes. `exceptionally()` recovers failure only. `handle()` maps either outcome into a new value.

## Solution: required-optional-dashboard - Required And Optional Dashboard

```java
CompletableFuture<User> userFuture = fetchUserAsync();
CompletableFuture<List<Order>> orderFuture = fetchOrdersAsync();
CompletableFuture<List<Recommendation>> recommendationFuture =
        fetchRecommendationsAsync().exceptionally(ex -> List.of());

CompletableFuture<Dashboard> dashboardFuture =
        CompletableFuture
                .allOf(userFuture, orderFuture, recommendationFuture)
                .thenApply(ignored -> new Dashboard(
                        userFuture.join(),
                        orderFuture.join(),
                        recommendationFuture.join()
                ));
```

The optional branch recovers before `allOf()`. Required branches are still allowed to fail.

## Solution: timeout-budget - Timeout Budget

```java
CompletableFuture<User> userFuture =
        fetchUserAsync().orTimeout(1, TimeUnit.SECONDS);

CompletableFuture<List<Recommendation>> recommendationFuture =
        fetchRecommendationsAsync()
                .completeOnTimeout(List.of(), 300, TimeUnit.MILLISECONDS);

CompletableFuture<Dashboard> bounded =
        dashboardFuture.orTimeout(2, TimeUnit.SECONDS);
```

Per-service timeouts define dependency behavior; the final timeout enforces total request budget.

## Solution: retry-once - Retry Once

```java
static <T> CompletableFuture<T> retryOnce(
        Supplier<CompletableFuture<T>> operation
) {
    return operation.get()
            .handle((value, ex) -> ex == null
                    ? CompletableFuture.completedFuture(value)
                    : operation.get())
            .thenCompose(Function.identity());
}
```

This is only a composition drill. In production Java, do not hand-roll broad CompletableFuture retry loops as the default. Prefer a resilience library such as Resilience4j for retry policies, backoff, jitter, circuit breaking, metrics, and retry budgets. Still design idempotency for non-idempotent operations such as payments.

## Solution: partial-result-warnings - Partial Result Warnings

```java
record PartialResult<T>(T value, String warning) {
}

CompletableFuture<PartialResult<List<Recommendation>>> recommendations =
        fetchRecommendationsAsync().handle((value, ex) -> {
            if (ex != null) {
                return new PartialResult<>(
                        List.of(),
                        "recommendations unavailable"
                );
            }
            return new PartialResult<>(value, null);
        });
```

This makes degraded output explicit instead of making failure indistinguishable from a genuine empty result.
