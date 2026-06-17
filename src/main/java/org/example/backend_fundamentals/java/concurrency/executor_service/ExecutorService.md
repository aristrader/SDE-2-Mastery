# ExecutorService & ThreadPoolExecutor

---

## Interface hierarchy

```
Executor
  └── ExecutorService
        └── ScheduledExecutorService
              └── ScheduledThreadPoolExecutor
AbstractExecutorService
  └── ThreadPoolExecutor          ← core implementation
        └── ScheduledThreadPoolExecutor
ForkJoinPool                      ← separate hierarchy; used by newWorkStealingPool()
```

`Executor` has one method: `execute(Runnable)`. `ExecutorService` adds lifecycle (`shutdown`, `shutdownNow`) and `submit`/`invokeAll`/`invokeAny`. `ThreadPoolExecutor` is the concrete class behind every `Executors` factory method except `newWorkStealingPool`.

---

## ThreadPoolExecutor constructor — all 7 parameters

```java
new ThreadPoolExecutor(
    int corePoolSize,          // threads kept alive even when idle
    int maximumPoolSize,       // hard ceiling on thread count
    long keepAliveTime,        // idle time before an above-core thread terminates
    TimeUnit unit,             // unit for keepAliveTime
    BlockingQueue<Runnable> workQueue,  // holds tasks when all core threads are busy
    ThreadFactory threadFactory,        // creates new threads (name, daemon, priority)
    RejectedExecutionHandler handler    // what to do when queue is full and max threads are reached
)
```

---

## Thread growth algorithm — the critical detail

The most commonly misunderstood behaviour:

1. If active threads < corePoolSize → **spawn a new thread** (even if idle threads exist).
2. If active threads ≥ corePoolSize → **enqueue the task**. No new thread yet.
3. If the queue is full → **spawn a new thread** up to maximumPoolSize.
4. If the queue is full and threads = maximumPoolSize → **invoke rejection handler**.

The queue fills **before** threads grow beyond core — surprising to people who expect threads to grow to max first. With an unbounded queue (LinkedBlockingQueue), step 3 is never reached and threads never exceed corePoolSize regardless of load.

---

## Queue strategies

| Queue | Capacity | Used by | Behaviour |
|---|---|---|---|
| `LinkedBlockingQueue` | Unbounded (default) | `newFixedThreadPool`, `newSingleThreadExecutor` | Queue grows without bound → OOM risk under sustained load; max threads = core |
| `SynchronousQueue` | Zero | `newCachedThreadPool` | No buffering; each task must be handed directly to a thread. If none idle, spawns a new thread → thread explosion risk |
| `ArrayBlockingQueue` | Bounded (you set N) | Custom pools | Queue fills → threads grow toward max → rejection. Provides natural backpressure |
| `PriorityBlockingQueue` | Unbounded | Custom pools | Tasks processed by priority (tasks must implement `Comparable` or supply a `Comparator`) |

**Production recommendation:** `ArrayBlockingQueue` with an explicit bound, paired with `CallerRunsPolicy`. This gives backpressure without thread explosion or OOM.

---

## Factory methods and their hidden defaults

### `newFixedThreadPool(n)`

```java
new ThreadPoolExecutor(n, n, 0L, MILLISECONDS, new LinkedBlockingQueue<>())
```

- core = max = n, keepAlive = 0 (no above-core threads to expire)
- **Risk:** unbounded queue — submit faster than threads can process and the queue grows forever → `OutOfMemoryError`

### `newCachedThreadPool()`

```java
new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, SECONDS, new SynchronousQueue<>())
```

- core = 0, max = `Integer.MAX_VALUE`, 60s keepAlive
- **Risk:** every task without an idle thread spawns a new one — up to `Integer.MAX_VALUE` threads → thread explosion under burst load

### `newSingleThreadExecutor()`

