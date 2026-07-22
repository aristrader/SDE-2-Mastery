---
order: 10
---

# CompletableFuture Basics

A `CompletableFuture<T>` represents a value that may be available later: either a successful `T` or a failure.

It is not a thread by itself. Work runs on whichever thread completes the stage: an executor worker, the caller thread, or another thread that manually completes the future.

## What problem it solves

`Future<T>` lets you submit work and later block for the result:

```java
Future<User> future = executor.submit(this::fetchUser);
User user = future.get(); // blocks
```

But plain `Future` cannot say: "when user arrives, transform it; if that succeeds, call another async service; if optional branch fails, fallback."

`CompletableFuture` adds that pipeline model.

## Creation

Use `runAsync()` for work with no result:

```java
CompletableFuture<Void> emailFuture =
        CompletableFuture.runAsync(() -> sendEmail(payload), executor);
```

Use `supplyAsync()` for work that returns a value:

```java
CompletableFuture<User> userFuture =
        CompletableFuture.supplyAsync(() -> fetchUser(userId), executor);
```

Always notice the executor. Without one, async stages use `ForkJoinPool.commonPool()` by default. That is a poor default for blocking database or HTTP calls in backend services because those calls can occupy common-pool worker threads.

## A future can already be complete

Not every future starts new work.

```java
CompletableFuture<User> cached =
        CompletableFuture.completedFuture(user);
```

This is useful when an API returns `CompletableFuture<T>` but you already have the value, such as a cache hit or test fixture.

Manual completion:

```java
CompletableFuture<String> future = new CompletableFuture<>();

future.complete("ready");
```

Manual completion is useful when a callback or external event decides when the value is available.

## Transformation

`thenApply()` transforms a successful value into another plain value.

```java
CompletableFuture<UserDto> dtoFuture =
        userFuture.thenApply(this::toDto);
```

Every stage returns a new future. The original future is not mutated.

```text
userFuture --thenApply--> dtoFuture
```

If `userFuture` fails, `thenApply()` is skipped and `dtoFuture` completes with the same failure unless a recovery stage is attached.

## Where stages run

Non-`Async` methods usually run on the thread that completes the previous stage.

```java
userFuture.thenApply(this::toDto);
```

If `userFuture` completes on an IO executor worker, `toDto` may run on that same worker.

`Async` methods submit the stage to an executor:

```java
userFuture.thenApplyAsync(this::toDto, cpuExecutor);
```

If you omit the executor:

```java
userFuture.thenApplyAsync(this::toDto);
```

it uses the common pool. Be deliberate.

Rule of thumb:

- small CPU-light mapping: non-`Async` is fine
- blocking IO or expensive CPU work: use `Async` with an explicit executor

## Blocking

Creating a pipeline does not block:

```java
CompletableFuture<UserDto> future =
        CompletableFuture.supplyAsync(() -> fetchUser(userId), executor)
                .thenApply(this::toDto);

System.out.println("pipeline created");
```

Blocking happens when a caller demands the result:

```java
UserDto dto = future.join();
```

`join()` still blocks the current thread. The benefit is that you avoid blocking between every stage and can start independent work early.

Bad helper shape:

```java
UserDto loadUserDto(String id) {
    return fetchUserAsync(id)
            .thenApply(this::toDto)
            .join(); // blocks inside helper, destroying async composition
}
```

Better:

```java
CompletableFuture<UserDto> loadUserDtoAsync(String id) {
    return fetchUserAsync(id)
            .thenApply(this::toDto);
}
```

Block at the outer boundary where a synchronous caller truly needs the final result.

## `get()` vs `join()`

| Method | Failure wrapper | Checked handling | Interrupt behavior |
|---|---|---|---|
| `get()` | `ExecutionException` | Must catch/declare | Throws `InterruptedException` |
| `join()` | `CompletionException` | Unchecked | Does not throw checked interrupt |

Use `join()` when composing futures and collecting results after `allOf()`.

Use `get()` when you are at a boundary that needs checked interruption handling.

Important: `join()` not throwing `InterruptedException` does not mean interruption is irrelevant. If your thread is interrupted while blocked in `join()`, the interrupt flag remains set. Boundary code should still have a cancellation policy.

## Cancellation

`CompletableFuture.cancel(true)` completes the future with cancellation. It does not reliably interrupt the underlying task the way people expect from some `Future` implementations.

Design cancellation and timeouts explicitly:

- set timeout on the HTTP/JDBC/client call
- use `orTimeout()` or `completeOnTimeout()` for future completion policy
- make long-running code check cancellation/interruption when appropriate

## Quick recall

**Q. Is a `CompletableFuture` a thread?**
A. No. It is a handle for a future result. Work runs on an executor, completing thread, caller thread, or manual completer.

**Q. `runAsync()` vs `supplyAsync()`?**
A. `runAsync()` has no result; `supplyAsync()` returns a value.

**Q. Does building a CompletableFuture pipeline block?**
A. No. Blocking starts at `join()`, `get()`, or another explicit wait.

**Q. Where does `thenApply()` run?**
A. Usually on the thread that completes the previous stage.

**Q. Why pass a custom executor?**
A. To control threads and isolate blocking or expensive work from the common pool.

**Q. Why avoid `join()` inside helper methods?**
A. It turns async code back into blocking code and prevents callers from composing work.

