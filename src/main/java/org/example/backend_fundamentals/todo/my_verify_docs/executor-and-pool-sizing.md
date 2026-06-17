# Executor, ExecutorService & Pool Sizing

Deep-dive into `Executor` / `ExecutorService`, the thread-vs-pool distinction, and how to pick a pool size. Companion to:
- `executor-deep-dive.md` — scheduling, context propagation, virtual threads.
- `thread-pools-and-concurrency.md` — pool inventory, monitoring, thread safety.
- `async-executor-and-shutdown.md` — shutdown flow for `@Async` executors.

---

## 1. Thread vs Pool — the core distinction

### A thread

An **OS-level execution context**. Has:
- Its own stack (~1 MB default on 64-bit Linux).
- Its own program counter and register state.
- A kernel thread ID.
- A state: `RUNNABLE`, `WAITING`, `BLOCKED`, `TIMED_WAITING`, `TERMINATED`.

Creating a thread requires a **syscall** to the kernel (`clone()` on Linux). Cost: ~50–100 microseconds for the syscall itself, plus ~1 MB of memory. Once created, a thread can run **one `Runnable` at a time**. When that `run()` method returns, if nothing else keeps the thread alive, it terminates.

```java
Thread t = new Thread(() -> doWork());
t.start();    // OS creates the thread, runs doWork()
// when doWork() returns, t is dead permanently
```

### A pool

A **Java-level object that manages a group of threads**. It is not itself a thread — it's a container. A pool:
- Holds N long-lived worker threads (each is a regular OS thread).
- Owns a task queue.
- Exposes `submit(task)` for producers to drop tasks into the queue.
- Keeps each worker parked in a loop that pulls tasks off the queue and runs them.

```
                    ┌─────────── Pool ───────────┐
                    │                            │
  submit(t1) ─────→ │   Task Queue  [t4][t5]     │
  submit(t2) ─────→ │        │                   │
  submit(t3) ─────→ │        ├─→ Worker Thread 1 │──→ runs t1, t4, t7...
                    │        ├─→ Worker Thread 2 │──→ runs t2, t5, t8...
                    │        └─→ Worker Thread 3 │──→ runs t3, t6, t9...
                    │                            │
                    └────────────────────────────┘
```

Invariant: **one worker thread runs many tasks over its lifetime.** It does not die after each task — it loops back to the queue.

### Why pools exist

Without a pool, 100 requests/second = 100 threads created and destroyed per second, ~10 ms/sec of pure kernel overhead, plus GC of dead `Thread` objects. A traffic spike of 1000 requests = 1000 OS threads × 1 MB stack = 1 GB of memory vanished, and the kernel scheduler thrashes time-slicing them all.

Pools solve both:
- **Amortized creation**: create N threads once, reuse forever.
- **Bounded concurrency**: at most N tasks run simultaneously; the rest queue.

---

## 2. The `Executor` interface — minimal by design

```java
public interface Executor {
    void execute(Runnable command);
}
```

One method. The entire contract: *"I will run this `Runnable` at some point, on some thread, which is my problem."*

What it **doesn't** say:
- *When* — could be immediate, could be later.
- *Where* — could be a new thread, a pooled thread, the caller's thread.
- *How many* — could be serial, could be parallel.

This decoupling means you can swap implementations without touching callers:

```java
// Direct — run on the calling thread
Executor direct = Runnable::run;

// New thread per task
Executor perTask = task -> new Thread(task).start();

// Thread pool
Executor pool = Executors.newFixedThreadPool(4);

// Queue for later processing
List<Runnable> q = new ArrayList<>();
Executor deferred = q::add;
```

All four satisfy `Executor`. Methods that take `Executor` cannot distinguish them.

### What `Executor` can't do

The interface is intentionally too minimal for real work:
- No return value (`Runnable.run()` is `void`).
- No way to know when a task finished.
- No way to cancel.
- No way to shut down.

That's why `ExecutorService` exists.

---

## 3. `ExecutorService` — adds lifecycle and results

