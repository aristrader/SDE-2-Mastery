---
order: 10
search: false
---

# CompletableFuture Composition Practice

## Exercise: compose-two-async-calls - Compose Two Async Calls

### Goal
Use `thenCompose()` when the next async step depends on the previous result.

### Task
Create `fetchUser(id)` returning `CompletableFuture<User>` and `fetchOrders(user)` returning `CompletableFuture<List<Order>>`. Compose them into one future of orders.

### Checks
- Use `thenCompose`, not nested `thenApply`.
- Explain why orders depend on user.

## Exercise: nested-future-bug - Nested Future Bug

### Goal
Make the type problem concrete.

### Task
Deliberately produce `CompletableFuture<CompletableFuture<List<Order>>>`, then fix it.

### Checks
- Identify the nested type.
- Replace `thenApply()` with `thenCompose()`.

## Exercise: combine-independent-calls - Combine Independent Calls

### Goal
Use `thenCombine()` for independent futures.

### Task
Mock `fetchUserAsync()` and `fetchAccountSummaryAsync()`. Combine them into `ProfileResponse`.

### Checks
- Start both calls without waiting.
- Explain why `thenCompose()` would serialize independent work.

## Exercise: allof-dashboard - allOf Dashboard

### Goal
Coordinate several independent futures.

### Task
Build a dashboard from user, orders, recommendations, and notifications.

### Checks
- Use `allOf()` and read results afterward.
- Block only at the outer boundary.

## Exercise: dynamic-allof-preserve-order - Dynamic allOf Preserve Order

### Goal
Handle a variable-sized batch.

### Task
Given `List<String> userIds`, call `fetchUserAsync(id)` for each ID and return `CompletableFuture<List<User>>` preserving input order.

### Checks
- Use `CompletableFuture.allOf(futures.toArray(...))`.
- Join futures in list order after `allOf()`.
- Explain input order vs completion order.

## Exercise: parallel-branches-after-dependency - Parallel Branches After Dependency

### Goal
Mix dependency and independence.

### Task
Fetch user first. Then, using that user, fetch orders and preferences in parallel and build a dashboard.

### Checks
- Outer step uses `thenCompose()`.
- Inner independent branches use `thenCombine()`.
