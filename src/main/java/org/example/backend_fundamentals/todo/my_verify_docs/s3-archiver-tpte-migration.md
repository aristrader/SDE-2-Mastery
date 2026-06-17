# S3 Archiver Refactor — Discussion & Learning

A complete record of the discussion, analysis, and decisions behind migrating `ApiRequestResponseArchiver` from a hand-rolled queue + worker pool to a Spring-managed `ThreadPoolTaskExecutor`. Intended as a reference for future work on this component and as a learning resource for the concepts involved.

## Table of contents

1. [Context and the reviewer comment](#1-context-and-the-reviewer-comment)
2. [The original code — what it looked like](#2-the-original-code--what-it-looked-like)
3. [The 16 issues we identified](#3-the-16-issues-we-identified)
4. [Decision 1 — Keep a separate pool, or collapse into `taskExecutor`?](#4-decision-1--keep-a-separate-pool-or-collapse-into-taskexecutor)
5. [Decision 2 — `ThreadPoolTaskExecutor` or virtual threads?](#5-decision-2--threadpooltaskexecutor-or-virtual-threads)
6. [Virtual threads — deep dive](#6-virtual-threads--deep-dive)
7. [Pinning — what it is, why it matters, how to detect it](#7-pinning--what-it-is-why-it-matters-how-to-detect-it)
8. [Load analysis and the final call](#8-load-analysis-and-the-final-call)
9. [What TPTE actually fixes, issue by issue](#9-what-tpte-actually-fixes-issue-by-issue)
10. [Policy decisions](#10-policy-decisions)
11. [The implementation](#11-the-implementation)
12. [Still-open work (post-refactor)](#12-still-open-work-post-refactor)
13. [PR reply](#13-pr-reply)
14. [Key concepts worth remembering](#14-key-concepts-worth-remembering)

---

## 1. Context and the reviewer comment

`ApiRequestResponseArchiver` is the component that writes audit copies of incoming verification requests and outgoing responses to S3. It sits one hop downstream of the `@Async`-annotated methods in `Auditing`, so by the time it runs, the HTTP request thread has long since returned to the user.

The reviewer comment on the original code was:

> can we improve the submission logic. If we are using JDK21+ we can use virtual threads as well

Two suggestions bundled into one: (a) improve the submission logic, (b) consider virtual threads as the implementation. We addressed both — adopting a better submission pattern, and deciding separately whether virtual threads were the right way to get there.

---

## 2. The original code — what it looked like

The archiver maintained its own queue and worker pool by hand:

```java
private final LinkedBlockingQueue<Runnable> writeQueue;         // bounded, 100
private final ExecutorService executorService;                  // fixed pool of 4
private boolean running;                                        // non-volatile stop flag

// worker pattern — N long-lived Runnables looping on the queue
private void startWriteExecutor() {
  this.running = true;
  for (int i = 0; i < consumerThreads; i++) {
    this.executorService.submit(() -> {
      while (running) {
        Runnable r;
        try {
          r = writeQueue.take();   // blocks until task available
          r.run();
        } catch (InterruptedException e) {
          log.error("Interrupted while waiting for an S3 archive task", e);
        } catch (Exception e) {
          log.error("Encountered an unexpected exception", e);
        }
      }
    });
  }
}

// submission
private void submitArchiveTask(Runnable archiveTask) {
  if (writeQueue.offer(archiveTask)) { ... }
  else {
    try { writeQueue.put(archiveTask); }   // blocks caller
    catch (InterruptedException e) { /* swallowed */ }
  }
}

// shutdown
@PreDestroy
public void shutdown() {
  this.running = false;
  this.executorService.shutdown();          // doesn't stop workers
}
```

Two loosely coupled pieces: a bounded queue (used directly by producer and consumer) and an executor that just hosts the 4 infinite worker loops. The executor's own task scheduler is unused; all work flows through the queue.

---

## 3. The 16 issues we identified

### Correctness / data-safety bugs

1. **Offer-then-put blocks the caller on a full queue.** `put()` waits indefinitely for queue space when full, parking the HTTP-upstream thread under S3 pressure — the exact coupling the queue was supposed to prevent.
2. **No explicit "queue full" policy.** The code half-commits to blocking; doesn't make a deliberate choice between drop, caller-runs, or block.
3. **Producer `InterruptedException` swallowed.** Catch block logs and returns — task silently lost, interrupt flag erased so upstream can't react to shutdown.
4. **Consumer `InterruptedException` swallowed.** Same shape in the worker loop. Interrupts meant to stop a worker are absorbed; loop continues.
5. **`running` flag is not `volatile`.** Written by shutdown thread, read by worker threads — no happens-before guarantee; workers may cache the field indefinitely.
6. **Workers parked in `take()` never see `running = false`.** The flag is only checked between iterations; a thread blocked inside `take()` doesn't evaluate it.
7. **`executorService.shutdown()` is the wrong shutdown.** It means "stop accepting new submissions; let running tasks finish." The running tasks are the infinite worker loops — they never finish. No new submissions are happening anyway. Effectively a no-op.
8. **No `awaitTermination` in `@PreDestroy`.** Method returns immediately; Spring proceeds to destroy other beans (`S3Client`, HTTP pool) while archive writes may still be mid-flight.
9. **Stranded queue on shutdown.** Tasks sitting in the queue at shutdown are not drained, not persisted, not logged. Silently lost.

### Observability gaps

10. **No metric on backpressure events.** Queue-full, drops, interrupt-losses exist only as log lines at uneven levels. No Micrometer counters.
11. **Threads have no descriptive name.** `pool-N-thread-M` defaults; unreadable in thread dumps and logs.
12. **No queue-depth / worker-health gauges.** No way to observe pool utilization without code changes.

### Design / hygiene smells

13. **Non-daemon threads by default.** Combined with broken shutdown, can hold the JVM alive on exit.
14. **`startWriteExecutor()` called from the constructor.** Workers observe `this` before construction completes — classic "this-reference escape" smell, currently safe by accident.
15. **Constructor does I/O** (`doesBucketExist`, `createBucket`). Bean construction can fail on AWS calls; hard to reason about, hard to test, and conflates "bean exists" with "bean is operationally ready."
16. **No retry / dead-letter for failed S3 writes.** If `putObject` throws, the worker's `catch (Exception)` logs and moves on. Archive permanently lost.

### Root cause

Almost every issue above has the same root cause: **the code hand-rolls a thread pool.** Everything from 1 through 14 is something `ThreadPoolExecutor` (and Spring's `ThreadPoolTaskExecutor` wrapper) handles correctly out of the box. Items 15 and 16 are orthogonal.

---

## 4. Decision 1 — Keep a separate pool, or collapse into `taskExecutor`?

### The call chain

```
HTTP request thread
    │ @Async (on taskExecutor)
    ▼
taskExecutor thread — does DB save, then hands off:
    │
    ▼
archiver.submitArchiveTask(task)
    │
    ▼
writeQueue → archiver worker — S3.putObject(...)
```

Because `@Async` already runs on `taskExecutor`, the archiver's own queue + workers aren't shielding the HTTP thread from S3 latency — that was already accomplished one hop earlier. So the question arose: do we even need two pools?

### Case for one pool (drop the archiver queue entirely)

- `taskExecutor` is already off the HTTP path.
- It's already a managed pool with backpressure.
- Two queues + two pools is duplicated plumbing.
- The hand-rolled archiver pool is broken in 16 ways; deleting it is cheaper than fixing it.

### Case for keeping separation

- **Failure-mode isolation.** If S3 slows, a shared pool also blocks DB-audit work. With separation, S3 trouble stays contained to archiving.
- **Different tuning needs.** DB writes are bounded by JDBC connections; S3 writes by network. They benefit from different pool sizes.
- **Different SLOs.** You might accept losing an S3 archive but not a DB audit row.

### What we picked

**Keep separate.** Rationale: legacy code, long production history, no live data showing the isolation isn't valuable — this is a minimal-risk choice. Collapsing the pools could be the right answer later, but it's a bigger change and deserves its own discussion with data.

---

## 5. Decision 2 — `ThreadPoolTaskExecutor` or virtual threads?

With "keep a separate pool" settled, the next question was what that pool should be built on.

### Option A — `ThreadPoolTaskExecutor` (classic platform threads, Spring-managed)

Same shape as the existing `taskExecutor` bean:
```java
ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
executor.setCorePoolSize(corePoolSize);
executor.setMaxPoolSize(maxPoolSize);
executor.setQueueCapacity(queueCapacity);
executor.setThreadNamePrefix("s3-archiver-");
executor.setWaitForTasksToCompleteOnShutdown(true);
executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
```

### Option B — Virtual threads + semaphore cap

```java
Semaphore inFlightGate = new Semaphore(maxInFlight);
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

submit:
  if (!inFlightGate.tryAcquire()) { drop + metric; return; }
  executor.submit(() -> {
    try { task.run(); } finally { inFlightGate.release(); }
  });
```

No queue, no pool in the traditional sense. The semaphore is the concurrency cap.

We dug into Option B in detail before deciding.

---

## 6. Virtual threads — deep dive

### The mount/unmount model

A **platform thread** is a thin Java wrapper over an OS thread. Costs ~1 MB of stack memory. OS-scheduled.

A **virtual thread** (VT) is a JVM-managed lightweight thread that runs on top of a pool of "carrier" OS threads (defaulting to `Runtime.availableProcessors()`, typically a `ForkJoinPool`).

The trick: when a VT performs a blocking I/O operation, the JVM **unmounts** it from its carrier — the OS thread is freed to run other VTs. When the I/O completes, the VT is remounted on any available carrier.

```
Carrier OS thread #1  ←→  [mounted] VT A  ←→  running Java code

    VT A calls socket.read() (blocking)
    JVM unmounts VT A from carrier #1

Carrier OS thread #1  ←→  [idle — can host another VT]
                           (parked)  VT A — waiting on socket
```

This is why VTs scale for I/O-bound workloads: you can have millions of VTs parked on I/O, each consuming a few KB, while a small number of OS threads stay busy multiplexing between them.

### Why S3 PUT is a perfect VT use case (in theory)

- It's a synchronous HTTP call → pure blocking I/O.
- Per-task work is small (JSON serialize + network write).
- No CPU hot loops, no native frames in the hot path (mostly).
- High fan-out potential if load grows.

### Shapes we considered

1. **VT per task, unbounded.** Risk of memory blow-up under sustained S3 outage (tasks accumulate forever).
2. **VT per task + semaphore cap.** Idiomatic. Semaphore gates concurrency; each task runs in its own VT.
3. **Bounded queue + VT consumers.** VTs replacing platform workers in otherwise-classic design. Doesn't really exploit VT cheapness since the bounded-consumer model doesn't need VT scale.

Option 2 was the realistic candidate.

### Where VTs and TPTE genuinely differ

| | TPTE (platform threads) | VT + semaphore |
|---|---|---|
| Concurrency ceiling | `maxPoolSize` (e.g., 8) | Semaphore cap (100s easy) |
| Memory per in-flight task | OS stack ~1 MB × N | VT ~few KB × N |
| Backpressure mechanism | Queue + rejection policy | Semaphore + drop or block |
| Shutdown | `setAwaitTerminationSeconds`, Spring-managed | `executor.close()` blocks until VTs finish |
| Spring integration | First-class | Works but less idiomatic |
| Production maturity | Decades | ~1.5 years since Java 21 GA |
| Pinning concerns | N/A | Real, library-dependent |

---

## 7. Pinning — what it is, why it matters, how to detect it

### The precise meaning

Pinning is when the JVM **cannot** unmount a VT from its carrier, even though the VT is about to block. The carrier OS thread sits idle alongside the parked VT until the pin is released. This is functionally identical to platform threads: one OS thread blocked per concurrent I/O op. Under widespread pinning, VT scalability collapses to platform-thread scalability.

### Why `synchronized` causes pinning (Java 21)

`synchronized` uses JVM-level object monitors. Monitor ownership is recorded as an **OS thread identity**, not a VT identity. If a VT holding a monitor were unmounted, the monitor state would become incoherent (a different VT on the same carrier would appear to own it). The JVM's only correct choice is to pin.

Concrete triggers:
```java
synchronized (lock) {
  socket.read();  // PINS for the duration
}

public synchronized void fetch() {        // implicit synchronized(this)
  socket.read();  // PINS
}
```

### Why `ReentrantLock` does NOT cause pinning

`ReentrantLock` is pure Java. Ownership is recorded as a `Thread` object reference. When a VT holding a lock is unmounted, the reference still points to "this VT" — the Java object — regardless of where (or whether) it's mounted. Mutual exclusion stays correct.

The rule of thumb: **`synchronized` is JVM, locks are Java. VT unmounting is a JVM feature — Java-level locks compose with it; JVM-level locks fight with it.**

Same applies to `Semaphore`, `CountDownLatch`, `CyclicBarrier`, `BlockingQueue`, and everything else in `java.util.concurrent` — all built on `AbstractQueuedSynchronizer`, all VT-friendly.

### Other pinning causes

- **Native frames on the stack (JNI).** Native stack frames can't be migrated. Cryptographic code using OpenSSL via JNI, native compression libs, etc.
- **A handful of legacy APIs** — very rare to hit.

### Not pinning (safe with VTs)

- Plain blocking I/O on NIO channels.
- Parking on `j.u.c.` primitives.
- `Thread.sleep()`, `Object.wait()` (fixed in Java 21).
- `CompletableFuture.get()`.

### Throughput math when full pinning occurs

Example: 1000 archive tasks, 200ms each S3 PUT, 8 carrier threads.

- **No pinning:** all 1000 run concurrently. Total time ≈ 200ms.
- **Full pinning:** 8 concurrent at most. Total time ≈ 25 seconds.
- **Partial pinning (20% pin):** mixed behavior — some VTs fly, others stuck on carriers. Tail latency spikes. Worst realistic case — p50 looks fine, p99 blows up. Easy to miss in dashboards.

### AWS SDK v2 reality

- SDK v2 internals use `ReentrantLock` in most hot paths (VT-friendly).
- Historical `synchronized` hotspots include: credentials provider chain, metrics publisher, Apache HttpClient connection-pool checkout.
- SDK versions 2.25+ have been progressively cleaning these up. Bulletproof only at 2.28+.
- Apache HttpClient 4 (default in pre-2.28 SDK): pins during pool checkout. HttpClient 5 much better.

### Java version status

- Java 20 and earlier: broad pinning, VTs in preview.
- **Java 21 (where we are):** `synchronized` still pins. `Object.wait()`, `Thread.sleep()`, `park()` do not pin. NIO channels do not pin.
- Java 24 (JEP 491, 2025): `synchronized` monitors rewritten to track VT identity. Pinning largely eliminated.

### Detecting pinning

Three layers, cheap to expensive:

1. **Grep own code** for `synchronized`. Fast but only covers your code.
2. **Dependency audit** — check AWS SDK version, HTTP client choice, Logback appender config for known hotspots.
3. **Runtime detection** with `-Djdk.tracePinnedThreads=full`. The JVM prints a stack trace annotated with `<== monitors:1` at each pinning event. Authoritative but noisy.
4. **JFR event** `jdk.VirtualThreadPinned` — structured, low-overhead, suitable for continuous production monitoring.

The only way to be sure is to run under VT with one of the detection mechanisms enabled. Static analysis alone is never complete because transitive library code is opaque.

### Why "pinned VTs behave like platform threads" oversimplifies

Under full pinning, VTs match platform-thread **throughput ceiling** but differ in:
- Which number sets the ceiling (`maxPoolSize` for TPTE; carrier count for VT, tied to CPU count).
- Memory per waiting task (small `Runnable` vs. VT object with continuation).
- Tail-latency predictability (TPTE uniform, VT volatile under partial pinning).
- Observability (TPTE standard JMX/Actuator, VT needs JFR).

Partial pinning — the realistic case — is where VTs perform **worse** than TPTE in tail latency.

---

## 8. Load analysis and the final call

### Measured load

| | Req/sec | Archive tasks/sec (2 per request) |
|---|---|---|
| Average | 2 | ~4 |
| Peak | 10 | ~20 |

### Capacity headroom at `maxPoolSize=8`

Pessimistic (500ms/PUT): 8 × (1/0.5) = **16 tasks/sec sustained**. Peak 20/sec briefly queues ~4 tasks, drains in 0.25s.

Realistic (200ms/PUT): 8 × (1/0.2) = **40 tasks/sec sustained**. 2x headroom over peak.

### What this means for VT benefits

| VT benefit | Real at our load? |
|---|---|
| Higher concurrency ceiling | No — need ~10, have 8 |
| Lower memory at high concurrency | No — 8 MB of stack is nothing |
| Better burst handling | No — current bursts fit within pool |
| No cold-start pool growth | No — stays at core size at this load |
| Better tail latency | No — queue is essentially empty |

At this load, VTs optimize something that isn't a bottleneck.

### This also explains why the broken code didn't cause incidents

The hand-rolled pool has 9 correctness bugs that only fire under pressure. At 4–20 tasks/sec with 4 workers, the queue is almost always empty, workers rarely park long, and shutdown rarely coincides with in-flight writes. The system has been living in the safe zone by accident.

### The decision

**Go with TPTE.** The load doesn't justify VTs, the risk profile argues against them (pinning uncertainty, no team VT experience), and TPTE resolves 11 of the 16 bugs by construction. VTs remain a future option if throughput grows to the point where the pool ceiling would matter, or once we're on Java 24+.

---

## 9. What TPTE actually fixes, issue by issue

Moving to `ThreadPoolTaskExecutor` fixes most of the 16 issues by removing the hand-rolled code that caused them:

| # | Issue | Fixed by TPTE? | How |
|---|---|---|---|
| 1 | Offer-then-put blocks caller | Yes | Replaced by rejection policy |
| 2 | No full-queue policy | Yes | Made explicit via `setRejectedExecutionHandler` |
| 3 | Producer `InterruptedException` swallowed | Yes | `execute()` doesn't throw it — no try/catch needed |
| 4 | Consumer `InterruptedException` swallowed | Yes | No custom consumer loop |
| 5 | `running` not volatile | Yes | No `running` flag |
| 6 | Workers parked in `take()` | Yes | No `take()` loop in our code |
| 7 | Wrong `shutdown()` call | Yes | Spring calls the correct one |
| 8 | No `awaitTermination` | Yes | `setAwaitTerminationSeconds` |
| 9 | Stranded queue on shutdown | Yes | `setWaitForTasksToCompleteOnShutdown(true)` |
| 10 | No metrics on backpressure events | No | Needs explicit Micrometer counters |
| 11 | Thread names | Yes | `setThreadNamePrefix` |
| 12 | No queue/worker gauges | Partial | Spring Actuator auto-binds pool/queue metrics |
| 13 | Non-daemon threads | Partial | Configurable; also mooted by correct shutdown |
| 14 | Constructor starts threads | Yes | Spring bean lifecycle |
| 15 | Constructor does I/O | No | Orthogonal — needs separate `@PostConstruct` move |
| 16 | No retry / DLQ | No | Orthogonal — policy question |

### What's left after TPTE

- **#10** — custom counters for submitted/rejected/failed tasks.
- **#12** — queue-depth and pool-size gauges (partially free via Actuator).
- **#15** — move bucket check to `@PostConstruct`.
- **#16** — retry / dead-letter policy for failed S3 writes.

These are all addressable later as separate, focused changes.

---

## 10. Policy decisions

Before writing code, three policy questions needed answers:

### Rejection policy when pool + queue are both saturated

**Chose `CallerRunsPolicy`.** When the executor can't accept a task, the calling thread runs `task.run()` inline. In our chain, the caller is a `taskExecutor` thread (not the HTTP thread — that returned long ago), so it's safe to block. Natural backpressure: the caller is busy running the task, can't submit more, upstream queue fills, pressure cascades back to source at a sustainable rate. Nothing is dropped.

JDK source of `CallerRunsPolicy`:
```java
public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
  if (!e.isShutdown()) {
    r.run();
  }
}
```

Alternatives rejected:
- `AbortPolicy` — throws, requires caller to handle. Would need drop-metric instrumentation.
- `DiscardPolicy` — silently drops. Worst observability.
- `DiscardOldestPolicy` — drops oldest queued. Weird for audit archives.

### Shutdown drain timeout

**30 seconds**, matching the existing `taskExecutor` bean. One less knob to reason about.

### Config knobs (env var names)

Kept the old env vars (`S3_ARCHIVE_CONSUMER_THREADS`, `S3_ARCHIVE_QUEUE_SIZE`) for backward compatibility — they now map to `core-pool-size` and `queue-capacity`. Added new ones for new knobs:

- `S3_ARCHIVE_MAX_POOL_SIZE` (default 8)
- `S3_ARCHIVE_THREAD_NAME_PREFIX` (default `s3-archiver-`)
- `S3_ARCHIVE_AWAIT_TERMINATION_SECONDS` (default 30)

### Final shape

Kept the `submitArchiveTask(Runnable)` wrapper method (now just `executor.execute(task)`) instead of going with `@Async("s3ArchiverExecutor")` on the archive methods. Smaller diff, keeps submission explicit and greppable.

---

## 11. The implementation

### Bean definition (eventually moved to `AsyncConfig.java`)

```java
@Bean(name = "s3ArchiverExecutor")
public Executor s3ArchiverExecutor(
    @Value("${myservice.api.archive.s3.core-pool-size}") int corePoolSize,
    @Value("${myservice.api.archive.s3.max-pool-size}") int maxPoolSize,
    @Value("${myservice.api.archive.s3.queue-capacity}") int queueCapacity,
    @Value("${myservice.api.archive.s3.thread-name-prefix}") String threadNamePrefix,
    @Value("${myservice.api.archive.s3.await-termination-seconds}")
        int awaitTerminationSeconds) {
  ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
  executor.setCorePoolSize(corePoolSize);
  executor.setMaxPoolSize(maxPoolSize);
  executor.setQueueCapacity(queueCapacity);
  executor.setThreadNamePrefix(threadNamePrefix);
  executor.setWaitForTasksToCompleteOnShutdown(true);
  executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
  executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
  return executor;
}
```

### Property additions (`application.properties`)

```properties
myservice.api.archive.s3.core-pool-size=${S3_ARCHIVE_CONSUMER_THREADS:4}
myservice.api.archive.s3.max-pool-size=${S3_ARCHIVE_MAX_POOL_SIZE:8}
myservice.api.archive.s3.queue-capacity=${S3_ARCHIVE_QUEUE_SIZE:100}
myservice.api.archive.s3.thread-name-prefix=${S3_ARCHIVE_THREAD_NAME_PREFIX:s3-archiver-}
myservice.api.archive.s3.await-termination-seconds=${S3_ARCHIVE_AWAIT_TERMINATION_SECONDS:30}
```

### Archiver changes

**Removed:** `LinkedBlockingQueue writeQueue`, `ExecutorService executorService`, `boolean running`, `int consumerThreads`, `int queueSize`, `startWriteExecutor()` method, `@PreDestroy shutdown()` method.

**Added:** single `Executor executor` field injected via `@Qualifier("s3ArchiverExecutor")`.

**Simplified `submitArchiveTask`:**
```java
private void submitArchiveTask(Runnable archiveTask) {
  executor.execute(archiveTask);
}
```

### Follow-up: config class split

Originally the bean lived in `MetricsConfig.java`, which also held the metric name constants and `TimedAspect` bean. Split into a new `AsyncConfig.java` holding `@EnableAsync` and both executor beans (`taskExecutor` and `s3ArchiverExecutor`). `MetricsConfig.java` is now purely metrics.

Rationale: the `conf/` package follows a one-concern-per-class convention (`AwsConfig`, `CacheConfig`, etc.). Keeping executors in a file named `MetricsConfig` violated discoverability.

---

## 12. Still-open work (post-refactor)

These were deliberately deferred — not solved by the pool migration, and each deserves its own change:

### Issue 10 — Backpressure metrics

Add Micrometer counters:
- `archive.submitted` — count of tasks accepted into the executor
- `archive.rejected` — count of times rejection policy fired
- `archive.failed` — count of task exceptions (S3 PUT errors)

Wire via a custom `RejectedExecutionHandler` that increments on rejection, then delegates to `CallerRunsPolicy`.

### Issue 12 — Queue/worker gauges

Mostly free via Spring Actuator (`executor.active`, `executor.queued`, `executor.pool.size`), but verify they're exposed in `/actuator/metrics` and scraped by Grafana.

### Issue 15 — Constructor I/O

Move `doesBucketExist(...)` / `createBucket(...)` out of the constructor into a `@PostConstruct initialize()` method. Constructor becomes pure field assignment; AWS calls happen during bean initialization but after construction.

### Issue 16 — Retry / dead-letter for failed S3 writes

Policy question first. Decide:
- Is archival best-effort or strict?
- What's the observed failure rate today?
- How sticky are failures (transient vs. extended)?

Implementation options, in increasing cost:
1. In-task retry with exponential backoff (Spring Retry or Resilience4j).
2. Dead-letter store (DB table, local disk queue, SQS DLQ).
3. Full durable task queue (Kafka, SQS as source of truth).

### When to revisit virtual threads

- Archive throughput grows past ~50–100 tasks/sec sustained (pool ceiling becomes relevant).
- Upgrade to Java 24+ (JEP 491 removes `synchronized` pinning).
- AWS SDK / HTTP client migrates fully to VT-native paths.

---

## 13. PR reply

> can we improve the submission logic. If we are using JDK21+ we can use virtual threads as well

Thanks — we improved the submission logic by moving to Spring's `ThreadPoolTaskExecutor`, but decided against virtual threads for this refactor. Current load is low (~2 req/sec average, ~10 peak), so a pool of 8 threads has plenty of headroom and the scaling benefit of virtual threads doesn't really apply. Java 21 also still has issues where virtual threads can get stuck inside `synchronized` blocks (fixed in Java 24), and the AWS SDK / HTTP client internals have hit this before, so using them here would need extra soak-testing first. No other component in this service uses virtual threads today, so adopting them just for this path would add monitoring and review overhead for something that isn't currently a bottleneck. The new executor fixes the submission, shutdown, and interrupt-handling issues regardless of which thread model we use. Happy to revisit if load grows past the pool ceiling or once we're on Java 24+.

---

## 14. Key concepts worth remembering

### Producer-consumer pattern

A bounded queue between a fast producer and a slow consumer is the classic way to decouple latency. The producer enqueues and returns; the consumer drains independently. Bounded queue + rejection policy provides backpressure when the consumer can't keep up.

### Rejection policy matters more than people realize

`ThreadPoolExecutor` offers four built-in policies. Most code just uses the default (`AbortPolicy` — throws). The right policy depends on what you're willing to trade:

- **`AbortPolicy`** — never delay the caller; throw and let caller decide (usually retry or drop + metric).
- **`CallerRunsPolicy`** — the caller runs the task inline. Predictable, bounded blocking time. Natural backpressure.
- **`DiscardPolicy`** — silently drop new task. Fast, lossy.
- **`DiscardOldestPolicy`** — drop oldest queued task. Keeps freshest data; loses stalest.
- **Custom** — most realistic production policy is AbortPolicy + Micrometer counter, or CallerRunsPolicy + metric.

### Interrupt handling

`InterruptedException` is Java's cooperative-cancellation mechanism. When you catch it, the interrupt flag is cleared. Two correct responses:
1. Rethrow / propagate (if possible).
2. **Restore** the flag: `Thread.currentThread().interrupt();`

Swallowing it (log-and-continue) breaks cooperation — upstream code can't tell the thread to stop.

### The Java Memory Model and `volatile`

Writes by one thread are not guaranteed to be visible to reads on another thread without a synchronization point. Required mechanisms:
- `volatile` field
- `synchronized` block
- `AtomicBoolean` / `AtomicReference` etc.
- `java.util.concurrent.locks`
- Implicit sync in `j.u.c.` classes

A plain `boolean` shared across threads is a bug waiting for an unusual memory-ordering architecture (ARM, Apple Silicon) to surface it.

### `ExecutorService` shutdown semantics

- `shutdown()` — stop accepting new submissions, let running tasks finish.
- `shutdownNow()` — stop accepting, interrupt running tasks, return what was queued.
- `awaitTermination(N, UNIT)` — block until all tasks finish or timeout.

Typical correct shutdown pattern:
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

Spring's `ThreadPoolTaskExecutor` with `setWaitForTasksToCompleteOnShutdown(true)` + `setAwaitTerminationSeconds(N)` encodes this pattern for you.

### `@Async` bean resolution in Spring

When you annotate a method with `@Async`, Spring's async infrastructure looks for an executor in this order:
1. A bean explicitly named in `@Async("name")`.
2. A bean named exactly `taskExecutor` that implements `Executor`.
3. A unique `TaskExecutor` / `Executor` bean in the context.
4. Spring Boot's auto-configured `applicationTaskExecutor`.
5. Fall back to `SimpleAsyncTaskExecutor` (new platform thread per task, no pooling — bad).

The `taskExecutor` bean name is a convention, not a type contract. If you have multiple `Executor` beans, always resolve by name or `@Qualifier` to avoid ambiguous fallback to Spring Boot defaults.

### `@Configuration` class organization

Convention in this codebase: one concern per config class. When a config accumulates multiple unrelated bean definitions (e.g., metrics + executors), split it. Beans resolve by name, not by declaring class, so splitting is zero-risk refactoring.

### Virtual threads summary

- **For I/O-bound workloads at high concurrency (100s–1000s of parallel blocking ops), VTs are genuinely transformative.**
- **For low-concurrency workloads, VTs are overkill** — platform threads fit fine.
- Pinning is the main gotcha on Java 21. Caused by `synchronized` and native frames. Look for them in your hot path and in your libraries.
- Detect pinning with `-Djdk.tracePinnedThreads=full` or JFR `jdk.VirtualThreadPinned` events.
- `ReentrantLock` and `j.u.c.` classes do not pin; they compose correctly with VTs.
- Java 24 (JEP 491) eliminates the `synchronized` pinning case.

### MySQL session timezones (tangentially relevant from related discussion)

Not strictly part of this refactor, but worth remembering: `DATETIME` columns are timezone-naive. `CURRENT_TIMESTAMP` fires in the session's timezone (`@@session.time_zone`). If Java reads/writes those columns with `LocalDateTime` (which uses JVM default zone), mismatched JVM and DB zones lead to off-by-offset bugs. The safest reading pattern is `NOW() - INTERVAL :n MINUTE` inside the SQL itself — both sides of the comparison use the same MySQL session clock.

---

*End of document.*