```java
public interface ExecutorService extends Executor {
    // Submit — returns a Future you can observe
    <T> Future<T> submit(Callable<T> task);
    Future<?>    submit(Runnable task);
    <T> Future<T> submit(Runnable task, T result);

    // Bulk
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks);
    <T> T invokeAny(Collection<? extends Callable<T>> tasks);

    // Lifecycle — absent from plain Executor
    void shutdown();
    List<Runnable> shutdownNow();
    boolean awaitTermination(long timeout, TimeUnit unit);
    boolean isShutdown();
    boolean isTerminated();
}
```

### 3a. `Future<T>` — a handle to the result

```java
Future<Integer> f = executor.submit(() -> computeSomething());
int result              = f.get();              // block until done
int resultWithTimeout   = f.get(5, SECONDS);    // block up to 5s
boolean cancelled       = f.cancel(true);       // try to interrupt
boolean done            = f.isDone();           // non-blocking poll
```

Under the hood: `submit()` wraps the `Callable` in a `FutureTask` — an object that implements **both** `Runnable` (so a worker can run it) and `Future` (so you can observe it). When the worker runs the `FutureTask`, it stores the return value inside itself and wakes anything blocked on `.get()`. This dual nature is the hinge that lets a queue of `Runnable`s deliver typed results.

### 3b. Lifecycle

- **`shutdown()`** — stop accepting submissions. In-flight and queued tasks finish.
- **`shutdownNow()`** — stop accepting, interrupt running workers, return unrun queue.
- **`awaitTermination(timeout, unit)`** — block until the pool is fully terminated or timeout.

Graceful shutdown:

```java
executor.shutdown();
if (!executor.awaitTermination(30, SECONDS)) {
    executor.shutdownNow();
    executor.awaitTermination(5, SECONDS);
}
```

Spring's `ThreadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true)` wires this automatically on context close. See `async-executor-and-shutdown.md`.

### The inheritance chain

```
Executor                       "run this Runnable"
   ↑ extends
ExecutorService                "+ Future results, + lifecycle"
   ↑ extends
ScheduledExecutorService       "+ schedule(task, delay), + scheduleAtFixedRate"
```

**Rule**: type your variable at the narrowest level you need. If callers only `execute()` and never inspect results, type the field `Executor` — you keep the option to swap in a virtual-thread-per-task executor, a direct executor for tests, etc. In this codebase, `traceableExecutorService` is typed `ExecutorService` because the stage code calls `submit(...)` and needs `Future`s.

---

## 4. How a pool actually works internally

### The worker loop

Every worker in a `ThreadPoolExecutor` runs (simplified from the JDK source):

```java
void workerLoop() {
    while (poolIsRunning) {
        Runnable task = queue.take();   // BLOCKS until a task arrives
        try {
            task.run();
        } catch (Throwable t) {
            // log; do not die
        }
    }
}
```

So `Executors.newFixedThreadPool(16)` creates in memory:
- 1 `ThreadPoolExecutor` object.
- 1 `LinkedBlockingQueue` (the task queue).
- 16 `Thread` objects, each running `workerLoop()`.

The 16 threads are **always alive**. They don't get created when you submit — they already exist, parked in the blocking `queue.take()` call. When `submit(task)` enqueues, one of the 16 waiting `take()` calls returns, hands the task to its worker, and runs it.

### Walking through a submit

```java
executor.submit(() -> doWork());
```

1. Submitter (e.g., an Undertow worker) calls `submit`.
2. `submit` wraps the lambda in a `FutureTask`, calls `execute(futureTask)`.
3. `execute` runs the admission ladder:
   - Pool size < `corePoolSize`? Create a new worker thread, hand it the task directly.
   - Queue has space? Add to queue.
   - Pool size < `maxPoolSize`? Create a new worker thread, hand it the task.
   - Otherwise? `RejectedExecutionHandler`.
4. Submitter returns with a `Future`, goes on with its life.
5. Some worker's `queue.take()` returns with the task; worker runs `task.run()`.
6. When `run()` returns, the worker loops back to `queue.take()` and parks again.

**What happens to a worker between tasks**: `WAITING` state, parked in `queue.take()`, consuming essentially zero CPU. The thread is alive but the kernel has nothing to schedule on a CPU for it. The kernel wakes it when the queue has something.

### "Does the executor hold threads forever?"

Yes — and this is the whole point of pooling.

**Core threads** are created lazily (as submissions arrive) but once created are **not recycled** per task. They live for the pool's lifetime. Exception: if you call `allowCoreThreadTimeOut(true)`, idle core threads die after `keepAliveTime` — useful for pools that are often idle.

