---
order: 10
search: false
---

# CompletableFuture Practice

## Exercise: compose-two-async-calls - Compose Two Async Calls

### Goal
Use `thenCompose` when the next async step depends on the previous result.

### Task
Create `fetchUser(id)` returning `CompletableFuture<User>` and `fetchOrders(user)` returning `CompletableFuture<List<Order>>`.

Compose them into one future of orders.

### Checks
- Use `thenCompose`, not nested `thenApply`.
- Handle failure with `exceptionally` or `handle`.