```java
new FinalizableDelegatedExecutorService(
    new ThreadPoolExecutor(1, 1, 0L, MILLISECONDS, new LinkedBlockingQueue<>()))
```

- Guarantees serial execution; tasks run in submission order.
- **Risk:** same unbounded queue problem as `newFixedThreadPool`

### `newWorkStealingPool()`

- Backed by `ForkJoinPool` with parallelism = available processors.
- Designed for recursive/divide-and-conquer tasks (`RecursiveTask`, `RecursiveAction`).
- Work stealing: idle threads steal tasks from busy threads' deques.

---

## Rejection policies

Triggered when: queue is full **and** thread count = maximumPoolSize.

| Policy | Behaviour | When to use |
|---|---|---|
| `AbortPolicy` (default) | Throws `RejectedExecutionException` | When callers must know about overload |
| `CallerRunsPolicy` | The **calling thread** runs the task | Natural backpressure — slows down the producer |
| `DiscardPolicy` | Silently drops the task | Fire-and-forget / non-critical work |
| `DiscardOldestPolicy` | Drops the oldest queued task, retries submission | When newer tasks supersede older ones (telemetry, heartbeats) |

`CallerRunsPolicy` is the production default for most services: it applies backpressure to the submitter without data loss or exceptions.

---

## Shutdown

```java
executor.shutdown();            // no new tasks accepted; in-flight + queued tasks complete
executor.shutdownNow();         // interrupts running threads, returns queued tasks as a List
boolean done = executor.awaitTermination(30, TimeUnit.SECONDS);
```

- `shutdown()` is graceful; always prefer it.
- `shutdownNow()` is best-effort — threads that ignore interrupts continue running.
- **Forgetting to shut down leaks threads.** A thread pool keeps the JVM alive until all non-daemon threads finish. In containers/services this causes slow memory leaks and port exhaustion.

**Pattern for safe shutdown:**

```java
executor.shutdown();
if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
    executor.shutdownNow();
}
```

---

## Virtual threads (Java 21)

```java
ExecutorService vte = Executors.newVirtualThreadPerTaskExecutor();
```

- Each submitted task gets its own **virtual thread** — lightweight, JVM-managed, not pinned to an OS thread.
- Blocking a virtual thread (IO, sleep) parks it without blocking the carrier OS thread.
- **When to prefer:** IO-bound workloads (JDBC, HTTP calls, file IO) — eliminates the need to size a pool for concurrency.
- **When NOT to use:** CPU-bound tasks — you still contend on carrier threads. Use `ForkJoinPool` / `newWorkStealingPool` with parallelism = CPU cores.

---

## Quick recall

**Q. What is the thread growth order in ThreadPoolExecutor?**
A. Grow to core → fill queue → grow to max → reject. Queue fills before threads grow beyond core — not the other way around.

**Q. Why is `newFixedThreadPool` dangerous under sustained load?**
A. It uses an unbounded `LinkedBlockingQueue`; tasks pile up without bound and can cause `OutOfMemoryError`.

**Q. Why is `newCachedThreadPool` dangerous under burst load?**
A. `SynchronousQueue` has zero capacity, so every unhandled task spawns a new thread; with `max = Integer.MAX_VALUE`, this can exhaust OS thread limits.

**Q. What does CallerRunsPolicy do and why is it useful?**
A. The submitting thread runs the rejected task itself, which slows down task submission — natural backpressure with no data loss.

**Q. How do you safely shut down a pool?**
A. Call `shutdown()` first (graceful), then `awaitTermination(timeout)`, then `shutdownNow()` if still not done.

**Q. When should you use virtual threads instead of a thread pool?**
A. IO-bound tasks in Java 21+ — virtual threads park on blocking IO without consuming OS threads, so you don't need to tune pool sizes.

**Q. What queue type is recommended for production custom pools and why?**
A. `ArrayBlockingQueue` with a finite bound — it triggers the max-thread and rejection policy paths, giving real backpressure instead of unbounded queuing.
