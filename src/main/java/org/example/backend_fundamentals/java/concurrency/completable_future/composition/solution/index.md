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

`fetchOrders(user)` cannot start correctly until `fetchUser(userId)` has completed, because it needs the actual `User`. That makes this a dependent async call.

`thenCompose()` is the right method because the next function already returns a `CompletableFuture<List<Order>>`. It flattens the result from:

```java
CompletableFuture<CompletableFuture<List<Order>>>
```

into:

```java
CompletableFuture<List<Order>>
```

Use this rule in interviews: if the lambda returns a plain value, use `thenApply()`; if it returns another future, use `thenCompose()`.

Common trap: calling `join()` inside the lambda:

```java
fetchUser(userId).thenApply(user -> fetchOrders(user).join());
```

That blocks an executor thread and defeats the composition model. Return the future and let the pipeline continue.

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

The bug is the type. `thenApply()` wraps the return value of the lambda as the next stage result. Since `fetchOrders(user)` already returns a future, the outer result becomes a future whose value is another future.

That creates awkward caller code:

```java
List<Order> orders = nested.join().join();
```

The first `join()` waits for the user stage and returns the inner future. The second `join()` waits for orders. That is a smell: callers should not need to unwrap layers created accidentally.

`thenCompose()` expresses the intended workflow: complete the user stage, start the orders stage, and expose one future for the final orders value.

Common trap: thinking `thenApply()` means "then run another async method." It means "map the previous value." If the map function returns a future, you probably need `thenCompose()`.

## Solution: combine-independent-calls - Combine Independent Calls

```java
CompletableFuture<User> userFuture = fetchUserAsync();
CompletableFuture<AccountSummary> accountFuture = fetchAccountSummaryAsync();

CompletableFuture<ProfileResponse> profileFuture =
        userFuture.thenCombine(accountFuture, ProfileResponse::new);
```

The two calls are independent: account summary does not need the `User` object returned by `fetchUserAsync()`, and the user fetch does not need the account summary. Start both futures before combining them so their latency can overlap.

`thenCombine()` waits for both futures to complete successfully, then calls the combiner function with both values.

`thenCompose()` would be the wrong shape if written like this:

```java
CompletableFuture<ProfileResponse> slower =
        fetchUserAsync().thenCompose(user ->
                fetchAccountSummaryAsync()
                        .thenApply(account -> new ProfileResponse(user, account)));
```

This starts the account call only after the user call completes, which serializes independent work.

Common trap: choosing methods by habit instead of dependency. Ask: "Does B need A's result to start?" If yes, compose. If no, start both and combine.

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

`allOf()` is a coordination future. It does not itself contain the typed results; it only completes when every input future completes, or fails when one input fails.

That is why the code reads each typed future after `allOf()`:

```java
userFuture.join()
orderFuture.join()
recommendationFuture.join()
notificationFuture.join()
```

Those inner `join()` calls are safe in the `thenApply()` because `allOf()` has already completed successfully. They are not starting new blocking waits in the normal success path; they are extracting already-available values.

The outer caller can decide where to block:

```java
Dashboard dashboard = dashboardFuture.join(); // boundary
```

In a web stack that supports async responses, the controller may return the future instead of joining.

Common traps:

- Calling `join()` before `allOf()`, which serializes work.
- Expecting `allOf()` to return `List<Object>` or typed results. It returns `CompletableFuture<Void>`.
- Adding optional fallbacks at the very end, which can hide required failures. Optional branches should recover before `allOf()`.

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

The `futures` list is built in the same order as `userIds`. `allOf()` waits for every future, but it does not reorder anything and it does not collect values. After all futures complete, the code streams over the original `futures` list and joins each one in list order.

That means the output order is input order, not completion order:

```text
input:        [u1, u2, u3]
completion:   u3, u1, u2
output:       [u1, u2, u3]
```

This is usually what backend callers expect when they pass a list of IDs.

Common traps:

- Appending results to a shared list from each completion handler. That can produce completion order and introduce thread-safety issues.
- Forgetting that if any future fails, `allOf()` fails. If the requirement is "one result per input ID even on failure", convert each item to a success/failure result with `handle()` before `allOf()`.
- Launching a huge number of futures against an unbounded executor. For large batches, use a bounded executor, chunking, or backpressure.

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

This workflow has two levels:

1. Fetch the user first because both later calls need the user.
2. After the user is available, fetch orders and preferences in parallel.

The outer `thenCompose()` is needed because the lambda starts more async work and returns a future. Inside that lambda, orders and preferences are independent of each other, so they are started immediately and combined with `thenCombine()`.

This shape avoids both common mistakes:

- It does not start orders/preferences before the required user exists.
- It does not serialize orders and preferences after the user exists.

Common trap: writing `fetchOrdersAsync(user).thenCompose(orders -> fetchPreferencesAsync(user)...)`. That makes preferences wait for orders even though preferences only depends on user.
