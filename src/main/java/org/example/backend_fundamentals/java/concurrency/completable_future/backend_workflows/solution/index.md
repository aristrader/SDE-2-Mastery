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
```

Coordinate with `allOf()` and build the dashboard after all futures complete. Required futures do not receive fallback.

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

`thenCombine()` is for independent inventory/price. `thenCompose()` is for payment and reservation dependencies.

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

Each item converts success/failure into a value, so the batch can complete with all outcomes.

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

The cache method returns the shared future and does not block.

## Solution: async-transaction-boundary - Async Transaction Boundary

The notification may be sent before the database transaction commits. If the transaction later rolls back, users or downstream systems see a success event for data that does not exist.

Safer designs include after-commit hooks for simple cases, or a transactional outbox plus durable message publishing for production workflows.

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

In Spring, prefer a `TaskDecorator`, `DelegatingSecurityContextExecutor`, or another context-aware executor. Clearing in `finally` prevents one request's context leaking into the next task on the same pooled thread.
