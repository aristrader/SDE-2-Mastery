---
order: 110
---

# ExecutorService & ThreadPoolExecutor

Use executors when you have many tasks. Manual `new Thread()` is for learning lifecycle, not normal backend work.

Mental model:

```text
submit task -> pool runs now / queues / creates worker / rejects -> Future may hold result
```

## What to study

| Topic | Interview depth | What to remember |
| --- | --- | --- |
| `ExecutorService` | High | Submit tasks, get `Future`, shut down the pool. |
| `execute()` vs `submit()` | High | `submit()` captures exceptions inside `Future`; `execute()` does not return a `Future`. |
| Pool sizing + queueing | High | Core threads first, then queue, then max threads, then rejection. |
| Unbounded queue risk | High | Can hide overload and grow memory/latency. |
| Rejection/backpressure | Medium | `CallerRunsPolicy` slows the submitter instead of dropping work. |
| Shutdown | High | `shutdown`, `awaitTermination`, `shutdownNow`, restore interrupt. |
| Pool starvation | High | Do not block all pool workers waiting for child tasks in the same pool. |
| Virtual threads | Medium | Good for blocking IO in Java 21+, not a CPU or downstream-limit fix. |
| `ForkJoinPool` internals | Low | Recognize it; skip deep work-stealing details for now. |

## Why executor instead of `new Thread()`

| Manual thread | Executor |
| --- | --- |
| Creates a new OS thread per task | Reuses worker threads |
| No built-in queue/backpressure | Can queue, reject, or slow submitter |
| Harder lifecycle management | Has shutdown APIs |
| No result abstraction unless you build one | `Future` from `submit()` |

Interview answer: executors centralize thread management, task queueing, results, failure handling, shutdown, and overload behavior.

## `execute()` vs `submit()`

| Method | Use when | Failure behavior |
| --- | --- | --- |
| `execute(Runnable)` | Fire-and-forget | Uncaught exception goes to thread's handler. |
| `submit(Runnable/Callable)` | Need result or completion handle | Exception is stored and rethrown from `Future.get()`. |

Trap:

```java
Future<?> future = executor.submit(() -> {
    throw new IllegalStateException("boom");
});

// No exception here.
future.get(); // throws ExecutionException
```

If nobody calls `get()`, failures from `submit()` can be missed. Log inside tasks or check returned futures when failure matters.

## ThreadPoolExecutor growth

Constructor knobs:

```java
new ThreadPoolExecutor(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        threadFactory,
        rejectedExecutionHandler
);
```

Submission order:

1. If workers < core, create a worker.
2. Else queue the task.
3. If queue is full, create workers up to max.
4. If queue is full and workers are at max, reject.

Important consequence:

```text
core = 10
max = 100
queue = unbounded

after 10 workers, tasks keep entering the queue
pool never grows to 100 because the queue never becomes full
```

So bounded backend pools should usually bound both workers and queue size.

## Queue and rejection choices

| Choice | Interview point |
| --- | --- |
| Unbounded queue | Simple but dangerous under overload. |
| Bounded queue | Makes overload visible and allows rejection/backpressure. |
| `AbortPolicy` | Throws `RejectedExecutionException`; caller must handle overload. |
| `CallerRunsPolicy` | Caller runs the task; slows producers naturally. |
| Discard policies | Only for disposable work. Say no unless loss is acceptable. |

Avoid memorizing every queue class. Know the risk: unbounded queues hide overload; unbounded threads can crash the process.

## Factory method traps

| Factory | Hidden risk |
| --- | --- |
| `newFixedThreadPool(n)` | Fixed workers + unbounded queue. Latency/memory can grow. |
| `newCachedThreadPool()` | Can create a very large number of platform threads. |
| `newSingleThreadExecutor()` | One worker + unbounded queue. Good ordering, bad overload behavior. |

Interview stance: factory methods are fine for demos. In services, prefer explicit bounded `ThreadPoolExecutor` or framework-managed executors.

## Pool sizing

CPU-bound work:

```text
threads ~= CPU cores
```

Blocking IO work:

```text
threads depend on downstream limits
```

Do not create 200 worker threads if the DB pool has 20 connections. You will mostly create blocked threads.

## Shutdown

```java
executor.shutdown();
try {
    if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
        executor.shutdownNow();
    }
} catch (InterruptedException e) {
    executor.shutdownNow();
    Thread.currentThread().interrupt();
}
```

| Method | Meaning |
| --- | --- |
| `shutdown()` | Stop accepting new tasks; finish existing tasks. |
| `awaitTermination()` | Wait for the pool to finish. |
| `shutdownNow()` | Best-effort interrupt of running tasks; returns tasks not started. |

`shutdownNow()` does not kill stubborn code. Tasks must respect interruption.

## Pool starvation

Bad shape:

```java
ExecutorService pool = Executors.newFixedThreadPool(2);

pool.submit(() -> {
    Future<String> child = pool.submit(this::loadData);
    return child.get();
});
```

If every worker blocks waiting for child tasks submitted to the same pool, the children may never run.

Fix: avoid blocking inside pool tasks, use async composition, use separate executors, or size the pool deliberately.

## Virtual threads

Java 21 virtual threads are good for many blocking IO tasks:

```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(this::blockingHttpCall);
}
```

Do not oversell them:

- CPU-bound work still needs CPU cores.
- DB/API limits still apply.
- Backpressure is still required.
- Some blocking/native/synchronized cases can reduce benefits.

Interview stance: virtual threads simplify blocking-style code, but they do not remove resource limits.

## What not to over-study

| Topic | Why |
| --- | --- |
| Full `Executor` interface hierarchy | Know `ExecutorService`; skip hierarchy memorization. |
| Every queue implementation | Know bounded vs unbounded. |
| Deep `ForkJoinPool` work-stealing | Usually asked separately, not core executor prep. |
| Every rejection policy edge case | Know abort, caller-runs, and when dropping is unacceptable. |
| Exact pool-size formulas | Use workload type and downstream limits; formulas are approximations. |

## Quick recall

**Q. Why use an executor instead of manual threads?**  
A. Reuse/control threads, queue work, get results, shut down cleanly, and handle overload.

**Q. `execute()` vs `submit()`?**  
A. `execute()` is fire-and-forget. `submit()` returns `Future` and stores task exceptions until `get()`.

**Q. What is the pool growth order?**  
A. Core threads, then queue, then max threads, then reject.

**Q. Why is an unbounded queue risky?**  
A. It can hide overload, grow latency/memory, and prevent growth beyond core threads.

**Q. What does `CallerRunsPolicy` do?**  
A. The submitting thread runs the task, slowing producers.

**Q. Safe shutdown pattern?**  
A. `shutdown()`, wait, `shutdownNow()` if needed, restore interrupt.

**Q. How can a pool deadlock without locks?**  
A. Workers block waiting for child tasks queued to the same saturated pool.
