---
order: 10
search: false
---

# CompletableFuture Backend Workflow Practice

## Exercise: dashboard-service - Dashboard Service

### Goal
Build a realistic fan-out/fan-in service.

### Task
Implement `loadDashboard()` with required user/orders, optional recommendations/notifications, per-service timeouts, overall timeout, and warnings for degraded optional branches.

### Checks
- Start independent calls immediately.
- Required failures still fail.
- Optional failures are visible in warnings.

## Exercise: order-pipeline - Order Pipeline

### Goal
Classify dependency correctly.

### Task
Design an order flow: validate, check inventory, calculate price, charge payment, reserve inventory, notify, record analytics.

### Checks
- Inventory and price run in parallel.
- Payment depends on price.
- Reservation happens only after payment succeeds.
- Analytics does not hide required failure.

## Exercise: batch-loader - Batch Loader

### Goal
Handle many async calls without losing input order.

### Task
Implement `loadUsers(List<String> ids)` returning one result per input ID, capturing individual failures instead of failing the whole batch.

### Checks
- Use `allOf()`.
- Preserve input order.
- Use `handle()` per item.
- Use a bounded executor or chunking for large batches.

## Exercise: async-cache-single-flight - Async Cache Single Flight

### Goal
Prevent duplicate async loads.

### Task
Implement `getUser(id)` with `ConcurrentHashMap<String, CompletableFuture<User>>` so concurrent misses share the same in-flight load.

### Checks
- Use `computeIfAbsent()`.
- Remove failed futures.
- Do not call `join()` inside the cache method.

## Exercise: async-transaction-boundary - Async Transaction Boundary

### Goal
Avoid false assumptions about thread-bound context.

### Task
Explain what can go wrong when a transactional request starts an async notification before commit. Propose a safer design.

### Checks
- Mention rollback after notification.
- Mention after-commit hooks, outbox, or durable messaging.

## Exercise: request-context-loss - Request Context Loss

### Goal
Catch thread-local context loss across async boundaries.

### Task
A request thread has MDC correlation ID, Spring `SecurityContext`, and tenant context. It starts work with `CompletableFuture.supplyAsync(...)` on a pooled executor. Explain what the worker sees and how production code should handle it.

### Checks
- Mention that these contexts are thread-local.
- Mention that pool threads are reused.
- Mention copying context before execution and clearing it in `finally`.
- Mention Spring `TaskDecorator` or context-aware executor wrappers.
