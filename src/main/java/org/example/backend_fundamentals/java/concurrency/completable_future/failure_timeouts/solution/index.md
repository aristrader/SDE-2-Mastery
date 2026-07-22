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

Use the methods by intent:

| Method | Runs on success? | Runs on failure? | Changes result? | Best use |
|---|---:|---:|---:|---|
| `whenComplete()` | yes | yes | no | logging, metrics, cleanup |
| `exceptionally()` | no | yes | yes | simple fallback |
| `handle()` | yes | yes | yes | convert either outcome into a new value |

The required user future uses `whenComplete()` because logging should not turn a required failure into success. If `fetchUserAsync()` fails, `userFuture` still fails after the log line.

The optional recommendations future can use `exceptionally()` when the fallback is simple: on failure, return an empty list. That is acceptable only if the business rule says recommendations are optional.

`handle()` is better when you need a value plus metadata. It can turn failure into:

```java
new PartialResult<>(List.of(), "recommendations unavailable")
```

That avoids silently pretending the dependency returned a real empty result.

Common trap: adding one final `exceptionally()` to the whole dashboard. That can accidentally hide required user/order failures. Recover optional branches locally instead.

## Solution: required-optional-dashboard - Required And Optional Dashboard

```java
CompletableFuture<User> userFuture = fetchUserAsync();
CompletableFuture<List<Order>> orderFuture = fetchOrdersAsync();
CompletableFuture<PartialResult<List<Recommendation>>> recommendationFuture =
        fetchRecommendationsAsync().handle((value, ex) -> {
            if (ex != null) {
                return new PartialResult<>(
                        List.of(),
                        "recommendations unavailable"
                );
            }
            return new PartialResult<>(value, null);
        });

CompletableFuture<Dashboard> dashboardFuture =
        CompletableFuture
                .allOf(userFuture, orderFuture, recommendationFuture)
                .thenApply(ignored -> {
                    User user = userFuture.join();
                    List<Order> orders = orderFuture.join();
                    PartialResult<List<Recommendation>> recommendations =
                            recommendationFuture.join();

                    return new Dashboard(
                            user,
                            orders,
                            recommendations.value(),
                            recommendations.warning() == null
                                    ? List.of()
                                    : List.of(recommendations.warning())
                    );
                });
```

The required branches have no fallback. If `fetchUserAsync()` or `fetchOrdersAsync()` fails, `allOf()` fails and the dashboard future fails. That is correct because a dashboard without the user or orders would be misleading.

The optional recommendations branch converts its own failure into a degraded value before `allOf()`. That lets the dashboard complete while still carrying a warning. This is better than returning only `List.of()` because an actual empty recommendation list and a failed recommendation service are different situations.

Common traps:

- Putting `exceptionally()` after `allOf()`, which can swallow required failures.
- Treating every dependency as optional because it makes the code "more resilient." Resilience means preserving the business contract, not hiding bad data.
- Forgetting warnings, which makes degraded responses invisible to callers and logs.

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

Use per-service timeouts to define each dependency's behavior, and one overall timeout to protect the request SLA.

For required calls, `orTimeout()` is usually right:

```java
fetchUserAsync().orTimeout(1, TimeUnit.SECONDS)
```

If the user service does not answer in time, the user future fails with a timeout. Since user is required, the dashboard should fail.

For optional calls, `completeOnTimeout()` is often right:

```java
fetchRecommendationsAsync()
        .completeOnTimeout(List.of(), 300, TimeUnit.MILLISECONDS)
```

If recommendations are slow, the branch completes with a fallback value. In a real response, pair this with a warning so callers know the result is degraded.

The final timeout protects the total budget:

```java
dashboardFuture.orTimeout(2, TimeUnit.SECONDS)
```

Three sequential two-second dependency timeouts can violate a two-second request SLA because the worst case becomes roughly six seconds. Start independent work together, set smaller per-service budgets, and enforce one overall budget.

Important trap: a CompletableFuture timeout does not guarantee the underlying I/O stopped. It completes the future with timeout behavior, but the HTTP call, JDBC query, or SDK operation may continue unless that client supports cancellation/timeouts too. Configure timeouts at the client layer as well.

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

The first `operation.get()` starts the first attempt. `handle()` receives either the successful value or the failure:

- On success, it returns `CompletableFuture.completedFuture(value)`.
- On failure, it calls `operation.get()` again to start exactly one retry.

Because `handle()` returns a future in both branches, the intermediate type is:

```java
CompletableFuture<CompletableFuture<T>>
```

`thenCompose(Function.identity())` flattens that into `CompletableFuture<T>`.

The second failure is not recovered. If the retry fails, the returned future fails. That matches the exercise requirement: retry once, then propagate.

Common traps:

- Calling `join()` before retrying. That blocks and turns async composition into synchronous waiting.
- Retrying every failure. Some failures are permanent, such as validation errors or authorization failures.
- Retrying non-idempotent operations, such as payments, without an idempotency key.
- Retrying without backoff, jitter, metrics, and a total retry budget.

For production Java, prefer a resilience library such as Resilience4j for broad retry policy. This example is mainly to learn CompletableFuture composition.

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

`PartialResult<T>` separates the value from the health of the dependency. That matters because these two states are not the same:

```text
recommendation service succeeded and returned no recommendations
recommendation service failed, so the dashboard used an empty fallback
```

Both may produce `List.of()`, but only the second one should carry a warning.

`handle()` is the right method because it sees both success and failure. On success, it keeps the real value and uses `null` warning. On failure, it returns a fallback value plus an explicit warning.

Required futures should not be converted into `PartialResult` just to make the dashboard complete. If user or orders are required, let those futures fail.

Common trap: swallowing the exception and losing all observability. In production, also log, emit a metric, or include structured warning metadata so degraded behavior can be monitored.
