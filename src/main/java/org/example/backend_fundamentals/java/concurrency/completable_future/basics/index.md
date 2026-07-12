---
order: 10
---

# CompletableFuture Basics

Start here before composition. The goal is to understand what a future represents, where work runs, and where blocking actually happens.

## Creation

Use `runAsync()` for work with no business result:

```java
CompletableFuture<Void> emailFuture =
        CompletableFuture.runAsync(this::sendEmail, executor);
```

Use `supplyAsync()` for work that returns a value:

```java
CompletableFuture<User> userFuture =
        CompletableFuture.supplyAsync(this::fetchUser, executor);
```

Without an executor, Java uses the common ForkJoin pool. That is usually a poor default for blocking database or HTTP calls in backend services.

## Transformation

`thenApply()` transforms a completed value into another plain value.

```java
CompletableFuture<UserDTO> dtoFuture =
        userFuture.thenApply(this::toDto);
```

Every stage returns a new future. The original future is not mutated.

## Blocking

Pipeline creation does not block:

```java
CompletableFuture<UserDTO> future =
        CompletableFuture.supplyAsync(this::fetchUser, executor)
                .thenApply(this::toDto);

System.out.println("created");
```

Blocking happens when a caller retrieves the result:

```java
UserDTO dto = future.join();
```

`join()` still blocks. The benefit of `CompletableFuture` is that you avoid blocking between every stage.

## get() vs join()

| Method | Failure wrapper | Checked handling | Interrupt handling |
|---|---|---|---|
| `get()` | `ExecutionException` | Yes | Throws `InterruptedException` |
| `join()` | `CompletionException` | No | Does not throw checked interrupt |

Use `join()` inside CompletableFuture pipelines and boundary assembly code. Use `get()` when an API requires checked exception handling.

## Completed And Manual Futures

Not every future launches new work.

```java
CompletableFuture<User> cached =
        CompletableFuture.completedFuture(user);
```

Manual completion is useful when another event completes the value:

```java
CompletableFuture<String> future = new CompletableFuture<>();
future.complete("ready");
```

## Quick recall

**Q. `runAsync()` vs `supplyAsync()`?**
A. `runAsync()` has no result; `supplyAsync()` returns a value.

**Q. Does creating a CompletableFuture pipeline block?**
A. No. Blocking starts at `join()`, `get()`, or another explicit wait.

**Q. Does repeated `join()` rerun the task?**
A. No. A completed future stores its terminal result or failure.

**Q. Why pass a custom executor?**
A. To control thread count, isolation, queueing, naming, and blocking workload behavior.

**Q. What is `completedFuture()` for?**
A. Returning an already available value through an async-shaped API.
