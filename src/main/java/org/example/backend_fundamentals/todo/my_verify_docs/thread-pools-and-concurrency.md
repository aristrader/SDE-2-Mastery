# Thread Pools, Concurrency & Performance Tuning

## Thread Pools in This Project

### Overview

```
Incoming HTTP Request (Undertow worker thread)
    ↓
VerifyApiService.processVerification()
    ├── Stages run in parallel ──→ traceableExecutorService (16 threads)
    ├── @Async audit calls ──────→ taskExecutor (4-8 threads)
    └── S3 archive ─────────────→ ApiRequestResponseArchiver.executorService (4 threads)
                                     ↑
                               permanent consumer loops
                               draining LinkedBlockingQueue
```

### Pool Details

| Pool | Bean | Size | Pattern | Thread Lifetime | What It Does |
|------|------|------|---------|-----------------|--------------|
| Undertow workers | auto-configured | ~cores x 8 | Thread-per-request | Per HTTP request (~1-2s) | Handles incoming HTTP requests |
| Stage executor | `traceableExecutorService` | 16 (fixed) | `CompletableFuture.supplyAsync()` | Per stage (~200-1500ms) | Runs OCR, liveness, face match, ID auth in parallel |
| Async audit | `taskExecutor` | 4 core, 8 max | Spring `@Async` | Per audit call (~200-500ms) | DB audit writes, fire-and-forget from request thread |
| S3 archiver | `ApiRequestResponseArchiver.executorService` | 4 (fixed) | Producer-consumer with `LinkedBlockingQueue` | Permanent (app lifetime) | Uploads request/response JSON to S3 |
| Scheduler | Spring `ThreadPoolTaskScheduler` | 1 (default) | `@Scheduled` cron | Per cron tick | Runs `failStaleCases` |

### Why They're Separate

Each pool has different characteristics:

**Stage executor threads (16)** are short-lived — a stage runs for 200-1500ms, then the thread returns to the pool. Multiple requests share the same 16 threads.

**Async audit threads (4-8)** are also short-lived but serve a different purpose. If they shared the stage executor, audit tasks would compete with verification stages for threads — a slow S3 upload could delay a liveness check.

**S3 archiver threads (4)** are permanent — each runs `while(running) { take(); run(); }` for the entire app lifetime. They never return to the pool. If these shared the `taskExecutor`, they'd permanently occupy 4 of the 8 max threads, starving `@Async` audit methods.

**Rule: pools with different lifecycles or different priorities should be separate.** Mixing short-lived tasks with permanent consumers in the same pool leads to thread starvation.

---

## How Thread Pools Work

### Java's `ThreadPoolExecutor`

Spring's `ThreadPoolTaskExecutor` wraps Java's `ThreadPoolExecutor`. The behavior follows a specific order:

```
Task submitted
  → Core thread available?       → YES → run immediately on core thread
                                  → NO  ↓
  → Queue has space?             → YES → queue the task (wait for a thread)
                                  → NO  ↓
  → Below maxPoolSize?           → YES → create new thread, run immediately
                                  → NO  ↓
  → REJECT (RejectedExecutionException)
```

Key insight: **the pool grows to maxPoolSize only after the queue is full.** With `queueCapacity=100`, you need 100+ pending tasks before the pool creates a 5th thread.

### `FixedThreadPool` (used by stage executor and archiver)

```java
Executors.newFixedThreadPool(16)
```

- Core size = max size = 16
- Unbounded queue (`LinkedBlockingQueue` with no capacity limit)
- Never rejects tasks — they queue forever
- Never creates extra threads beyond 16

### `ThreadPoolTaskExecutor` (used by async audit)

```java
executor.setCorePoolSize(4);    // 4 threads always alive
executor.setMaxPoolSize(8);     // can grow to 8
executor.setQueueCapacity(100); // queue between core and max
```

- 4 threads always alive
- Tasks 5-104 go into the queue
- Tasks 105+ cause threads 5-8 to be created
- Task 109+ rejected (queue full + max threads reached)

---

## How to Decide Thread Count

### The Constraint Chain