**Non-core threads** (created when queue is full and pool < max) die after `keepAliveTime` of idleness. The pool shrinks back to core during quiet periods.

Cost of holding idle threads:
- **Memory per thread** — ~1 MB stack. 16-thread pool ≈ 16 MB.
- **CPU per idle thread** — near zero. The thread is parked.
- **Scheduling overhead per idle thread** — zero. The kernel only schedules runnable threads onto CPUs.

**Keeping 16 idle threads is cheap.** What is expensive is having 16 threads *all trying to run simultaneously* on a 4-core machine — that's when CPU contention and context switching start hurting.

---

## 5. CPU cores, logical CPUs, and what "parallel" really means

Before we size pools, we need to be clear what "parallel execution" actually costs.

### Physical core vs logical CPU (hyperthreading)

- A **physical core** is a hardware unit that executes instructions. Has its own ALU, its own L1/L2 cache.
- A **logical CPU** is what the OS sees as a schedulable unit. With **hyperthreading** (Intel SMT, AMD SMT), one physical core exposes 2 logical CPUs — they share the execution units but have separate register sets, so the core can switch between them cheaply when one stalls on memory.

`Runtime.getRuntime().availableProcessors()` returns the number of **logical CPUs**. In containers (Docker, Kubernetes), it returns the CPU limit set by the container runtime — usually fractional, rounded up.

**Rule of thumb for CPU-bound work**: two hyperthreads on one core ≠ two cores. Under hyperthreading, the second logical CPU gives you maybe 20–30% extra throughput for compute, not 100%. For CPU sizing, treat hyperthreads as ~1.2x, not 2x.

### Context switching cost

When the OS scheduler switches a core from running thread A to running thread B:
- Saves A's registers (~100 bytes).
- Loads B's registers.
- Flushes or partially flushes the TLB (virtual memory lookups).
- Cold-start penalties in L1/L2 cache as B's working set isn't there anymore.

Direct cost: ~1–10 microseconds per switch. Real cost (cache effects): often 10–100× more for memory-intensive workloads. Measurable with `perf stat` showing `context-switches` and `cache-misses`.

**Implication**: running 100 CPU-bound threads on 8 cores doesn't speed things up. You get 92 threads waiting for CPU time at any moment, and the 8 that are running lose significant cache locality every time they're preempted. You're paying context-switching cost without getting more work done.

This is why the CPU-bound formula is `cores + 1`, not "as many as you can."

### The I/O case is different

If a thread is blocked on a network read, it's in `WAITING` state — the kernel does not schedule it onto a CPU. 100 I/O-bound threads on 8 cores is fine, because at any moment maybe 3 are actually runnable and the other 97 are parked waiting for bytes.

That's why I/O-bound pools can be much larger than core count.

---

## 6. Pool sizing — the decision framework

### Step 1: Classify the workload

| Workload | Signs | Example in this service |
|---|---|---|
| **CPU-bound** | Thread runs hot, `top` shows ~100% CPU per thread, no blocking I/O | Rare. Would be things like big-image resize, heavy JSON processing, crypto. |
| **I/O-bound** | Thread spends most time blocked in `read()`, `take()`, `Future.get()` | Almost everything: Feign calls to VIDA/ASG, DB reads/writes, S3 puts. |
| **Mixed** | Both matter | Stage that does a Feign call then parses a big payload. Treat by dominant component. |

### Step 2a: CPU-bound formula

```
threads = cores + 1
```

Brian Goetz's *Java Concurrency in Practice* default. One thread per core keeps the CPU saturated; the +1 covers the occasional stall (cache miss, page fault) so you don't waste a core cycle.

Going higher hurts: more threads than cores means preemption, which costs context switches and cache-cold penalties. You spend more CPU on bookkeeping, less on work.

Going lower leaves cycles on the floor: fewer threads than cores means some cores are idle even when work is available.

### Step 2b: I/O-bound formula — Little's Law

The foundational formula. For a system in steady state:

```
L = λ × W
```

- `L` = average number of tasks simultaneously in the system (concurrency).
- `λ` = arrival rate (tasks per second).
- `W` = average time each task spends in the system (latency).

Re-arranged for pool sizing:

```
threads_needed = throughput × latency_per_task
```

