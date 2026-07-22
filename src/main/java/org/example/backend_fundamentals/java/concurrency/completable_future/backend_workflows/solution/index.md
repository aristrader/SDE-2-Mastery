---
order: 20
search: false
---

# CompletableFuture Backend Workflow Solutions

## Solution: dashboard-service - Dashboard Service

```java
CompletableFuture<User> userFuture =
        fetchUser().orTimeout(1, TimeUnit.SECONDS);

CompletableFuture<List<Order>> orderFuture =
        fetchOrders().orTimeout(1500, TimeUnit.MILLISECONDS);

CompletableFuture<PartialResult<List<Recommendation>>> recommendations =
        fetchRecommendations()
                .completeOnTimeout(List.of(), 500, TimeUnit.MILLISECONDS)
                .handle((value, ex) -> ex == null
                        ? new PartialResult<>(value, null)
                        : new PartialResult<>(List.of(), "recommendations unavailable"));

CompletableFuture<PartialResult<List<Notification>>> notifications =
        fetchNotifications()
                .completeOnTimeout(List.of(), 300, TimeUnit.MILLISECONDS)
                .handle((value, ex) -> ex == null
                        ? new PartialResult<>(value, null)
                        : new PartialResult<>(List.of(), "notifications unavailable"));

CompletableFuture<Dashboard> dashboard =
        CompletableFuture
                .allOf(userFuture, orderFuture, recommendations, notifications)
                .thenApply(ignored -> {
                    PartialResult<List<Recommendation>> recommendationResult =
                            recommendations.join();
                    PartialResult<List<Notification>> notificationResult =
                            notifications.join();

                    List<String> warnings = Stream.of(
                                    recommendationResult.warning(),
                                    notificationResult.warning()
                            )
                            .filter(Objects::nonNull)
                            .toList();

                    return new Dashboard(
                            userFuture.join(),
                            orderFuture.join(),
                            recommendationResult.value(),
                            notificationResult.value(),
                            warnings
                    );
                })
                .orTimeout(2, TimeUnit.SECONDS);
```

Start all independent calls immediately. User and orders are required, so they use `orTimeout()` and receive no fallback. If either one fails or times out, the dashboard future fails.

Recommendations and notifications are optional, so each branch owns its degradation behavior. `completeOnTimeout()` handles slowness with a fallback, and `handle()` converts any failure into a `PartialResult` with a warning. That keeps the response honest: it can still render, but callers know part of it is degraded.

`allOf()` coordinates completion. The `join()` calls inside `thenApply()` are used only after all component futures have completed successfully from the perspective of the composed workflow.

Common traps:

- Calling `join()` immediately after each fetch, which serializes the workflow.
- Adding one broad final fallback to the dashboard, which hides required failures.
- Returning fallback data for optional dependencies without warnings.
- Assuming `orTimeout()` cancels the underlying HTTP/JDBC work. Configure client-level timeouts too.

## Solution: order-pipeline - Order Pipeline

```java
OrderRequest valid = validate(request);

CompletableFuture<Inventory> inventoryFuture = checkInventory(valid);
CompletableFuture<Price> priceFuture = calculatePrice(valid);

CompletableFuture<OrderResult> result =
        inventoryFuture
                .thenCombine(priceFuture, OrderContext::new)
                .thenCompose(context ->
                        chargePayment(context)
                                .thenCompose(payment ->
                                        reserveInventory(context, payment)))
                .thenApply(this::buildSuccess)
                .whenComplete((order, ex) -> recordAnalytics(order, ex));
```

The dependency graph is:

```text
validate
  -> inventory check
  -> price calculation
inventory + price
  -> charge payment
  -> reserve inventory
  -> success response
analytics observes the final outcome
```

Inventory and price can run in parallel after validation because neither needs the other's result. `thenCombine()` waits for both and creates an `OrderContext`.

Payment depends on the calculated price and validated context, so it is a dependent async step. Reservation must happen only after payment succeeds, so it is another dependent async step. That is why both are modeled with `thenCompose()`.

`whenComplete()` is used for analytics because analytics should observe success or failure without changing the result. If payment fails, analytics can record the failure, but the order pipeline should still fail.

Common traps:

- Reserving inventory before payment succeeds when the business rule requires payment first.
- Letting analytics failure fail the whole order after the required order work succeeded. In production, analytics is often best-effort and should be isolated.
- Catching payment failure and returning success. Required workflow failures must propagate.

## Solution: batch-loader - Batch Loader

```java
List<CompletableFuture<UserResult>> futures = ids.stream()
        .map(id -> fetchUser(id)
                .handle((user, ex) -> ex == null
                        ? UserResult.success(id, user)
                        : UserResult.failure(id, rootCause(ex).getMessage())))
        .toList();

return CompletableFuture
        .allOf(futures.toArray(CompletableFuture[]::new))
        .thenApply(ignored -> futures.stream()
                .map(CompletableFuture::join)
                .toList());
```

