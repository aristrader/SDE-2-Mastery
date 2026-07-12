---
order: 120
---

# CompletableFuture

---

## Study path

Use the focused subpages in this order:

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `basics` | `runAsync`, `supplyAsync`, `thenApply`, blocking, `get` vs `join`, completed futures. |
| 2 | `composition` | `thenCompose`, `thenCombine`, `allOf`, dynamic lists, preserving order. |
| 3 | `failure_timeouts` | `exceptionally`, `handle`, `whenComplete`, required vs optional failure, timeout, retry. |
| 4 | `backend_workflows` | Dashboard, order pipeline, async cache, batch loading, transaction/context boundaries. |

The rest of this page is the compact reference view.

---

## Why Future fell short

`Future<T>` (Java 5) was the first async primitive. It has five hard limitations:

| Limitation | Impact |
|---|---|
| `get()` blocks the calling thread | No way to chain work without blocking |
| No completion callback | Can't say "when done, do X" |
| No exception handling pipeline | Checked `ExecutionException` wraps every failure; must unwrap manually |
| Can't complete manually | No `complete(value)` — you can't resolve a `Future` programmatically from outside the executor |
| `cancel()` doesn't propagate | Cancelling one `Future` doesn't cancel dependent downstream work |

`CompletableFuture<T>` (Java 8) solves all five. It implements both `Future<T>` and `CompletionStage<T>` — the stage interface provides the composition API.

---

## Creation

```java
// Runs Supplier in ForkJoinPool.commonPool() by default
CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> fetchUser(id));

// Supply a dedicated executor — critical for IO-bound work (see thread pool trap below)
CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> fetchUser(id), ioExecutor);

// Runnable — no return value
CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> sendEmail(payload), ioExecutor);

// Already-completed — useful in tests or as a constant fallback
CompletableFuture<String> cf = CompletableFuture.completedFuture("cached-value");
```

---

## Transformation

Every method in this section returns a **new** `CompletableFuture`. The original is not mutated.

| Method | What it does | Thread it runs on |
|---|---|---|
| `thenApply(fn)` | Synchronous transform on the result (like `Stream.map`) | Completing thread or calling thread |
| `thenApplyAsync(fn)` | Same transform, but submitted to the executor | Common pool or provided executor |
| `thenAccept(consumer)` | Consume the result, return `CompletableFuture<Void>` | Completing thread |
| `thenRun(runnable)` | Run next step — no input, no output | Completing thread |

```java
CompletableFuture.supplyAsync(() -> fetchUser(id))
    .thenApply(user -> user.getEmail())       // String → String
    .thenAccept(email -> log.info(email));    // terminal consumer
```

---

## Composition: thenApply vs thenCompose

The most common source of bugs.

**`thenApply`** — the function returns a plain value `T`. Use it when the next step is a simple in-memory transform.

**`thenCompose`** — the function returns a `CompletableFuture<T>`, which gets flattened. Use it when the next step is itself async.

```java
// WRONG — thenApply with an async function produces a nested future
CompletableFuture<CompletableFuture<Order>> nested =
    CompletableFuture.supplyAsync(() -> fetchUser(id))
        .thenApply(user -> fetchOrders(user));  // fetchOrders returns CF<Order>

// CORRECT — thenCompose flattens it to CF<Order>
CompletableFuture<Order> flat =
    CompletableFuture.supplyAsync(() -> fetchUser(id))
        .thenCompose(user -> fetchOrders(user));
```

Mental model: `thenApply` is `Stream.map`, `thenCompose` is `Stream.flatMap`.

---

## Combining multiple futures

```java
CompletableFuture<String> userFuture  = CompletableFuture.supplyAsync(() -> fetchUser(id));
CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync(() -> fetchOrders(id));

// Wait for both, combine their results
CompletableFuture<String> combined =
    userFuture.thenCombine(orderFuture, (user, orders) -> user + " | " + orders);

// Wait for all; returns CF<Void>
CompletableFuture<Void> all = CompletableFuture.allOf(userFuture, orderFuture);

// Collect results after allOf completes
all.thenApply(v -> Stream.of(userFuture, orderFuture)
                         .map(CompletableFuture::join)
                         .collect(Collectors.toList()));

// Complete when the first one completes (result type is Object)
CompletableFuture<Object> any = CompletableFuture.anyOf(userFuture, orderFuture);
```