Example — the stage executor:
- Target: 20 concurrent requests × 3 stages each = 60 stage executions in flight.
- Stage latency: ~500 ms average.
- Threads needed: 60 × 0.5 = **30**.

Alternative form using wait/service ratio:

```
threads = cores × (1 + wait_time / service_time)
```

- `service_time` = CPU time the task actually uses.
- `wait_time` = time the task spends blocked (network, disk, DB).

Example — a typical Feign call:
- Network round trip (`wait_time`): ~450 ms.
- JSON marshaling + response handling (`service_time`): ~50 ms.
- Ratio: 450 / 50 = 9.
- On 8 cores: `8 × (1 + 9) = 80 threads`.

Both formulas agree in principle. The Little's-Law form is easier when you know target throughput and latency; the wait/service form is easier when you've profiled a task and know its blocking fraction.

### Step 2c: Measuring wait_time / service_time

You can't size I/O-bound pools without knowing this ratio. Three ways to measure:

**1. Profile with Java Flight Recorder (JFR)** — lowest overhead (<2%), production-safe.
```bash
jcmd <pid> JFR.start duration=60s filename=profile.jfr
# view in JDK Mission Control
```
Look at "Thread Activity" — time spent in `RUNNABLE` vs `WAITING`/`TIMED_WAITING`. That ratio is `service_time : wait_time`.

**2. Approximate from metrics** — in this service, Micrometer emits per-stage timers. Compare:
- Stage latency P50 (total wall time).
- CPU time contribution (harder to get directly; approximate via `jstack` sampling or `async-profiler` in CPU mode).

**3. Reason from architecture** — for a stage that makes a Feign call with ~50 ms of parsing on top of a ~500 ms HTTP round trip, `wait/service ≈ 9`.

For most stages in this service, `wait/service` is 5–20. This is why the 16-thread pool is roughly right on 4–8 cores.

### Step 3: Cap by downstream resources

Pool size is always bounded by the smallest downstream bottleneck:

```
Thread pool (N threads)
    ↓ each thread may need
DB connections (HikariCP default: 10)       ← hard cap ≈ 10 × 1.5 for DB work
External service rate limit (e.g. 30 rps)   ← depends on task duration
Memory / heap size                          ← 1 MB per thread stack
Network I/O, disk I/O                       ← soft caps
```

**Hard rule**: `practical_threads ≤ downstream_pool_size × small_factor` (typically 1.5 — allows some threads to be mid-task while others hold a connection).

For DB-heavy work with HikariCP=10:
```
practical_threads = min(little's_law_result, 10 × 1.5) = min(30, 15) = 15
```

Above this cap, extra threads just wait on `HikariPool — Connection is not available`. Throughput stops improving. P99 latency gets *worse* because threads now pay the acquisition timeout before failing.

**Corollary**: thread pool size and connection pool size must be tuned **together**. Bumping threads from 16 → 32 without bumping HikariCP from 10 → 20 gains nothing and loses tail latency.

### Step 4: Measure in production

The formulas give a starting point. The actual optimum depends on real traffic patterns, network latency distribution, and downstream behavior. Once deployed:

| Metric | Healthy | Unhealthy signal |
|---|---|---|
| `executor.active / executor.pool.size` | Fluctuates, rarely saturated | Pinned at 100% → undersized (or slow downstream) |
| `executor.queued` | ~0 most of the time | Consistently > 0 → undersized or downstream slow |
| `executor.completed` rate | Steady, tracks throughput | Plateaus while load rises → bottleneck reached |
| `hikaricp.connections.pending` | 0 | > 0 → DB pool is the bottleneck; more threads won't help |
| `hikaricp.connections.timeout` | 0 | > 0 → critical; requests failing on connection acquisition |
| P99 request latency | Stable under load | Climbs linearly with load → saturation starting |

Tune iteratively. Each change, watch these for a day.

---

## 7. Concrete sizing examples from this service

### Example A — stage executor (16 fixed threads)

```java
Executors.newFixedThreadPool(16)   // wrapped by ContextExecutorService
```

Workload: I/O-bound (Feign + DB).
Peak concurrency: ~20 concurrent requests × 3 stages = ~60 tasks in flight.
Stage latency: 200–1500 ms.

