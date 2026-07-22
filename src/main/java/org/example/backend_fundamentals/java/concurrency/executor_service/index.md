---
order: 110
---

# ExecutorService & ThreadPoolExecutor

An executor separates "what work should run" from "which thread runs it".

Manual threads are fine for learning lifecycle. Backend services should usually submit tasks to an executor because executors control thread reuse, queueing, shutdown, naming, rejection, and backpressure.

## Mental model

```text
caller submits task
  -> executor decides: run now, queue, create thread, or reject
  -> worker thread executes task
  -> Future represents task result
```

`Executor` is the smallest interface:

```java
executor.execute(() -> doWork());
```

`ExecutorService` adds lifecycle and result-bearing task submission:

```java
Future<Result> future = executor.submit(this::loadResult);
```

`ThreadPoolExecutor` is the main concrete implementation behind most classic executor factory methods.

## Interface hierarchy

```text
Executor
  -> ExecutorService
       -> ScheduledExecutorService

ThreadPoolExecutor
ScheduledThreadPoolExecutor
ForkJoinPool
```

`ForkJoinPool` is a separate implementation designed for work-stealing and CPU-style divide-and-conquer tasks. It is also the default pool behind many `CompletableFuture` async calls when you do not pass an executor.

## `execute()` vs `submit()`

| Method | Input | Result | Exception behavior |
|---|---|---|---|
| `execute(Runnable)` | Fire-and-forget task | No `Future` | Uncaught exception goes to thread's uncaught exception handler |
| `submit(Runnable/Callable)` | Task with optional result | Returns `Future` | Exception is captured and rethrown from `Future.get()` |

This surprises people:

```java
Future<?> future = executor.submit(() -> {
    throw new IllegalStateException("boom");
});

// Exception is not thrown here.
future.get(); // throws ExecutionException wrapping the failure
```

If nobody calls `get()`, submitted task failures can be silently ignored unless you log them inside the task or customize executor hooks.

## ThreadPoolExecutor constructor

```java
new ThreadPoolExecutor(
    int corePoolSize,
    int maximumPoolSize,
    long keepAliveTime,
    TimeUnit unit,
    BlockingQueue<Runnable> workQueue,
    ThreadFactory threadFactory,
    RejectedExecutionHandler handler
)
```

Meaning:

| Parameter | Meaning |
|---|---|
| `corePoolSize` | Normal thread count the pool tries to keep. |
| `maximumPoolSize` | Hard upper bound on worker threads. |
| `keepAliveTime` | How long above-core idle threads live before exiting. |
| `workQueue` | Where tasks wait when workers are busy. |
| `threadFactory` | Names/configures worker threads. |
| `handler` | What happens when the pool cannot accept more work. |

## Thread growth algorithm

This is the most important `ThreadPoolExecutor` detail.

When a task is submitted:

1. If running workers < `corePoolSize`, create a worker for the task.
2. Else try to enqueue the task.
3. If the queue is full, create another worker up to `maximumPoolSize`.
4. If the queue is full and workers are already at max, reject the task.

So the queue fills before the pool grows beyond core.

This matters because an unbounded queue prevents growth beyond core:

```text
core = 10
max = 100
queue = unbounded LinkedBlockingQueue

After 10 active workers, task 11 goes into the queue.
The queue never becomes "full".
The pool never grows to 100.
```

## Queue strategies

| Queue | Capacity | Behavior |
|---|---|---|
| `LinkedBlockingQueue` without capacity | Unbounded | Queue grows until memory runs out; max threads are effectively ignored after core. |
| `ArrayBlockingQueue` | Bounded | Queue fills, pool can grow to max, then rejection applies. |
| `SynchronousQueue` | Zero | No storage; each task must hand off directly to a worker or create one. |
| `PriorityBlockingQueue` | Usually unbounded | Orders tasks by priority; still has unbounded-memory risk unless carefully controlled. |

