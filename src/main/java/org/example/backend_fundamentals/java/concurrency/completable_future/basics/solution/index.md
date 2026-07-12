---
order: 20
search: false
---

# CompletableFuture Basics Solutions

## Solution: run-supply-apply - runAsync, supplyAsync, thenApply

```java
CompletableFuture<Void> emailFuture =
        CompletableFuture.runAsync(this::sendEmail, executor);

CompletableFuture<String> answerFuture =
        CompletableFuture.supplyAsync(() -> 42, executor)
                .thenApply(value -> "The answer is " + value);
```

`runAsync()` has no business result, so its type is `Void`. `join()` blocks the caller when the result is requested.

## Solution: immediate-join - Immediate Join

```java
User user = CompletableFuture
        .supplyAsync(this::fetchUser, executor)
        .join();

List<Order> orders = CompletableFuture
        .supplyAsync(this::fetchOrders, executor)
        .join();
```

That starts and waits one at a time. Prefer:

```java
CompletableFuture<User> userFuture =
        CompletableFuture.supplyAsync(this::fetchUser, executor);
CompletableFuture<List<Order>> orderFuture =
        CompletableFuture.supplyAsync(this::fetchOrders, executor);

User user = userFuture.join();
List<Order> orders = orderFuture.join();
```

## Solution: get-vs-join-failure - get vs join Failure

```java
CompletableFuture<Integer> future =
        CompletableFuture.supplyAsync(() -> {
            throw new IllegalStateException("boom");
        }, executor);
```

`future.get()` throws `ExecutionException`; `future.join()` throws `CompletionException`. In both cases, inspect `getCause()` for the original failure.

## Solution: completed-and-manual-future - Completed And Manual Future

```java
CompletableFuture<User> cached =
        CompletableFuture.completedFuture(user);

CompletableFuture<String> manual = new CompletableFuture<>();
new Thread(() -> manual.complete("ready")).start();
```

`completedFuture()` keeps APIs uniform for already available values. Manual completion lets outside code resolve the future when an event arrives.
