---
order: 20
---

# CompletableFuture Composition

Composition is the main reason to use `CompletableFuture`: start independent work early, chain dependent work clearly, and wait only at the boundary.

## thenApply vs thenCompose

Use `thenApply()` when the function returns a plain value:

```java
CompletableFuture<UserDTO> dtoFuture =
        fetchUserAsync()
                .thenApply(this::toDto);
```

Use `thenCompose()` when the next function returns another future:

```java
CompletableFuture<List<Order>> ordersFuture =
        fetchUserAsync()
                .thenCompose(this::fetchOrdersAsync);
```

Using `thenApply()` with an async-returning method creates a nested future:

```java
CompletableFuture<CompletableFuture<List<Order>>> nested =
        fetchUserAsync().thenApply(this::fetchOrdersAsync);
```

Mental model: `thenApply()` is `map`; `thenCompose()` is `flatMap`.

## thenCompose vs thenCombine

Use `thenCompose()` for dependency:

```text
fetch user -> fetch orders for that user
```

Use `thenCombine()` for independence:

```text
fetch user
fetch account summary
combine both
```

```java
CompletableFuture<ProfileResponse> profileFuture =
        fetchUserAsync()
                .thenCombine(fetchAccountSummaryAsync(), ProfileResponse::new);
```

## allOf

Use `allOf()` for several independent futures, especially a dynamic list.

```java
List<CompletableFuture<User>> futures = userIds.stream()
        .map(this::fetchUserAsync)
        .toList();

CompletableFuture<List<User>> usersFuture =
        CompletableFuture
                .allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
```

The result order above follows the `futures` list order, not completion order.

## Quick recall

**Q. When do you use `thenCompose()`?**
A. The next step depends on the previous result and returns another `CompletableFuture`.

**Q. When do you use `thenCombine()`?**
A. Two futures are independent and their results need merging.

**Q. What does `allOf()` return?**
A. `CompletableFuture<Void>`; read values from the original futures after it completes.

**Q. What is accidental serialization?**
A. Chaining independent calls or joining too early so latency becomes the sum instead of the max.

**Q. How do you preserve input order for a dynamic batch?**
A. Store futures in input order, wait with `allOf()`, then join in that same order.