Example bounded pool shape:

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
        20,
        50,
        30,
        TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(500),
        namedThreadFactory("orders-worker"),
        new ThreadPoolExecutor.CallerRunsPolicy()
);
```

The important part is not these exact numbers. The important part is that both worker count and queue size have explicit limits.

## Factory methods and hidden defaults

### `newFixedThreadPool(n)`

```java
Executors.newFixedThreadPool(n)
```

Hidden shape:

```text
core = n
max = n
queue = unbounded LinkedBlockingQueue
```

Risk: under sustained overload, tasks queue forever until memory pressure or unacceptable latency.

### `newCachedThreadPool()`

Hidden shape:

```text
core = 0
max = Integer.MAX_VALUE
queue = SynchronousQueue
```

Risk: under burst load, it can create a huge number of OS threads and exhaust the process or machine.

### `newSingleThreadExecutor()`

One worker, unbounded queue, serial execution.

Good for strict ordering. Dangerous if producers can submit faster than the single worker can drain.

### `newWorkStealingPool()`

Backed by `ForkJoinPool`. Good for CPU-bound fork/join style work. Do not use it as a dumping ground for blocking database or HTTP calls.

## Rejection policies

Rejection happens only when the queue is full and the pool is at `maximumPoolSize`.

| Policy | Behavior | Risk / use |
|---|---|---|
| `AbortPolicy` | Throws `RejectedExecutionException` | Good when caller must handle overload explicitly. |
| `CallerRunsPolicy` | Caller thread runs the task | Applies backpressure by slowing submitter. |
| `DiscardPolicy` | Silently drops the new task | Only for truly disposable work. |
| `DiscardOldestPolicy` | Drops oldest queued task, retries submit | Only when newer work supersedes older work. |

`CallerRunsPolicy` is often a good service default because it does not lose work and it slows the producer naturally.

## Sizing pools

There is no magic number. Start from workload type.

CPU-bound work:

```text
threads ~= number of CPU cores
```

More threads than cores usually just adds context switching.

IO-bound work:

```text
threads depend on expected concurrent blocking calls and downstream limits
```

If each task spends most time waiting for DB/HTTP, more threads than cores may help, but only up to the point your database, remote service, or connection pool can handle.

Always align executor size with downstream resources. A 200-thread HTTP worker pool with a 20-connection DB pool often just creates blocked threads.

## Shutdown

Executors own non-daemon worker threads by default. If you forget to shut them down, the JVM may stay alive and services leak resources.

Graceful shutdown pattern:

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

Meaning:

- `shutdown()`: stop accepting new tasks; finish queued and running tasks.
- `awaitTermination()`: wait for shutdown to finish.
- `shutdownNow()`: best-effort interrupt of running workers and returns tasks that never started.

`shutdownNow()` does not magically kill code. Tasks must respond to interruption.

## Pool starvation

Do not block worker threads waiting for other tasks submitted to the same small pool.

```java
ExecutorService pool = Executors.newFixedThreadPool(2);

pool.submit(() -> {
    Future<String> child = pool.submit(this::loadData);
    return child.get(); // parent worker blocks waiting for child
});
```

If all workers become parents waiting for children, child tasks sit queued forever.

Fix by composing work differently, using separate executors, increasing capacity deliberately, or avoiding blocking inside pool tasks.

## Virtual threads

Java 21 adds:

```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(this::blockingHttpCall);
}
```

Virtual threads are lightweight JVM-managed threads. Blocking a virtual thread on supported blocking IO parks the virtual thread instead of monopolizing an OS thread.

Good fit:

- request-per-task style code
- blocking HTTP/JDBC/file IO
- code that becomes simpler when written sequentially

Not a magic fix:

- CPU-bound work still needs CPU cores.
- synchronized pinning and native calls can reduce benefits.
- downstream limits still matter: DB pools, rate limits, connection limits.

Virtual threads reduce the need to tune thread-pool size for blocking concurrency. They do not remove the need for backpressure.

## Quick recall

**Q. Why use an executor instead of manual `new Thread()`?**
A. Executors reuse and control threads, queue tasks, expose results, handle shutdown, and provide overload behavior.

**Q. What is the `ThreadPoolExecutor` growth order?**
A. Grow to core, then queue, then grow to max, then reject.

**Q. Why is an unbounded queue dangerous?**
A. It can grow until memory is exhausted and it prevents growth beyond core threads.

**Q. Why is `newCachedThreadPool()` risky?**
A. It can create up to `Integer.MAX_VALUE` platform threads under burst load.

**Q. What does `CallerRunsPolicy` do?**
A. The submitting thread runs the task, slowing producers and applying backpressure.

**Q. What is the safe shutdown pattern?**
A. `shutdown()`, then `awaitTermination(timeout)`, then `shutdownNow()` if needed, restoring interrupt if interrupted.

**Q. How can a pool deadlock without locks?**
A. Workers block waiting for child tasks submitted to the same saturated pool, leaving no worker free to run the children.
