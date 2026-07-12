---
order: 20
search: false
---

# CompletableFuture Mixed Review Solutions

## Solution: classify-completablefuture-workflow - Classify CompletableFuture Workflow

Use this classification:

| Workflow piece | Method |
|---|---|
| Plain DTO conversion | `thenApply()` |
| User then orders for user | `thenCompose()` |
| User plus account summary | `thenCombine()` |
| Many independent service calls | `allOf()` |
| Optional dependency fallback | `exceptionally()` or `handle()` on that branch |
| Required dependency failure | no fallback unless business says so |
| Final retrieval | one boundary `join()` or framework return |

The method choice follows dependency, not personal style.

## Solution: spot-completablefuture-bugs - Spot CompletableFuture Bugs

- Immediate `join()` serializes work. Start independent futures first.
- `thenApply(asyncMethod)` creates nested futures. Use `thenCompose()`.
- Broad final `exceptionally()` can hide required failure. Attach fallback only to optional branches.
- Common pool for blocking calls risks shared-pool starvation. Pass a dedicated executor.
- Ignored returned future can hide failure. Return it, join it at a boundary, or attach logging/metrics.

## Solution: end-to-end-completablefuture-review - End-To-End CompletableFuture Review

```java
CompletableFuture<User> userFuture =
        fetchUser(executor).orTimeout(1, TimeUnit.SECONDS);

CompletableFuture<List<Order>> orderFuture =
        fetchOrders(executor).orTimeout(1500, TimeUnit.MILLISECONDS);

CompletableFuture<PartialResult<List<Recommendation>>> recommendationFuture =
        fetchRecommendations(executor)
                .completeOnTimeout(List.of(), 500, TimeUnit.MILLISECONDS)
                .handle((value, ex) -> ex == null
                        ? new PartialResult<>(value, null)
                        : new PartialResult<>(List.of(), "recommendations unavailable"));

CompletableFuture<PartialResult<List<Notification>>> notificationFuture =
        fetchNotifications(executor)
                .completeOnTimeout(List.of(), 300, TimeUnit.MILLISECONDS)
                .handle((value, ex) -> ex == null
                        ? new PartialResult<>(value, null)
                        : new PartialResult<>(List.of(), "notifications unavailable"));

CompletableFuture<Dashboard> dashboardFuture =
        CompletableFuture
                .allOf(userFuture, orderFuture, recommendationFuture, notificationFuture)
                .thenApply(ignored -> Dashboard.from(
                        userFuture.join(),
                        orderFuture.join(),
                        recommendationFuture.join(),
                        notificationFuture.join()))
                .orTimeout(2, TimeUnit.SECONDS);
```

The required branches have no fallback. Optional branches return explicit degraded values.