`allOf` is the most commonly needed. It returns `CF<Void>` — collect individual results via `join()` inside `thenApply`.

---

## Exception handling

Three methods — different contracts:

| Method | Signature | Called when | Can recover? | Passes result forward? |
|---|---|---|---|---|
| `exceptionally(fn)` | `T fn(Throwable)` | Exception only | Yes — return fallback | Yes (the fallback value) |
| `handle(fn)` | `U fn(T, Throwable)` | Always (result OR exception) | Yes — or re-throw | Yes |
| `whenComplete(fn)` | `void fn(T, Throwable)` | Always | No — side-effect only | Passes original result/exception through |

```java
CompletableFuture.supplyAsync(() -> riskyFetch())
    .exceptionally(ex -> "default-value")          // recover with fallback
    .thenApply(value -> transform(value));

CompletableFuture.supplyAsync(() -> riskyFetch())
    .handle((result, ex) -> {
        if (ex != null) return "fallback";
        return result;
    });

// whenComplete is like finally — does NOT suppress the exception
CompletableFuture.supplyAsync(() -> riskyFetch())
    .whenComplete((result, ex) -> log.info("done, ex={}", ex))
    .thenApply(value -> transform(value));  // still sees the original exception if one occurred
```

---

## get() vs join()

| | `get()` | `join()` |
|---|---|---|
| Checked exception | `ExecutionException` (must catch) | None |
| Unchecked exception | `ExecutionException` | `CompletionException` |
| Interrupt behavior | Throws `InterruptedException` | Ignores interrupts |
| Preferred in | Legacy / API that requires checked handling | Non-blocking chains, streams |

Use `join()` when assembling a pipeline in application code. Use `get()` only when the calling API forces checked exception handling.

---

## Thread pool trap

`supplyAsync` without an executor uses `ForkJoinPool.commonPool()`. In a web server:

- The common pool is also used by parallel streams and `ForkJoinTask` work
- Long-running IO tasks (DB calls, HTTP calls) **block common pool threads**
- This can starve unrelated parallel computations across the whole JVM

**Rule:** always pass a dedicated executor for IO-bound async work.

```java
ExecutorService ioPool = Executors.newFixedThreadPool(50);  // or use a bounded pool sized for your IO concurrency

CompletableFuture.supplyAsync(() -> httpClient.get(url), ioPool);
```

For CPU-bound work, the common pool is fine (it's sized to `Runtime.availableProcessors() - 1`).

---

## Quick recall

**Q. What are the key limitations of `Future` that `CompletableFuture` fixes?**
A. Blocking `get()`, no composition, no exception pipeline, can't complete manually, cancel doesn't propagate to dependents.

**Q. `thenApply` vs `thenCompose` — what's the rule?**
A. If the next step returns a plain value, use `thenApply`. If it returns a `CompletableFuture`, use `thenCompose` to avoid `CF<CF<T>>`.

**Q. `allOf` returns `CF<Void>` — how do you get the results?**
A. Call `join()` on each individual future inside a `thenApply` after `allOf` completes.

**Q. `exceptionally` vs `handle` vs `whenComplete` — when does each trigger?**
A. `exceptionally` only on failure (returns fallback). `handle` always (can recover or re-throw). `whenComplete` always, side-effect only, doesn't suppress the exception.

**Q. `join()` vs `get()` — which to prefer and why?**
A. `join()` — throws unchecked `CompletionException`, no need to catch checked exceptions in pipeline code.

**Q. Why is using `ForkJoinPool.commonPool()` for IO dangerous in a web server?**
A. IO tasks block common pool threads and can starve parallel streams and other FJ work across the JVM; always pass a dedicated executor.
