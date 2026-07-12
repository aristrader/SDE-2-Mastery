---
order: 20
search: false
---

# CompletableFuture Composition Solutions

## Solution: compose-two-async-calls - Compose Two Async Calls

```java
CompletableFuture<List<Order>> ordersFuture =
        fetchUser(userId)
                .thenCompose(this::fetchOrders);
```

`thenCompose()` flattens the future returned by `fetchOrders`.

## Solution: nested-future-bug - Nested Future Bug

```java
CompletableFuture<CompletableFuture<List<Order>>> nested =
        fetchUser(userId)
                .thenApply(this::fetchOrders);
```

Fix:

```java
CompletableFuture<List<Order>> flat =
        fetchUser(userId)
                .thenCompose(this::fetchOrders);
```

## Solution: combine-independent-calls - Combine Independent Calls

```java
CompletableFuture<ProfileResponse> profileFuture =
        fetchUserAsync()
                .thenCombine(
                        fetchAccountSummaryAsync(),
                        ProfileResponse::new
                );
```

The two calls do not depend on each other.

## Solution: allof-dashboard - allOf Dashboard

```java
CompletableFuture<User> userFuture = fetchUserAsync();
CompletableFuture<List<Order>> orderFuture = fetchOrdersAsync();
CompletableFuture<List<Recommendation>> recommendationFuture = fetchRecommendationsAsync();
CompletableFuture<List<Notification>> notificationFuture = fetchNotificationsAsync();

CompletableFuture<Dashboard> dashboardFuture =
        CompletableFuture
                .allOf(userFuture, orderFuture, recommendationFuture, notificationFuture)
                .thenApply(ignored -> new Dashboard(
                        userFuture.join(),
                        orderFuture.join(),
                        recommendationFuture.join(),
                        notificationFuture.join()
                ));
```

The inner `join()` calls happen after `allOf()` completes.

## Solution: dynamic-allof-preserve-order - Dynamic allOf Preserve Order

```java
List<CompletableFuture<User>> futures = userIds.stream()
        .map(this::fetchUserAsync)
        .toList();

return CompletableFuture
        .allOf(futures.toArray(CompletableFuture[]::new))
        .thenApply(ignored -> futures.stream()
                .map(CompletableFuture::join)
                .toList());
```

The final list preserves input order because the stream reads futures in input order.

## Solution: parallel-branches-after-dependency - Parallel Branches After Dependency

```java
CompletableFuture<UserDashboard> future =
        fetchUserAsync()
                .thenCompose(user -> {
                    CompletableFuture<List<Order>> ordersFuture =
                            fetchOrdersAsync(user);
                    CompletableFuture<Preferences> preferencesFuture =
                            fetchPreferencesAsync(user);

                    return ordersFuture.thenCombine(
                            preferencesFuture,
                            (orders, preferences) ->
                                    new UserDashboard(user, orders, preferences)
                    );
                });
```

The user is required before both branches, but the branches are independent after that.