Threads don't exist in isolation. They interact with:

```
Thread Pool
    ↓ borrows
DB Connection Pool (HikariCP — 10 connections)
    ↓ sends queries to
MySQL (max_connections — typically 100-500)
    ↓ reads/writes
Disk I/O

Thread Pool
    ↓ makes HTTP calls to
External Services (VIDA, ASG, S3)
    ↓ limited by
Network bandwidth + service rate limits
```

Adding more threads only helps until you hit the next bottleneck in the chain.

### For I/O-Bound Work (most of this project)

Verification stages, audit writes, and S3 uploads are all I/O-bound — the thread spends most of its time **waiting** for a response (DB, HTTP, S3), not computing.

**Formula (Little's Law):**
```
optimal_threads = throughput × latency
```

Example for stages:
- Target: handle 20 concurrent requests, each with 3 stages
- Stage latency: ~500ms average
- Throughput needed: 20 × 3 = 60 stages/sec
- Optimal threads: 60 × 0.5s = 30

But 30 threads all doing DB queries would exhaust the 10-connection pool. So the real constraint is:
```
practical_threads = min(optimal_threads, connection_pool_size × 2)
```

With 10 DB connections: ~16-20 threads is a reasonable ceiling for DB-heavy work.

### For CPU-Bound Work (rare in this project)

If threads are doing computation (JSON parsing, image processing), more threads than CPU cores causes context switching overhead.

**Formula:**
```
optimal_threads = number_of_CPU_cores
```

Or `cores + 1` to keep the CPU busy during occasional I/O waits.

### Practical Guidelines

| Scenario | Recommended Pool Size | Reasoning |
|----------|----------------------|-----------|
| I/O-bound, moderate concurrency | 2 × CPU cores | Threads wait on I/O, CPU is idle — use more threads |
| I/O-bound, high concurrency | Constrained by downstream | DB pool, external service limits |
| CPU-bound | CPU cores | More threads = context switching waste |
| Mixed | CPU cores + I/O queue | Separate pools for CPU and I/O work |
| Permanent consumers (archiver) | 2-4 | Enough to drain queue, not more — threads are never returned |

### What Happens With Wrong Sizing

**Too few threads:**

```
20 requests arrive, each needs 3 stages
4 threads available
Stage execution serialized → request latency 3x-5x higher
Queue builds up → memory grows
Downstream services are idle — capacity wasted
```

Symptoms: high request latency, low CPU usage, queue depth growing.

**Too many threads:**

```
64 threads all make DB queries simultaneously
10 HikariCP connections available
54 threads WAIT for a connection (up to 30s timeout)
Connection pool exhausted → HikariCP timeout exceptions
External services overwhelmed → HTTP 429 / 503 responses
```

Symptoms: `HikariPool - Connection is not available` errors, external service errors, high memory usage, context switching overhead.

**Sweet spot:**

```
16 threads, 10 DB connections
At any time: ~10 threads actively querying, ~6 waiting for DB or I/O
DB connections fully utilized but not exhausted
External services under reasonable load
Request latency stable
```

Symptoms: consistent request latency, DB connections ~70-80% utilized, no timeout errors.

---

## Monitoring Thread Pools

### Key Metrics to Watch (Grafana/Prometheus)

**Thread pool health:**
- `executor.pool.size` — current number of threads
- `executor.active` — threads currently executing tasks
- `executor.queued` — tasks waiting in queue
- `executor.completed` — total tasks completed

**Signs of trouble:**
- `queued` consistently > 0 → pool is undersized or downstream is slow
- `active` == `pool.size` constantly → pool is saturated
- `completed` rate dropping → threads are blocked somewhere

**DB connection pool (HikariCP):**
- `hikaricp.connections.active` — connections in use
- `hikaricp.connections.pending` — threads waiting for a connection
- `hikaricp.connections.timeout` — connection acquisition failures

**Signs of trouble:**
- `pending` > 0 frequently → too many threads competing for connections
- `timeout` > 0 → critical — requests are failing due to connection starvation

### How to Expose These Metrics

Spring Boot Actuator + Micrometer auto-exposes thread pool and HikariCP metrics at `/actuator/prometheus`. Ensure these are scraped by Prometheus and dashboarded in Grafana.

---

## Thread Safety Concerns

### Shared Mutable State

Spring beans are **singletons** — all threads share the same instance. If a bean has a mutable field, concurrent threads can corrupt it.

**Unsafe:**
```java
@Service
public class MyService {
    private int counter = 0;  // shared across all threads!

    public void process() {
        counter++;  // race condition — two threads can read the same value
    }
}
```

**Safe approaches:**
- `AtomicInteger` — lock-free atomic operations
- `synchronized` block — mutual exclusion (but reduces parallelism)
- `ThreadLocal` — each thread gets its own copy (used by `MyServiceContext` and `MDC`)
- Immutable objects — no mutable state to corrupt

### ThreadLocal

`MyServiceContext` uses `ThreadLocal` to store per-request data (transaction ID, group ID). Each thread has its own copy — no sharing, no race conditions.

**Risk:** If a thread is reused (thread pools!), the ThreadLocal value from the previous request leaks into the next. Must clear after each request — the `MyServiceContextFilter` handles this.

### `@Async` and Context Propagation

When Spring submits an `@Async` task, it runs on a different thread. `ThreadLocal` values (MDC, MyServiceContext) are NOT automatically carried over.

The stage executor handles this manually:
```java
Map<String, Object> capturedContext = MyServiceContext.getContextMapCopy();
Map<String, String> capturedMdc = MDC.getCopyOfContextMap();
return CompletableFuture.supplyAsync(() -> {
    MyServiceContext.setContextMap(capturedContext);
    MDC.setContextMap(capturedMdc);
    return doWork();
}, traceableExecutorService);
```

The `ContextExecutorService.wrap()` on `traceableExecutorService` automates this for Micrometer tracing context.

### `LinkedBlockingQueue` Thread Safety

The archiver's `writeQueue` is a `LinkedBlockingQueue` — it's **thread-safe** by design. Multiple producer threads (request handlers calling `offer()`/`put()`) and multiple consumer threads (calling `take()`) can operate concurrently without external synchronization.

---

## Shutdown Behavior

### The Problem

During pod restart, threads may still be executing tasks when the JVM starts shutting down.

| Pool | Shutdown Behavior | Config |
|------|-------------------|--------|
| `taskExecutor` | Drains queue, waits 30s | `waitForTasksToCompleteOnShutdown=true`, `awaitTerminationSeconds=30` |
| `traceableExecutorService` | Immediate — no shutdown config | `Executors.newFixedThreadPool()` default |
| Archiver | `running=false` + `executor.shutdown()` | `@PreDestroy` method |
| Scheduler (ShedLock) | Redis lock auto-expires in 10s | `defaultLockAtMostFor=PT10S` |

### `shutdown()` vs `shutdownNow()`

| Method | Running Tasks | Queued Tasks | Blocked Threads |
|--------|--------------|--------------|-----------------|
| `shutdown()` | Finish | Execute then stop | Stay blocked until task arrives or interrupt |
| `shutdownNow()` | Interrupted | Discarded (returned as list) | Interrupted |

The `taskExecutor` uses `shutdown()` (via `waitForTasksToComplete=true`).
The archiver uses `shutdown()` (via `@PreDestroy`).

---

## The `@Transactional` + Thread Pool Interaction

A `@Transactional` method borrows a DB connection for the entire method duration:

```java
@Transactional
public void recomputeOverallStatus(String groupId) {
    // DB connection borrowed HERE
    KycStatusEntity status = repo.findByGroupId(groupId);     // uses connection
    List<...> attempts = repo.findAllAttempts(groupId);        // uses connection
    OverallStatus computed = compute(attempts);                // CPU work, connection held idle
    repo.updateOverallStatus(groupId, computed);               // uses connection
    // DB connection returned HERE (on commit)
}
```

If 16 stage threads all call `@Transactional` methods simultaneously, they all try to borrow from the 10-connection pool. 6 threads wait. This is normal and expected — but if the transaction holds the connection for a long time (e.g., slow external call inside a transaction), connections get starved.

**Rule: keep transactions short. Don't put HTTP calls or slow I/O inside `@Transactional`.**

---

## Configuration Reference

### Current settings (application.properties)

```properties
# Stage executor — parallel verification stages
myservice.api.vida.thread.pool.size=${VIDA_THREAD_POOL_SIZE:16}

# S3 archiver — queue + consumer threads
myservice.api.archive.s3.queue.size=${S3_ARCHIVE_QUEUE_SIZE:100}
myservice.api.archive.s3.consumer.threads=${S3_ARCHIVE_CONSUMER_THREADS:4}

# DB connection pool (HikariCP defaults, not explicitly set)
# spring.datasource.hikari.maximum-pool-size=10
# spring.datasource.hikari.minimum-idle=10
# spring.datasource.hikari.connection-timeout=30000

# KYC scheduler
kyc.status.scheduler.batch-size=${KYC_STATUS_SCHEDULER_BATCH_SIZE:1000}
```

### Async audit executor (MetricsConfig.java)

```java
executor.setCorePoolSize(4);
executor.setMaxPoolSize(8);
executor.setQueueCapacity(100);
executor.setWaitForTasksToCompleteOnShutdown(true);
executor.setAwaitTerminationSeconds(30);
```

---

## Topics for Further Reading

### Java Concurrency
- `java.util.concurrent` package — `ExecutorService`, `CompletableFuture`, `CountDownLatch`, `Semaphore`
- `ThreadPoolExecutor` internals — core/max/queue interaction, `RejectedExecutionHandler` policies
- `CompletableFuture` composition — `thenApply`, `thenCompose`, `allOf`, `handle`, `exceptionally`
- `volatile` keyword — visibility guarantees across threads (used by `running` flag in archiver)
- `synchronized` vs `Lock` vs `AtomicReference` — when to use which
- Deadlocks in Java — lock ordering, `jstack` for thread dump analysis
- `ForkJoinPool` — work-stealing for recursive/divide-and-conquer tasks

### Virtual Threads (Java 21)
- `Thread.ofVirtual()` / `Executors.newVirtualThreadPerTaskExecutor()`
- How virtual threads change the thread-per-request model — millions of threads, no pool tuning
- When NOT to use virtual threads — CPU-bound work, `synchronized` blocks (pinning)
- Migration path from `ThreadPoolExecutor` to virtual threads
- Impact on connection pools — virtual threads can exhaust DB connections easily without Semaphore guards

### Connection Pool Tuning
- HikariCP wiki — "About Pool Sizing" (the definitive guide)
- Formula: `connections = (core_count * 2) + effective_spindle_count`
- `leakDetectionThreshold` — detect connections not returned to pool
- `maxLifetime` — rotate connections to avoid MySQL's `wait_timeout` kill
- Correlation between thread pool size and connection pool size

### Blocking vs Non-Blocking I/O
- Java NIO — channels, selectors, buffers
- Event loop model (Netty) — single thread handling thousands of connections
- Reactive programming (Project Reactor) — `Mono`, `Flux`, backpressure
- When blocking is fine (this project) vs when reactive is needed (high-connection-count services)

### Observability for Concurrency
- Thread dumps (`jstack`, `kill -3`) — diagnosing deadlocks and thread starvation
- Java Flight Recorder (JFR) — low-overhead production profiling
- Micrometer thread pool metrics — `executor.*` metric names
- HikariCP metrics — `hikaricp.connections.*`
- Grafana dashboards for thread pool and connection pool monitoring

### Production Debugging
- `jstack <pid>` — snapshot of all thread states
- `WAITING` state — thread blocked on `take()`, `wait()`, or lock acquisition
- `RUNNABLE` state — thread actively executing or in I/O syscall
- `BLOCKED` state — thread waiting to enter `synchronized` block
- `TIMED_WAITING` state — thread in `sleep()` or `wait(timeout)`
- Thread dump analysis tools — fastThread.io, TDA (Thread Dump Analyzer)
