---
order: 10
search: false
---

# CompletableFuture Basics Practice

## Exercise: run-supply-apply - runAsync, supplyAsync, thenApply

### Goal
Choose the basic creation and transformation methods.

### Task
Create one `runAsync()` task that returns no value and one `supplyAsync()` task that returns `42`, then transform `42` into `"The answer is 42"` with `thenApply()`.

### Checks
- Explain why `runAsync()` returns `CompletableFuture<Void>`.
- Explain where blocking occurs when `join()` is called.

## Exercise: immediate-join - Immediate Join

### Goal
Notice accidental blocking.

### Task
Write one version that calls `join()` immediately after `supplyAsync()`, and one version that starts two futures before joining either.

### Checks
- Explain why immediate join gives little concurrency benefit.
- Explain why starting both futures first allows overlap.

## Exercise: get-vs-join-failure - get vs join Failure

### Goal
Observe failure wrappers.

### Task
Create a future that throws `IllegalStateException`. Retrieve it once with `get()` and once with `join()`.

### Checks
- `get()` reports `ExecutionException`.
- `join()` reports `CompletionException`.
- Original cause is still available.

## Exercise: completed-and-manual-future - Completed And Manual Future

### Goal
Understand that not every future starts new async work.

### Task
Use `completedFuture()` for a cache hit. Then create a new `CompletableFuture<String>()` and complete it manually from another thread.

### Checks
- Explain when `completedFuture()` is useful.
- Explain what manual completion adds beyond `supplyAsync()`.
