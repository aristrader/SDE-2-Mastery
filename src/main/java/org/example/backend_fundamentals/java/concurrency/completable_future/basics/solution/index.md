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

String answer = answerFuture.join();
```

`runAsync()` is for work where completion matters but no value is produced, such as sending a notification, writing an audit event, or warming a cache. Because there is no result value, the future type is `CompletableFuture<Void>`. You can still observe success, failure, or completion, but `join()` returns `null`.

`supplyAsync()` is for work that produces a value. Here it produces `42`, and `thenApply()` performs a synchronous transformation on that completed value. `thenApply()` does not start a second asynchronous operation by itself; it says "when the previous stage has a value, map it to another value."

`join()` is the blocking point. The async work starts when `runAsync()` or `supplyAsync()` is called, but the current thread waits only when it calls `join()`. In backend code, keep that wait at the outer boundary, such as the controller, job runner, or test, instead of hiding it inside helper methods.

Common trap: using `runAsync()` when the caller needs a result. If a value matters, use `supplyAsync()`.

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

The first version gets little concurrency benefit because it starts the user fetch and immediately blocks until the user is available. Only after that does it start the orders fetch. The caller has written async-looking code, but the runtime shape is still mostly sequential:

1. Start user fetch.
2. Wait for user fetch.
3. Start orders fetch.
4. Wait for orders fetch.

The second version starts both independent futures first. If both calls are independent and use an executor with available threads, their latency can overlap:

1. Start user fetch.
2. Start orders fetch.
3. Wait for user.
4. Wait for orders.

This does not mean `join()` is always wrong. It means early `join()` is usually wrong when there is more independent work that could already be running.

Common trap: moving `join()` into `fetchUser()` or `fetchOrders()`. That hides blocking inside a helper and makes the caller think the workflow is async when it is not.

## Solution: get-vs-join-failure - get vs join Failure

```java
CompletableFuture<Integer> future =
        CompletableFuture.supplyAsync(() -> {
            throw new IllegalStateException("boom");
        }, executor);
```

Retrieving with `get()` uses the older `Future` API shape:

```java
try {
    future.get();
} catch (ExecutionException ex) {
    Throwable original = ex.getCause(); // IllegalStateException("boom")
}
```

Retrieving with `join()` uses the `CompletableFuture` convenience shape:

```java
try {
    future.join();
} catch (CompletionException ex) {
    Throwable original = ex.getCause(); // IllegalStateException("boom")
}
```

Both preserve the original `IllegalStateException` as the cause. The difference is the wrapper:

| Method | Wrapper | Checked? |
|---|---|---|
| `get()` | `ExecutionException` | yes |
| `join()` | `CompletionException` | no |

In CompletableFuture pipelines, `join()` is often cleaner at the final boundary because it avoids checked exception plumbing. In lower-level APIs that already expose `Future`, `get()` may be expected.

Common trap: logging only the wrapper and losing the cause. The useful failure is usually `ex.getCause()`.

## Solution: completed-and-manual-future - Completed And Manual Future

```java
CompletableFuture<User> cached =
        CompletableFuture.completedFuture(user);

CompletableFuture<String> manual = new CompletableFuture<>();
new Thread(() -> manual.complete("ready")).start();
```

`completedFuture()` is useful when the value is already available but the method contract returns a future. A cache hit is the classic example:

```java
CompletableFuture<User> findUser(String id) {
    User cached = cache.get(id);
    if (cached != null) {
        return CompletableFuture.completedFuture(cached);
    }
    return loadUserAsync(id);
}
```

This keeps the API uniform. The caller can compose the result the same way whether it came from cache or from async I/O.

Manual completion is different. It creates a future now and lets some external event complete it later:

```java
CompletableFuture<String> response = new CompletableFuture<>();

messageBroker.register("request-123", message -> {
    if (message.isSuccess()) {
        response.complete(message.body());
    } else {
        response.completeExceptionally(message.error());
    }
});
```

Manual futures are useful when completion is driven by callbacks, listeners, message brokers, timers, or another framework. `supplyAsync()` owns the work itself; manual completion adapts work that is completed somewhere else.

Common traps:

- Forgetting to complete the future on failure, leaving callers waiting forever.
- Completing the same future from multiple paths without understanding that only the first completion wins.
- Starting raw `new Thread(...)` in production code instead of using a managed executor or framework callback.