Each item uses `handle()` before `allOf()`. That is the key detail. It converts each individual outcome into a `UserResult`, so one failed ID does not fail the whole batch.

The final list preserves input order because the result stream reads the original `futures` list in the same order as `ids`. Completion order does not matter.

Example result shape:

```java
record UserResult(String id, User user, String error) {
    static UserResult success(String id, User user) {
        return new UserResult(id, user, null);
    }

    static UserResult failure(String id, String error) {
        return new UserResult(id, null, error);
    }
}
```

For a small bounded list, this shape is fine. For a large list, do not fire thousands of requests blindly into a shared pool. Use a bounded executor, chunk the IDs, or use a client with backpressure.

Common traps:

- Putting one `exceptionally()` after `allOf()`, which loses which ID failed.
- Collecting into a shared mutable list from completion callbacks, which can scramble order and require synchronization.
- Using the common pool for blocking remote calls.

## Solution: async-cache-single-flight - Async Cache Single Flight

```java
final class AsyncUserCache {
    private final ConcurrentHashMap<String, CompletableFuture<User>> cache =
            new ConcurrentHashMap<>();

    CompletableFuture<User> getUser(String id) {
        return cache.computeIfAbsent(id, key ->
                loadUserAsync(key).whenComplete((user, ex) -> {
                    if (ex != null) {
                        cache.remove(key);
                    }
                }));
    }
}
```

`computeIfAbsent()` makes concurrent misses for the same ID share one in-flight future. The first caller creates the load; later callers receive the same `CompletableFuture<User>`.

The method returns the future directly:

```java
return cache.computeIfAbsent(...);
```

It does not call `join()`, so callers can still compose, timeout, cancel, or combine the operation at a higher boundary.

Removing failed futures is important. If the load fails and the failed future stays in the map, every later caller immediately sees the old failure and the cache can never recover for that key.

For stricter removal, remove only the same future instance:

```java
CompletableFuture<User> created = loadUserAsync(key);
created.whenComplete((user, ex) -> {
    if (ex != null) {
        cache.remove(key, created);
    }
});
return created;
```

Common traps:

- Caching failed futures forever.
- Blocking inside `getUser()`.
- Starting the load outside `computeIfAbsent()`, which can still allow duplicate concurrent loads.
- Treating this as a complete cache. It is single-flight; production caches may also need TTL, size limits, invalidation, and metrics.

## Solution: async-transaction-boundary - Async Transaction Boundary

The notification may be sent before the database transaction commits. If the transaction later rolls back, users or downstream systems see a success event for data that does not exist.

Example failure:

```text
1. Request thread starts a transaction.
2. Service writes Order(id=123, status=CREATED).
3. Service starts CompletableFuture.runAsync(sendNotification).
4. Notification says "order 123 created".
5. Database commit fails or transaction rolls back.
6. User/downstream system saw an event for an order that does not exist.
```

The async task also runs on another thread, so it does not automatically share the request transaction. Depending on the code, it may read stale data, missing data, or data that later rolls back.

Safer designs:

- Use an after-commit hook for simple in-process side effects that should run only after commit succeeds.
- Use a transactional outbox for production workflows: write the domain change and an outbox row in the same transaction, then publish the outbox row asynchronously after commit.
- Use durable messaging when downstream systems must eventually receive the event.

Common trap: assuming `CompletableFuture.runAsync()` is "inside" the transaction because the line appears inside a `@Transactional` method. Transaction context is thread-bound; the worker thread does not automatically inherit it.

## Solution: request-context-loss - Request Context Loss

The worker thread usually sees blank or stale thread-local context. MDC, `SecurityContextHolder`, tenant context, request attributes, and Spring transaction state are bound to the current thread, not magically copied to executor workers.

Production code should wrap tasks with explicit capture/restore/clear logic:

```java
Runnable decorated = () -> {
    try {
        restoreCapturedContext();
        task.run();
    } finally {
        clearContext();
    }
};
```

The dangerous part is pooled thread reuse. A worker thread may have handled a previous request. If context is not cleared, the next task can accidentally see stale MDC, tenant, or security data.

A safer shape is:

```java
Map<String, String> mdc = MDC.getCopyOfContextMap();
SecurityContext securityContext = SecurityContextHolder.getContext();
String tenant = TenantContext.getTenantId();

executor.execute(() -> {
    try {
        if (mdc != null) {
            MDC.setContextMap(mdc);
        }
        SecurityContextHolder.setContext(securityContext);
        TenantContext.setTenantId(tenant);

        task.run();
    } finally {
        MDC.clear();
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }
});
```

In Spring, prefer framework-supported hooks such as `TaskDecorator`, `DelegatingSecurityContextExecutor`, or a context-aware executor wrapper instead of hand-copying context at every call site.

Common traps:

- Assuming thread-local request data automatically crosses async boundaries.
- Copying context but not clearing it.
- Propagating transaction context into async work. Usually the async work should start its own transaction or run after commit.
