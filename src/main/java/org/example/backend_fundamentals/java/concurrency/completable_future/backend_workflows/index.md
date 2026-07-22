---
order: 40
---

# CompletableFuture Backend Workflows

Backend `CompletableFuture` design starts by classifying work before writing code.

For every step, ask:

- Is it local CPU work or remote IO?
- Is it dependent on a previous result?
- Is it independent and safe to start now?
- Is it required or optional?
- What is the timeout budget?
- What executor should run it?
- What context must cross the thread boundary?

## Dashboard aggregation

Example:

```text
user profile       required
orders             required
recommendations    optional
notifications      optional
```

Start independent calls immediately:

```java
CompletableFuture<User> user =
        fetchUserAsync(userId).orTimeout(800, TimeUnit.MILLISECONDS);

CompletableFuture<List<Order>> orders =
        fetchOrdersAsync(userId).orTimeout(800, TimeUnit.MILLISECONDS);

CompletableFuture<List<Recommendation>> recommendations =
        fetchRecommendationsAsync(userId)
                .completeOnTimeout(List.of(), 300, TimeUnit.MILLISECONDS)
                .exceptionally(ex -> List.of());

CompletableFuture<Dashboard> dashboard =
        user.thenCombine(orders, RequiredDashboardData::new)
                .thenCombine(recommendations, Dashboard::new);
```

The optional branch handles its own fallback. Required branches still fail the dashboard.

## Order pipeline

Example dependency graph:

```text
validate request
inventory check     \
                     -> price quote -> payment -> reservation -> notification
fraud check         /
analytics optional
```

Use local validation synchronously before starting remote async work. Do not spend remote capacity on invalid requests.

Use `thenCombine()` for independent required checks:

```java
CompletableFuture<Inventory> inventory = checkInventoryAsync(order);
CompletableFuture<FraudResult> fraud = checkFraudAsync(order);

CompletableFuture<ValidatedOrder> validated =
        inventory.thenCombine(fraud, (inv, fraudResult) ->
                validate(order, inv, fraudResult));
```

Use `thenCompose()` when the next async step depends on the previous result:

```java
CompletableFuture<Receipt> receipt =
        validated
                .thenCompose(this::priceAsync)
                .thenCompose(this::chargePaymentAsync)
                .thenCompose(this::reserveInventoryAsync);
```

Payment retries require idempotency keys outside `CompletableFuture`. The async chain controls order; it does not make side effects safe to repeat.

## Async cache

If many requests ask for the same key, cache the in-flight future so only one load starts.

```java
private final ConcurrentHashMap<String, CompletableFuture<User>> cache =
        new ConcurrentHashMap<>();

CompletableFuture<User> getUser(String userId) {
    return cache.computeIfAbsent(userId, id ->
            fetchUserAsync(id)
                    .whenComplete((value, ex) -> {
                        if (ex != null) {
                            cache.remove(id);
                        }
                    }));
}
```

Why remove failed futures: otherwise the cache stores a permanent failure and every later request fails immediately.

Be careful with cancellation. If one caller cancels a shared in-flight future, it may affect other callers. Often request-level timeout should wrap waiting for the result, not cancel the shared load itself.

## Batch loading

For a dynamic list:

```java
List<CompletableFuture<User>> futures = userIds.stream()
        .map(this::getUser)
        .toList();

CompletableFuture<List<User>> users =
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
```

Limit concurrency if the list can be large. Starting 10,000 HTTP calls at once can overload your own service or the downstream service. Use a bounded executor, semaphore, or batching API.

## Transaction and context boundaries

Async work runs on another thread. In Spring applications, many important values are stored in `ThreadLocal`.

Do not assume async code inherits:

- Spring transaction context
- `SecurityContext`
- MDC / trace IDs
- tenant context
- request attributes
- locale

`InheritableThreadLocal` is not a reliable fix for thread pools because pool threads are created once and reused across requests.

If context must cross the boundary, copy it deliberately and clear it in `finally`. In Spring, use a `TaskDecorator`, context-aware executor wrapper, or framework-supported context propagation.

## Transactions and side effects

This is risky:

```java
@Transactional
void placeOrder(Order order) {
    orderRepository.save(order);
    CompletableFuture.runAsync(() -> sendSuccessEmail(order), executor);
}
```

The email can be sent before the transaction commits. If the transaction later rolls back, the side effect already escaped.

Better patterns:

- publish after commit
- transactional outbox
- durable message after commit
- idempotent downstream processing

## Executor choice

Do not use one executor for every kind of async work.

Separate pools by blocking profile and ownership:

| Work | Executor guidance |
|---|---|
| Blocking HTTP/JDBC | Dedicated bounded IO executor or virtual threads |
| CPU-heavy transformation | CPU-sized executor |
| Fire-and-forget non-critical side effect | Bounded executor with explicit rejection behavior |
| Shared app-wide async API | Named, monitored executor |

Always name threads. Anonymous pool threads make production dumps harder to read.

## Common gotchas

**"The async method is called inside `@Transactional`, so it shares the transaction."**
No. The worker thread does not automatically share the caller's transaction context.

**"MDC disappeared only in async logs."**
Expected. MDC is thread-local. Copy it intentionally and clear it after the task.

**"SecurityContext is null in async code."**
Expected with default thread-local strategy. Use Spring delegation helpers or a task decorator.

**"Notification sent, then DB rolled back."**
The side effect escaped before commit. Use after-commit publication or outbox.

**"CompletableFuture made the request faster."**
Only if independent work actually ran in parallel and you avoided blocking too early.

## Quick recall

**Q. What is the first workflow step?**
A. Classify dependency vs independence, required vs optional, timeout budget, and executor choice.

**Q. Where should blocking happen?**
A. At the outer boundary where the final result is required, not inside helper methods.

**Q. How do you prevent duplicate async cache loads?**
A. Cache the in-flight future with `ConcurrentHashMap.computeIfAbsent`.

**Q. Why remove failed futures from an async cache?**
A. So a transient failure does not poison the cache permanently.

**Q. Why is async inside a transaction risky?**
A. The worker thread does not inherit the transaction, and side effects may happen before commit.

**Q. What context is commonly lost across async boundaries?**
A. MDC/tracing IDs, security context, tenant context, request attributes, locale, and transaction context.

**Q. Why not start thousands of futures at once?**
A. You can overwhelm downstream systems. Use bounded executors, semaphores, or batching.
