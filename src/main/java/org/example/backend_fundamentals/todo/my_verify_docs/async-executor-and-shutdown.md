# Async Executor, Connection Pools & Graceful Shutdown

## The Problem

During pod restarts, in-flight `@Async` audit tasks were rejected:

```
RejectedExecutionException: ExecutorService in shutdown state did not accept task
```

The root cause: Spring was using a single-thread `ThreadPoolTaskScheduler` (meant for `@Scheduled` cron jobs) as the executor for `@Async` methods. On SIGTERM, this scheduler shut down immediately, rejecting any pending audit submissions.

---

## How `@Async` Works in Spring

When a method is annotated with `@Async`:

1. The caller doesn't wait for the method to finish — it returns immediately
2. Spring submits the method body as a task to a `TaskExecutor`
3. The task runs on a separate thread from the executor pool

```
Request thread:  [--- processVerification() ---]  →  return response
                              |
                        @Async call
                              ↓
Executor thread:        [--- auditVerificationResponse() ---]  ← runs independently
```

### Executor Resolution Order

Spring's `@EnableAsync` looks for an executor in this order:

1. A bean named `taskExecutor`
2. A bean of type `TaskExecutor`
3. A bean of type `Executor`
4. Fallback: `SimpleAsyncTaskExecutor` or any available `TaskScheduler`

In this project, no `taskExecutor` bean existed, so Spring fell back to the `ThreadPoolTaskScheduler` (pool size 1) used for `@Scheduled` jobs.

---

## The Fix: Custom TaskExecutor

```java
@Bean(name = "taskExecutor")
public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("async-audit-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);
    return executor;
}
```

### Settings Explained

| Setting | Value | Why |
|---------|-------|-----|
| `corePoolSize` | 4 | 3 `@Async` audit methods, 4 threads allows parallel execution under concurrent requests |
| `maxPoolSize` | 8 | 2x core for burst handling. I/O-bound tasks (DB + S3), so more than 8 could overwhelm downstream resources |
| `queueCapacity` | 100 | Buffers ~10s of burst at 10 tasks/sec. Overflow creates threads up to maxPoolSize |
| `threadNamePrefix` | `async-audit-` | Threads appear as `async-audit-1` in logs instead of `scheduling-1` |
| `waitForTasksToCompleteOnShutdown` | `true` | On SIGTERM: `shutdown()` (drain queue) instead of `shutdownNow()` (kill everything) |
| `awaitTerminationSeconds` | 30 | Safety cap — force-terminate if tasks don't finish within 30s |

### How ThreadPoolTaskExecutor Processes Tasks

```
Task submitted
  → Core thread available?  → YES → run immediately
                            → NO  → Queue has space?  → YES → queue the task
                                                       → NO  → Below maxPoolSize?  → YES → create new thread
                                                                                    → NO  → REJECT (RejectedExecutionException)
```

### Shutdown Behavior

**Without `waitForTasksToComplete`:**
```
SIGTERM → shutdownNow() → interrupt running tasks, discard queued → RejectedExecutionException
```

**With `waitForTasksToComplete`:**
```
SIGTERM → shutdown() → stop accepting NEW tasks → drain running + queued → wait up to 30s → terminate
```

---

## Thread Pools vs Connection Pools

These are completely independent resources:

### Thread Pool (ThreadPoolTaskExecutor)
- **What it holds:** Worker threads waiting for tasks
- **Cost of idle thread:** ~1MB stack memory, no I/O resources
- **When active:** Thread picks up a task, executes it, returns to pool
- **Configured by:** `setCorePoolSize`, `setMaxPoolSize`

### Connection Pool (HikariCP)
- **What it holds:** Open JDBC connections to MySQL
- **Cost of idle connection:** Open TCP socket + MySQL session (~3-5MB on MySQL side)
- **When active:** Thread borrows a connection, runs queries, returns it to pool
- **Configured by:** `spring.datasource.hikari.maximum-pool-size` (default: 10)

### How They Interact

```
Idle thread:     [waiting...]   → no connection held
Task arrives:    [get connection from HikariCP] → [run queries] → [return connection] → [waiting...]
```

A thread only holds a DB connection during query/transaction execution. Idle threads consume zero DB connections.

### Current Pool Sizes in This Project

| Pool | Size | Source |
|------|------|--------|
| Stage executor threads | 16 | `myservice.api.vida.thread.pool.size=16` |
| Async audit threads | 4-8 | `TaskExecutor` bean (new) |
| Scheduler threads | 1 | Spring default `ThreadPoolTaskScheduler` |
| DB connections (HikariCP) | 10 | Spring Boot default |
| Request threads (Undertow) | ~varies | Undertow I/O threads (typically cores x 8) |

Under peak load, 16 stage threads + 8 audit threads + request threads could all compete for 10 DB connections. HikariCP queues the overflow (waits up to 30s). This hasn't been an issue in production, but if DB connection timeouts appear, consider increasing `spring.datasource.hikari.maximum-pool-size`.

---

## Graceful Shutdown

### What It Does

```properties
server.shutdown=graceful
spring.lifecycle.timeout-per-shutdown-phase=30s
```

On SIGTERM:
1. Web server stops accepting **new** HTTP requests (returns 503)
2. Waits for **in-flight** requests to complete (up to timeout)
3. Then proceeds with bean destruction

### Why We Didn't Add It

The K8s deployment already handles traffic routing — new pods spin up first, traffic shifts to them, THEN old pods get SIGTERM. By the time SIGTERM arrives, no new requests hit the old pod.

The only concern is `@Async` tasks that were already submitted but haven't finished. `setWaitForTasksToCompleteOnShutdown(true)` handles that directly at the executor level.

If K8s traffic routing ever changes, or if there are edge cases where requests arrive during shutdown, `server.shutdown=graceful` can be re-added as defense-in-depth.

---

## Topics for Further Reading

### Thread Pool Internals
- Java `ThreadPoolExecutor` — core/max/queue interaction, rejection policies (`AbortPolicy`, `CallerRunsPolicy`)
- `ForkJoinPool` vs `ThreadPoolExecutor` — when to use which
- Virtual threads (Java 21) — `Executors.newVirtualThreadPerTaskExecutor()`, how they change the threading model

### Connection Pool Internals
- HikariCP configuration — `maximumPoolSize`, `minimumIdle`, `connectionTimeout`, `leakDetectionThreshold`
- Connection pool sizing formula: `connections = (core_count * 2) + effective_spindle_count` (from HikariCP wiki)
- How `@Transactional` interacts with connection checkout/return
- Connection leak detection — `spring.datasource.hikari.leak-detection-threshold`

### Spring Transaction Management
- `@Transactional` propagation types — `REQUIRED`, `REQUIRES_NEW`, `NESTED`
- The inner-transaction rollback-only problem (what caused the liveness bug)
- Spring proxy self-invocation limitation — why `@Transactional` doesn't work on private or self-called methods
- `TransactionTemplate` as an alternative to declarative `@Transactional`

### Spring Async
- `@Async` with return types — `CompletableFuture<T>` for async results
- `AsyncConfigurer` interface — custom exception handling for async methods
- `@Async` + `@Transactional` interaction — each async method gets its own transaction
- `CompletableFuture` composition — `thenCompose`, `allOf`, `handle` (used in stage executor)

### Monitoring & Observability
- Micrometer metrics — `@Timed`, custom counters, gauges
- HikariCP metrics — `hikaricp.connections.active`, `hikaricp.connections.pending`
- Thread pool metrics — active count, queue size, completed task count
- Prometheus + Grafana setup for Spring Boot Actuator
