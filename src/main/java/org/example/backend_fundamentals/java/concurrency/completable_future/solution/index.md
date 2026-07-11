---
order: 20
search: false
---

# CompletableFuture Solutions

## Solution: compose-two-async-calls - Compose Two Async Calls

```java
CompletableFuture<List<Order>> ordersFuture = fetchUser(userId)
    .thenCompose(user -> fetchOrders(user))
    .exceptionally(ex -> List.of());
```

`thenCompose` flattens `CompletableFuture<CompletableFuture<List<Order>>>` into `CompletableFuture<List<Order>>`.
