# Executors, Scheduling, Context Propagation & Virtual Threads

A tour of the six topics from the chat discussion, one section each. Designed as a shared reference for follow-up deep dives.

Related existing docs:
- `async-executor-and-shutdown.md` — why we moved off the single-thread `ThreadPoolTaskScheduler` for `@Async`, full shutdown walkthrough.
- `thread-pools-and-concurrency.md` — per-pool breakdown for this service, `ThreadPoolExecutor` mechanics, sizing rationale.

---

## 1. `ScheduledExecutorService`

Third tier in the executor hierarchy:

```
Executor          →  fire-and-forget: execute(Runnable)
ExecutorService   →  + lifecycle + Future<T> submit + invokeAll/Any
ScheduledExecutorService  →  + schedule(runnable, delay)
                             + scheduleAtFixedRate
                             + scheduleWithFixedDelay
```

### What it buys you

```java
ScheduledExecutorService ses = Executors.newScheduledThreadPool(2);

// Run once after 5 seconds
ses.schedule(() -> log.info("fired"), 5, TimeUnit.SECONDS);

// Run every 10s, measured from each start
ses.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);

// Run every 10s, measured from the end of the last run
ses.scheduleWithFixedDelay(task, 0, 10, TimeUnit.SECONDS);
```

`scheduleAtFixedRate` vs `scheduleWithFixedDelay` is the most common trap:

- **Fixed rate** — if the task takes 15s and the rate is 10s, the next run fires immediately (queued) and you can end up stacking overlapping work. Use when wall-clock cadence matters (metrics snapshots, heartbeats).
- **Fixed delay** — always waits `delay` after the previous run finishes. Safer default — no overlapping runs. Use for most polling and cleanup jobs.

### Why the archiver used to use one

Before the recent refactor, `ApiRequestResponseArchiver` declared:

```java
private final ScheduledExecutorService executorService =
    Executors.newSingleThreadScheduledExecutor();
```

…but never called `schedule*`. It just did `executorService.submit(consumerLoop)` — one permanent consumer running a `while(running)` loop. So `ScheduledExecutorService` was strictly broader than needed; swapping to `ExecutorService` + `newFixedThreadPool(N)` dropped an unused capability **and** enabled multiple consumer threads.

Rule of thumb: don't type a field as `ScheduledExecutorService` unless you actually call a `schedule*` method. It's a wider contract, which limits what you can swap in later.

### Spring's version

`TaskScheduler` is the Spring-flavored equivalent, backed by `ThreadPoolTaskScheduler`. This is what `@Scheduled` cron jobs use. See `async-executor-and-shutdown.md` for why using the same scheduler for `@Async` was the original bug.

---

## 2. `Executors.newXxx` factory methods

Each factory is a pre-baked `ThreadPoolExecutor` or `ScheduledThreadPoolExecutor`. Convenient — but the defaults hide important tradeoffs.

| Factory | Core | Max | Queue | When it's right | When it bites |
|---------|------|-----|-------|-----------------|----------------|
| `newFixedThreadPool(n)` | n | n | unbounded `LinkedBlockingQueue` | Known, bounded concurrency for CPU- or I/O-bound workloads | **Unbounded queue** — memory can balloon under backpressure; `maxPoolSize` never kicks in |
| `newCachedThreadPool()` | 0 | `Integer.MAX_VALUE` | `SynchronousQueue` (no buffering) | Many short-lived tasks, bursty workload | Can spawn thousands of threads under load; almost never the right default in production |
| `newSingleThreadExecutor()` | 1 | 1 | unbounded queue | Strict ordering / single consumer | Any failure that blocks the thread halts all queued work |
| `newScheduledThreadPool(n)` | n | `Integer.MAX_VALUE` | delayed queue | Multiple recurring jobs | Uncaught exceptions in `scheduleAtFixedRate` silently kill the future — wrap each task in try/catch |
| `newSingleThreadScheduledExecutor()` | 1 | 1 | delayed queue | One recurring job | Same silent-death issue |
| `newWorkStealingPool()` | `availableProcessors` | unbounded | per-thread deques | Compute-heavy, many independent tasks | Unordered execution; submission order ≠ start order |
| `newVirtualThreadPerTaskExecutor()` (Java 21) | — | — | no pool | I/O-bound or blocking tasks with high concurrency | Cannot limit concurrency; care with synchronized, pinning (see §6) |

### When to skip the factories

If you need any of these, configure `ThreadPoolExecutor` / `ThreadPoolTaskExecutor` directly:

- A **bounded queue** (so load sheds via `RejectedExecutionHandler` instead of OOM).
- A **custom rejection policy** (drop, caller-runs, log-and-discard).
- A **thread name prefix** (makes logs and thread dumps readable).
- Non-daemon threads, or a custom `ThreadFactory` (e.g., to set `UncaughtExceptionHandler`).

