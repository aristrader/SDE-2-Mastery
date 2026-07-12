---
order: 10
search: false
---

# ExecutorService Practice

## Exercise: fixed-thread-pool - Fixed Thread Pool

### Goal
Run tasks through an executor instead of creating threads manually.

### Task
Create a fixed thread pool of size 3.

Submit 10 tasks that print the task id and thread name. Shut down the executor cleanly.

### Checks
- No manual `new Thread(...).start()` per task.
- `shutdown()` is called.
- Explain why pools avoid unbounded thread creation.

## Exercise: callable-future - Callable And Future

### Goal
Return a value from background work.

### Task
Submit a `Callable<Integer>` that sums `1..100`. Retrieve the result with `Future.get()`.

### Checks
- Explain where blocking occurs.
- Handle `InterruptedException` by restoring interrupt status.
- Explain how task failure is wrapped.

## Exercise: submit-before-waiting - Submit Before Waiting

### Goal
Avoid accidental serialization.

### Task
Mock `fetchUser()` and `fetchOrders()` as one-second callables. Compare immediate `get()` after each submit with submitting both first, then waiting.

### Checks
- Explain why the first version takes roughly the sum.
- Explain why the second version can overlap.

## Exercise: bounded-pool-backpressure - Bounded Pool Backpressure

### Goal
Understand core size, max size, queue capacity, and rejection.

### Task
Create a `ThreadPoolExecutor` with core `2`, max `4`, queue capacity `5`, and `CallerRunsPolicy`. Submit slow tasks and print thread names.

### Checks
- Explain when the pool grows beyond core.
- Explain when rejection policy runs.
- Explain how caller-runs slows producers.

## Exercise: hidden-submit-failure - Hidden submit Failure

### Goal
Observe the difference between `execute()` and `submit()`.

### Task
Run one failing task with `execute()` and one with `submit()`. For the submitted task, first ignore the `Future`, then call `get()`.

### Checks
- Explain why ignored futures can hide failures.
- Explain why fire-and-forget work still needs logging or monitoring.

## Exercise: pool-starvation-deadlock - Pool Starvation Deadlock

### Goal
Spot nested blocking inside small pools.

### Task
Spot the bug:

```java
ExecutorService pool = Executors.newFixedThreadPool(2);

Callable<String> parent = () -> {
    Future<String> child = pool.submit(() -> fetchRemoteValue());
    return "parent + " + child.get();
};

Future<String> first = pool.submit(parent);
Future<String> second = pool.submit(parent);
```

### Checks
- Identify why child tasks never run.
- Propose a fix using composition, separate executors, or avoiding nested blocking.

## Exercise: cancellation-aware-task - Cancellation Aware Task

### Goal
Practice cooperative cancellation.

### Task
Submit a task that loops, sleeps, and exits when interrupted. Cancel it with `future.cancel(true)`.

### Checks
- Do not swallow `InterruptedException`.
- Restore interrupt status when catching it.
- Explain why cancellation is not forceful termination.

## Exercise: graceful-shutdown - Graceful Shutdown Utility

### Goal
Avoid executor leaks.

### Task
Write `shutdownGracefully(ExecutorService executor)` that calls `shutdown()`, waits, falls back to `shutdownNow()`, and preserves interrupt status.

### Checks
- Use `awaitTermination`.
- Call `shutdownNow()` if interrupted.
- Explain why pools should not be created and forgotten.

## Exercise: custom-thread-factory - Custom Thread Factory

### Goal
Make thread dumps and logs readable.

### Task
Create a fixed pool whose workers are named `report-worker-1`, `report-worker-2`, etc.

### Checks
- Use `AtomicInteger` for the sequence.
- Explain why thread names matter in production debugging.
