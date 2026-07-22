---
order: 20
search: false
---

# CompletableFuture Mixed Review Solutions

## Solution: classify-completablefuture-workflow - Classify CompletableFuture Workflow

Use this classification:

| Workflow piece | Classification | Method |
|---|---|---|
| Convert `User` to `UserDto` | synchronous transform | `thenApply()` |
| Fetch user, then fetch orders for that user | dependent async call | `thenCompose()` |
| Fetch user and account summary independently | independent async calls | start both, then `thenCombine()` |
| Fetch many independent widgets | independent fan-out/fan-in | `allOf()` |
| Recommendations are nice-to-have | optional dependency | branch-local `exceptionally()` or `handle()` |
| User and orders are mandatory | required dependency | no fallback unless business explicitly allows it |
| Turn final future into response value | boundary blocking point | one `join()` at the caller boundary, or return the future |

The method choice follows dependency, not personal style.

Use this interview rule:

```text
plain value returned       -> thenApply()
future returned            -> thenCompose()
two independent futures    -> thenCombine()
many independent futures   -> allOf()
optional failure allowed   -> recover on that optional branch
required failure not ok    -> let it propagate
```

Example:

```java
CompletableFuture<UserDto> userDto =
        fetchUser(userId)
                .thenApply(UserDto::from);

CompletableFuture<List<Order>> orders =
        fetchUser(userId)
                .thenCompose(this::fetchOrders);

CompletableFuture<Profile> profile =
        fetchUser(userId)
                .thenCombine(fetchAccountSummary(userId), Profile::new);
```

Failures should propagate for required data because returning a dashboard without required user/order data lies to the caller. Failures may degrade for optional data such as recommendations, notifications, and non-critical personalization, but the response should include warning metadata.

Common trap: choosing `thenCompose()` for everything. `thenCompose()` is only needed when the next function returns another future.

## Solution: spot-completablefuture-bugs - Spot CompletableFuture Bugs

Immediate `join()` after `supplyAsync()`:

```java
User user = CompletableFuture.supplyAsync(this::fetchUser, executor).join();
Orders orders = CompletableFuture.supplyAsync(this::fetchOrders, executor).join();
```

This starts one task, waits, then starts the next task. The smallest fix is to start independent futures first:

```java
CompletableFuture<User> userFuture =
        CompletableFuture.supplyAsync(this::fetchUser, executor);
CompletableFuture<Orders> ordersFuture =
        CompletableFuture.supplyAsync(this::fetchOrders, executor);

User user = userFuture.join();
Orders orders = ordersFuture.join();
```

`thenApply()` around an async-returning method:

```java
CompletableFuture<CompletableFuture<List<Order>>> nested =
        fetchUser(userId).thenApply(this::fetchOrders);
```

The bug is the nested future. Use `thenCompose()`:

```java
CompletableFuture<List<Order>> orders =
        fetchUser(userId).thenCompose(this::fetchOrders);
```

One broad final `exceptionally()`:

```java
dashboardFuture.exceptionally(ex -> Dashboard.empty());
```

This can hide required failures such as user/order failure. The smallest fix is to recover only optional branches:

```java
CompletableFuture<List<Recommendation>> recommendations =
        fetchRecommendations().exceptionally(ex -> List.of());
```

Required futures should fail unless the business rule says otherwise.

Default common pool for blocking calls:

```java
CompletableFuture.supplyAsync(this::callRemoteService);
```

Without an executor, this uses the common pool. Blocking I/O can starve unrelated work using that shared pool. Pass a dedicated bounded executor for backend blocking calls:

```java
CompletableFuture.supplyAsync(this::callRemoteService, remoteCallExecutor);
```

Ignored returned future:

```java
CompletableFuture.runAsync(this::sendNotification, executor);
return response;
```

The failure is now easy to lose. The smallest fix depends on intent:

- If notification is required, return or compose the future.
- If notification is best-effort, attach logging/metrics.
- If the caller should wait, join only at the boundary.

Common trap: "fire and forget" is rarely free. It still needs failure handling, executor sizing, and shutdown behavior.

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

The important design choices are:

- User and orders start immediately and are required.
- Recommendations and notifications also start immediately, but are optional.
- Optional branches convert failure or timeout into explicit degraded values.
- `allOf()` coordinates all branches.
- `join()` is used only after `allOf()` completes.
- The final `orTimeout()` protects the whole request budget.

Required branches have no fallback:

```java
fetchUser(executor).orTimeout(1, TimeUnit.SECONDS);
fetchOrders(executor).orTimeout(1500, TimeUnit.MILLISECONDS);
```

If either fails, the dashboard fails. That is correct because the response cannot honestly be built without them.

Optional branches return explicit partial results:

```java
fetchRecommendations(executor)
        .completeOnTimeout(List.of(), 500, TimeUnit.MILLISECONDS)
        .handle((value, ex) -> ex == null
                ? new PartialResult<>(value, null)
                : new PartialResult<>(List.of(), "recommendations unavailable"));
```

This makes degradation visible instead of silently returning an empty list.

One possible supporting record is:

```java
record PartialResult<T>(T value, String warning) {
}
```

The assembly step should collect warnings:

```java
CompletableFuture<Dashboard> dashboardFuture =
        CompletableFuture
                .allOf(userFuture, orderFuture, recommendationFuture, notificationFuture)
                .thenApply(ignored -> {
                    PartialResult<List<Recommendation>> recommendations =
                            recommendationFuture.join();
                    PartialResult<List<Notification>> notifications =
                            notificationFuture.join();

                    List<String> warnings = Stream.of(
                                    recommendations.warning(),
                                    notifications.warning()
                            )
                            .filter(Objects::nonNull)
                            .toList();

                    return Dashboard.from(
                            userFuture.join(),
                            orderFuture.join(),
                            recommendations.value(),
                            notifications.value(),
                            warnings
                    );
                })
                .orTimeout(2, TimeUnit.SECONDS);
```

Common traps:

- Sequential calls caused by early `join()`.
- One final fallback that hides required failures.
- Optional fallbacks without warnings.
- Assuming CompletableFuture timeout cancels the underlying client call.
- Using the common pool for blocking service calls instead of a bounded executor.