The `taskExecutor` bean documented in `async-executor-and-shutdown.md` is a good example of configuring `ThreadPoolTaskExecutor` directly for exactly these reasons.

---

## 3. Pool sizing & queueing

Deep-dive lives in `thread-pools-and-concurrency.md` (§ "How Thread Pools Work"). Recap:

### `ThreadPoolExecutor` admission order

```
submit(task)
  ├─ pool size < corePoolSize?   → create new thread, run it
  ├─ queue has space?             → enqueue
  ├─ pool size < maxPoolSize?     → create new thread, run it
  └─ else                         → RejectedExecutionHandler
```

Non-obvious consequence: **the queue fills up before `maxPoolSize` kicks in.** `newFixedThreadPool` uses an unbounded queue, so you never grow past `corePoolSize` — `max` is effectively dead config for that factory.

### Sizing heuristics

- **CPU-bound**: `n = cores + 1` (Brian Goetz default).
- **I/O-bound**: `n = cores * (1 + wait_time / service_time)`. For audit writes where wait dominates, this is often `cores * 4` or higher.
- **Real answer**: measure. Throughput, queue depth, and P99 latency under realistic load beat any formula.

### Queue choice

- `LinkedBlockingQueue` unbounded — simple, but no backpressure. Dangerous for producer-faster-than-consumer patterns.
- `LinkedBlockingQueue(capacity)` bounded — pushes back via the rejection handler once full.
- `ArrayBlockingQueue(capacity)` — fixed-size array, slightly faster, same semantics.
- `SynchronousQueue` — no buffering at all; every submit must hand off directly to a thread. Pairs with `newCachedThreadPool`.
- `PriorityBlockingQueue` — scheduled/ranked work.

### Rejection policies

Defaults in `ThreadPoolExecutor`:

- `AbortPolicy` (default) — throws `RejectedExecutionException`.
- `CallerRunsPolicy` — runs the task on the submitting thread. Natural backpressure, but blocks the caller.
- `DiscardPolicy` — silently drops. Almost never right.
- `DiscardOldestPolicy` — drops the oldest queued task, enqueues the new one.

For audit-style fire-and-forget work, `CallerRunsPolicy` is often the safest non-default — the producer naturally slows under overload.

---

## 4. Shutdown semantics

Full walkthrough in `async-executor-and-shutdown.md`. The three methods to know:

| Method | What it does | In-flight tasks | Queued tasks |
|--------|--------------|-----------------|---------------|
| `shutdown()` | Stops accepting new submissions | Continue | Still run |
| `shutdownNow()` | Stops accepting + interrupts workers | Interrupted | Returned as a list; not run |
| `awaitTermination(timeout)` | Blocks until pool is fully terminated or timeout | — | — |

### Graceful shutdown pattern

```java
executor.shutdown();                                    // stop accepting
if (!executor.awaitTermination(30, TimeUnit.SECONDS)) { // drain window
    executor.shutdownNow();                             // force-stop
    executor.awaitTermination(5, TimeUnit.SECONDS);     // mop up
}
```

Spring's `ThreadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true)` wires this up automatically (with `setAwaitTerminationSeconds(...)` as the drain window) — that's the pattern the `taskExecutor` bean uses.

### SIGTERM flow in Kubernetes

1. Pod gets SIGTERM, readiness probe fails, traffic stops.
2. Spring's `ApplicationContext.close()` triggers `@PreDestroy`.
3. Beans shut down in reverse dependency order.
4. If any executor uses `shutdownNow()` (the default `ThreadPoolTaskScheduler` behavior), in-flight `@Async` tasks get `RejectedExecutionException`.
5. `terminationGracePeriodSeconds` (default 30s) is the hard cap — after that, SIGKILL.

The fix in this codebase was setting `waitForTasksToCompleteOnShutdown=true` on the `taskExecutor` bean.

---

## 5. `ContextExecutorService` internals

Micrometer's `context-propagation` library. The wrapper pattern:

```java
ExecutorService wrapped = ContextExecutorService.wrap(
    delegate,                              // the real executor
    contextSnapshotFactory::captureAll);   // Supplier<ContextSnapshot>
```

### What happens on submit

```
caller thread                     worker thread
─────────────                     ─────────────
submit(task)
  │
  ├─ capture = supplier.get()     │
  │   (grab MDC, trace, etc.)     │
  │                               │
  ├─ wrapped = () -> {            │
  │     try (Scope s =            │
  │        capture.setThreadLocals()) {   ← restore on worker
  │         task.run();           │        before running
  │     }                         │        (auto-clears on close)
  │   }                           │
  │                               │
  └─ delegate.submit(wrapped) ────┼──→ run wrapped
                                  │     ├─ setThreadLocals (MDC, trace)
                                  │     ├─ task.run() — logs carry txId
                                  │     └─ close Scope (restore prior)
```

