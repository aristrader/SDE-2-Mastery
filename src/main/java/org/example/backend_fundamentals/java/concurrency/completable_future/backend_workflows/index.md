---
order: 40
---

# CompletableFuture Backend Workflows

Backend CompletableFuture design starts by classifying work:

- synchronous local validation or mapping
- dependent async calls
- independent async calls
- required dependencies
- optional dependencies
- fire-and-forget side effects

## Dashboard Aggregation

```text
user required
orders required
recommendations optional
notifications optional
```

Start all independent calls immediately. Attach fallbacks only to optional branches. Coordinate with `allOf()`.

## Order Pipeline

```text
validate request
inventory and price in parallel
payment depends on price
reservation after payment
notification after success
analytics optional
```

Use `thenCombine()` for independent branches and `thenCompose()` for dependent async stages.

Payment retries require idempotency outside CompletableFuture.

## Async Cache

Use an in-flight future cache to prevent duplicate loads:

```java
ConcurrentHashMap<String, CompletableFuture<User>> cache =
        new ConcurrentHashMap<>();
```

Remove failed futures so later calls can retry.

## Transaction And Context Boundaries

Async work runs on another thread. Do not assume it inherits:

- Spring transaction context
- SecurityContext
- MDC / tracing context
- tenant context

Spring stores most of these in `ThreadLocal`. A pooled worker thread starts with its own thread-local values, not the request thread's values. `InheritableThreadLocal` is not a reliable fix for thread pools because pool threads are created once and reused.

If context must cross the boundary, copy it deliberately and clear it in `finally`. In Spring, this is usually a `TaskDecorator`, a context-aware executor wrapper, or a framework-supported context propagation mechanism.

Do not send success notifications before the database transaction commits. Use after-commit hooks, outbox, or durable messaging.

## Trick questions / gotchas

**"The async method is called inside `@Transactional`, so it shares the transaction."** No. The worker thread has its own transaction context unless a new transaction is explicitly started there.

**"MDC disappeared only in async logs."** Expected: MDC is thread-local. Copy it intentionally and clear it after the task.

**"SecurityContext is null in async code."** Expected with default `MODE_THREADLOCAL`. Use Spring's delegation helpers or a task decorator; do not rely on `MODE_INHERITABLETHREADLOCAL` for pools.

**"Notification sent, then DB rolled back."** The side effect escaped before commit. Use after-commit publication or outbox.

## Quick recall

**Q. Where should blocking happen?**
A. At the outer boundary where the final result is needed, not inside helper methods.

**Q. What is the first workflow step?**
A. Classify dependency vs independence and required vs optional failure.

**Q. How do you prevent duplicate async cache loads?**
A. `ConcurrentHashMap.computeIfAbsent(key, key -> loadAsync(key))`.

**Q. Why not create one executor per request?**
A. It leaks resources and defeats pool sizing.

**Q. Why is async inside a transaction risky?**
A. The worker thread does not automatically share the caller's transaction, and side effects can happen before commit.

**Q. What request context is commonly lost across async boundaries?**
A. MDC/tracing IDs, SecurityContext, tenant context, request attributes, and transaction context.