Little's Law suggests ~30. HikariCP cap (10 × 1.5) forces ≤ 15. Pool set to 16 — within tolerance, leaves headroom. Queue is unbounded (`LinkedBlockingQueue`), so the 61st concurrent task queues rather than rejecting.

**If traffic doubled**: bump to 24 and HikariCP to 15 together. Don't move one without the other.

**Why not virtual threads**: 16 works fine today; virtual threads would remove the "16 is not enough during spikes" risk but adds unknowns around `synchronized` pinning. Measure before switching. See `executor-deep-dive.md` §6.

### Example B — async audit (4 core, 8 max, queue 100)

```java
executor.setCorePoolSize(4);
executor.setMaxPoolSize(8);
executor.setQueueCapacity(100);
```

Workload: I/O-bound (DB writes, small payloads).
Concurrency: one audit per request, short transactions, no fan-out.
Latency: ~200–500 ms.

Why 4 core / 8 max: normal load needs ~4 threads (Little's Law: 8 req/sec × 0.5 sec = 4). Bursts absorb into the 100-slot queue; beyond that the pool grows to 8. Beyond 108 pending → reject.

This is the only pool that uses the full core → queue → max pattern. Most others are fixed because load is predictable.

### Example C — S3 archiver (4 fixed permanent consumers)

The threads **never return to the pool** — each runs `while (running) { writeQueue.take(); uploadToS3(); }` for the app's lifetime.

So the "pool" here is a thread host, not a dispatcher. Actual concurrency is controlled by the archiver's own bounded `writeQueue` (producers `offer(...)` and get backpressure if full).

Sizing question: how many parallel S3 uploads without overwhelming S3 or the network? 4 is enough for this service's traffic — measured via `executor.queued` staying near zero.

**Key rule**: permanent consumers must never share a pool with short-lived tasks. If the 4 S3 consumers ran on `taskExecutor` (4 core / 8 max), they'd permanently occupy 4 of 8 max threads, and `@Async` audit would starve.

### Example D — what a CPU-bound pool would look like

Hypothetical: a batch job doing image resizing.

Machine: 8 logical CPUs (4 physical cores + HT).
Task: pure CPU — JPEG decode + resize + encode, ~80 ms each.
Arrival rate: queue drained as fast as possible.

Sizing:
- Goetz formula: `cores + 1 = 9` threads.
- Going to 16 gives no throughput improvement (CPU is the bottleneck) and adds context switching.
- Going to 4 leaves 4 cores idle.

For this kind of work, `newWorkStealingPool(8)` or `ForkJoinPool(8)` with a `WorkStealing` queue can outperform `newFixedThreadPool` under variable task sizes.

---

## 8. Queue sizing and rejection — the other half of pool design

Thread count is only half the picture. The queue is the buffer between producers and workers.

### Queue types

| Queue | Behavior | Use when |
|---|---|---|
| `LinkedBlockingQueue()` (unbounded) | Infinite buffer | Known upper bound on task rate; simple fire-and-forget |
| `LinkedBlockingQueue(capacity)` | Bounded FIFO | You want backpressure via rejection |
| `ArrayBlockingQueue(capacity)` | Bounded FIFO, array-backed (slightly faster) | Fixed-capacity queues with low allocation |
| `SynchronousQueue` | Zero capacity — every put must match a take | `newCachedThreadPool` — forces immediate thread handoff |
| `PriorityBlockingQueue` | Ordered by comparator | Priority tasks |

### The admission ladder, in detail

```
submit(task)
  │
  ├─ pool size < corePoolSize?   → create a new worker, hand task to it
  │
  ├─ offer to queue succeeds?    → task queued; a worker will pick it up
  │
  ├─ pool size < maxPoolSize?    → create a new worker, hand task to it
  │
  └─ else                        → RejectedExecutionHandler kicks in
```

Non-obvious consequence: **the queue fills up before `maxPoolSize` is reached.** So with `newFixedThreadPool(n)` (unbounded queue), the pool never exceeds `corePoolSize` — `maxPoolSize` is effectively dead config.

If you want the pool to grow past core under load, you **must** bound the queue. Otherwise tasks queue forever and the extra-capacity behavior never triggers.

### Rejection policies

When the queue is full and pool is at max, the `RejectedExecutionHandler` decides what to do:

| Policy | Behavior | Use when |
|---|---|---|
| `AbortPolicy` (default) | Throw `RejectedExecutionException` | Client can retry or fail fast |
| `CallerRunsPolicy` | Run the task on the submitting thread | Natural backpressure — producer slows down |
| `DiscardPolicy` | Silently drop the task | Almost never right |
| `DiscardOldestPolicy` | Drop oldest queued task, enqueue new | Replace-if-newer semantics |
| Custom | Anything (log + drop, dead-letter queue, etc.) | Domain-specific |

For audit-style fire-and-forget, `CallerRunsPolicy` is often safest — the producer can't overwhelm the pool because if the pool is saturated, the producer runs the task itself and naturally slows down.

### Queue sizing guidance

- **Latency-sensitive, bursty traffic**: small queue, `maxPoolSize > corePoolSize`. Bursts spill to new threads rather than waiting in the queue.
- **Throughput-sensitive, steady traffic**: larger queue, `maxPoolSize = corePoolSize`. Smooth out variance.
- **Memory-constrained**: bound the queue at an explicit size. Unbounded queues can OOM under slow downstream.

The audit pool's queue=100 / core=4 / max=8 says: "tolerate 100 queued during normal bursts; if the queue fills and load is still coming, grow to 8 threads."

---

## 9. Common pitfalls and anti-patterns

### Pitfall 1: Unbounded `newFixedThreadPool` in production

```java
Executors.newFixedThreadPool(16)
```

Queue is unbounded. Under sustained overload, it grows without limit, OOM eventually. Add bounded `ArrayBlockingQueue` + `CallerRunsPolicy`:

```java
new ThreadPoolExecutor(
    16, 16,
    0L, TimeUnit.MILLISECONDS,
    new ArrayBlockingQueue<>(1000),
    new ThreadPoolExecutor.CallerRunsPolicy());
```

### Pitfall 2: Mixing short-lived tasks with permanent consumers

As in the S3 archiver case above — if permanent consumers share the `taskExecutor`, they permanently occupy slots and short-lived tasks starve. Always give permanent consumers their own executor.

### Pitfall 3: Treating thread count independently from connection pool

Adding threads to "go faster" without bumping HikariCP = threads wait on `Connection is not available`, P99 climbs, nothing gained.

### Pitfall 4: CPU-bound work on an I/O-bound pool

If a stage suddenly starts doing heavy JSON parsing or image work on the 16-thread I/O pool, you're running 16 CPU-bound threads on ~4 cores → context-switch thrash. Either keep the pool small, or route CPU-heavy work to a separate `cores+1` pool.

### Pitfall 5: Context-switch thrashing from oversized pools

Symptom: CPU usage is high but throughput is flat or falling. `perf stat` shows high `context-switches` and `cache-misses`. Fix: shrink the pool. More threads is not always faster.

### Pitfall 6: Ignoring `maxPoolSize` with an unbounded queue

`ThreadPoolExecutor(4, 32, ..., new LinkedBlockingQueue<>())` — the 32 is a lie. The queue never rejects, so the pool never grows past 4. Always pair `max > core` with a **bounded** queue.

### Pitfall 7: `ForkJoinPool.commonPool()` under the hood

`CompletableFuture.supplyAsync(task)` (no executor arg) runs on the common pool — shared JVM-wide, tiny (`cores - 1`), with no MDC propagation. In Spring Boot services, always pass an explicit executor.

### Pitfall 8: Leaking thread-locals across tasks

Pooled threads are reused. A thread-local set during task A is still there when task B runs on the same thread. Either use `ContextExecutorService.wrap(...)` (this codebase's pattern), or clear thread-locals in a try/finally around each task.

---

## 10. A decision tree for sizing

```
Is the task CPU-bound?
│
├─ YES → threads = cores + 1
│        • Match to physical cores if hyperthreading doesn't help your workload.
│        • Use newWorkStealingPool or ForkJoinPool for variable-sized tasks.
│        • Don't go above cores × 2 — context switching eats the gains.
│
└─ NO → I/O-bound:
        │
        ├─ Compute Little's Law:
        │     threads = target_throughput × avg_latency_per_task
        │
        ├─ Or wait/service:
        │     threads = cores × (1 + wait_time / service_time)
        │
        ├─ Cap by downstream:
        │     threads ≤ min(DB_pool × 1.5, external_rate_limit × avg_latency, ...)
        │
        ├─ Pick queue:
        │     • Bounded (ArrayBlockingQueue) + rejection policy for backpressure
        │     • Unbounded only for small, trusted task volume
        │
        └─ Deploy, measure, adjust:
              • executor.queued near 0 → size is right or oversized
              • executor.queued consistently > 0 → undersized or downstream slow
              • hikaricp.pending > 0 → DB is bottleneck, don't add threads
              • P99 stable under load → healthy
```

### Quick reference table

| Scenario | Typical pool size |
|---|---|
| Pure CPU (image encode, compression) | `cores + 1` |
| Light I/O, compute-heavy (JSON + small HTTP) | `cores × 2` to `cores × 4` |
| Heavy I/O (Feign calls, DB reads) | `cores × 4` to `cores × 20` — capped by downstream |
| DB-bound writes | `HikariCP_size × 1.5` (~15 if pool is 10) |
| Permanent consumers (archiver) | Just enough to drain at peak (2–8) |
| Background maintenance jobs | 1–2 |
| Thread-per-request virtual threads | unlimited — no pool |

### Absolute limits to keep in mind

- **Per-thread memory**: ~1 MB stack × N threads. 1000 threads = 1 GB heap just for stacks. Configure with `-Xss256k` to shrink if you know your stack depth is small.
- **Linux PID/thread limit**: typically 4M+ per system, but process-level `ulimit -u` may be much lower (often 4096). `/proc/sys/kernel/threads-max` shows the kernel limit.
- **JVM internal pools**: GC threads, JIT compiler threads, Netty event loops all coexist. Count them too.
- **Container CPU limits**: in Kubernetes with a CPU limit of 2, `availableProcessors()` returns 2 — `cores + 1` means 3, not the node's 64.

---

## 11. Benchmarking methodology — how to actually know

Formulas are a starting point. To find the real optimum:

1. **Baseline**: current pool size, realistic load generator (wrk, JMeter, k6), 5-minute run.
2. **Record**: throughput (req/sec), P50/P95/P99 latency, `executor.active`, `executor.queued`, `hikaricp.pending`, CPU%, context-switches/sec, GC pauses.
3. **Sweep**: run the same load at pool sizes `N/2`, `N`, `N×2`, `N×4`.
4. **Plot**: throughput vs pool size. There's usually a curve: throughput climbs, plateaus, then falls (or stays flat as pool size grows past the downstream bottleneck).
5. **Pick the knee**: the smallest pool size that gives you ~95% of peak throughput. Smaller is better — less memory, less contention, more headroom.

Tools:
- `perf stat -e context-switches,cache-misses <pid>` — catches context-switch thrash.
- `async-profiler` — flame graphs showing where threads actually spend time.
- `jcmd <pid> Thread.print` — snapshot of thread states for debugging starvation.
- `jstack -l <pid>` — thread dump with lock info.
- Spring Boot Actuator `/actuator/metrics/executor.active` — live pool metrics.

---

## 12. Mental model in one page

**A thread** is an OS primitive that runs one `Runnable` at a time. Creating it costs ~100 μs and ~1 MB; once `run()` returns, it dies.

**A pool** is a Java object owning N long-lived worker threads. Each worker runs `while(true) { queue.take().run(); }` — so a thread runs many tasks over its lifetime. The pool amortizes thread creation and bounds concurrency.

**`Executor`** is the minimal "run this Runnable" interface. **`ExecutorService`** adds `Future` results and lifecycle. **`ScheduledExecutorService`** adds delayed/periodic scheduling.

**Pool sizing**:
- CPU-bound: `cores + 1`.
- I/O-bound: Little's Law — `threads = throughput × latency`, or equivalently `cores × (1 + wait/service)`.
- Always cap by downstream bottleneck (especially DB connection pool).
- Queue is the other half: bound it for backpressure; unbounded only when you know the rate.
- Thread count and connection pool size must move together.
- Measure in production — the formulas are a starting point, not a destination.

**The point of "threads live forever"**: that's what makes pools faster than per-task threads. The cost of idle threads is nearly zero (~1 MB memory each, no CPU). The cost of *oversized* pools comes when too many threads try to run at once, not when they sit idle.