### What `captureAll` captures

The registered `ThreadLocalAccessor`s. Out of the box with Spring Boot 3 and Micrometer Tracing:

- `MDC` (SLF4J) — transaction IDs, correlation IDs, custom fields.
- Micrometer `Observation` context.
- Tracing (Brave / OpenTelemetry) — active span, trace/span IDs.
- `LocaleContextHolder`, `RequestContextHolder` (if Spring Web integration is on the classpath).
- Reactor `Context` (when bridging reactive code).

`ContextRegistry.getInstance()` lists what's currently registered. You can register custom accessors by implementing `ThreadLocalAccessor<T>` — useful for domain-specific thread-locals (e.g., tenant ID).

### Why `::captureAll` is a method reference

`ContextExecutorService.wrap(Executor, Supplier<ContextSnapshot>)` needs a supplier because each task submission needs a *fresh* snapshot of the caller's context. If you passed a single already-captured `ContextSnapshot`, every task would restore the same stale state.

You could also write it as:

```java
ContextSnapshotFactory factory = ContextSnapshotFactory.builder().build();
return ContextExecutorService.wrap(underlying, factory::captureAll);
```

Functionally identical — cleaner if you want to reuse the factory or configure it (e.g., `.clearMissing(true)` to clear thread-locals not present in the snapshot rather than leaving the worker's prior values in place).

### The gotcha

Wrapping only helps for tasks submitted through the wrapper. If something grabs the underlying `delegate` (or a task internally uses `ForkJoinPool.commonPool()` or `CompletableFuture.supplyAsync()` without specifying an executor), context propagation is lost. Spring's `ContextPropagatingTaskDecorator` works at the `TaskExecutor` level with the same underlying mechanism.

---

## 6. Virtual threads (Java 21)

### What they are

JEP 444 / "Project Loom". Lightweight threads managed by the JVM, not the OS.

- A platform thread maps 1:1 to an OS thread; stack is ~1 MB; OS schedules it.
- A virtual thread runs on top of a small pool of platform threads ("carriers"); stack is a few KB; JVM parks and resumes it when it blocks.

You can have millions of virtual threads; platform threads top out in the low thousands.

### What they change

The classic trade "blocking code is simple but thread-per-request doesn't scale" stops being a trade. You write blocking code, you get async-like scale.

```java
// Old: thread pool to limit concurrency for I/O-bound work
ExecutorService pool = Executors.newFixedThreadPool(200);

// New: one virtual thread per task, no pool
ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
```

Or directly:

```java
Thread.ofVirtual().start(() -> doIo());
```

### What they don't change

- CPU-bound work still needs bounded parallelism — a virtual thread doing matrix math still needs a carrier OS thread.
- Thread-locals work, but are per-virtual-thread. If you had `ThreadLocal` caches sized for "20 platform threads," a million virtual threads means a million copies. Use scoped values (JEP 429, preview) or shared caches.
- `synchronized` blocks can **pin** a virtual thread to its carrier (it can't unmount while holding the monitor). Long-held synchronized blocks destroy the scalability benefit. Replace with `ReentrantLock` where possible. (JDK 24 removes most of this pinning; check your JDK version.)

### Implications for this codebase

- **Stage executor** (`traceableExecutorService`, 16 fixed threads running I/O-bound Feign calls) is a prime candidate. Switching to a virtual-thread-per-task executor could drop the "16 is not enough during bursts" problem entirely.
- **S3 archiver** (4 fixed consumers + bounded queue) is a bad candidate — the fixed pool *is* the concurrency limit, which is the point. Removing it loses backpressure.
- **`@Async` audit** (4 core, 8 max) could go either way. The bounded queue gives you OOM protection; virtual threads give you more flexibility. Measure.
- **Tracing/MDC**: context-propagation library already supports virtual threads; no code changes for MDC. But double-check that `ContextExecutorService.wrap(...)` works over a virtual-thread-per-task executor — it does, since the wrapper operates at submit time and is executor-agnostic.

### Spring Boot switch

`spring.threads.virtual.enabled=true` in Spring Boot 3.2+ makes:

- Tomcat/Undertow worker threads virtual.
- `@Async` default executor virtual.
- `ThreadPoolTaskExecutor` still physical (explicit config wins).

Not a drop-in for everything — benchmark first, especially for anything using `synchronized` heavily or with thread-pool-based rate limiting semantics.

---

## Follow-up topics (not yet expanded)

- Rejection policy trade-offs under sustained vs. spiky load.
- `CompletableFuture` chain execution — which executor runs `thenApply` vs. `thenApplyAsync`.
- ForkJoinPool vs. ThreadPoolExecutor for stage fan-out.
- Deciding between `@Async`, direct `CompletableFuture`, and reactive for new work in this service.
