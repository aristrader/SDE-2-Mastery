# Concurrency and Deadlock Analysis: KYC Tables

## Scope

This document covers three independent concurrency problems on the KYC tables and the mechanisms that resolve each. Read each section as a self-contained "context → problem → solution → why" analysis.

| Section | Table | Problem |
|---|---|---|
| [Multi-Pod Cron Contention](#problem-1-multi-pod-contention) | `kyc_status` | All pods fire the same cleanup cron simultaneously |
| [Scheduler vs Request-Thread Deadlock](#problem-2-scheduler-vs-request-thread) | `kyc_status` | Bulk UPDATE and per-row UPDATE lock secondary indexes in opposing order |
| [Pessimistic Locking on `kyc_operation_attempts`](#pessimistic-locking-on-kyc_operation_attempts) | `kyc_operation_attempts` | Lost updates between parallel post-checks racing on the same row |

## Background

The `kyc_status` table tracks the overall state of a KYC verification flow. It is written to by two independent code paths:

1. **Request threads** — during verification processing (`KycStorageFacade.ensureCase`, `recomputeOverall`, `updateOverall`).
2. **Scheduled cleanup** — `KycStatusTimeoutScheduler.failStaleCases()` transitions stale `IN_PROGRESS` cases to `FAILED` after the configured timeout.

**Symptom observed in production:** the scheduler's bulk UPDATE was being killed periodically with `Deadlock found when trying to get lock; try restarting transaction` (MySQL error 1213). The request threads survived because MySQL chose the bulk UPDATE as the deadlock victim, but the scheduler's job rolled back entirely on each occurrence — so stale `IN_PROGRESS` cases accumulated until the next successful cron run.

The doc is structured as: how DB deadlocks work in general → the specific incident on `kyc_status` → the alternatives considered → the chosen fix. A separate section at the end covers a different (read-modify-write) concurrency problem on `kyc_operation_attempts`.

---

## How Database Deadlocks Work

A deadlock occurs when two (or more) transactions each hold a lock that the other needs, creating a **circular wait** where neither can proceed.

**Example:**
```
Transaction A:  holds Lock X  →  waiting for Lock Y
Transaction B:  holds Lock Y  →  waiting for Lock X
```

MySQL's InnoDB engine detects this circular wait automatically (usually within milliseconds) and kills one transaction as the "deadlock victim" — rolling it back so the other can proceed.

### InnoDB Locks Index Entries, Not Rows

InnoDB doesn't lock "rows" directly. It locks **index entries**. A single row can have entries in multiple indexes, and each entry is locked independently. This is the root cause of most deadlocks — two transactions accessing the same row through different indexes can acquire locks in different orders.

### Lock Types

- **Shared lock (S)** — allows other transactions to read but not write
- **Exclusive lock (X)** — blocks all other access
- **Next-key lock** — locks an index entry AND the gap before it (prevents phantom reads in REPEATABLE READ)

### Why Secondary Index Updates Cause Deadlocks

When an UPDATE changes a column that is part of a secondary index, InnoDB must:
1. Lock and delete the **old** secondary index entry
2. Lock and insert the **new** secondary index entry
3. Lock and update the **primary key** (clustered index) entry

If two transactions update the same row but find it through different indexes, they lock these entries in different orders — creating deadlock potential.

---

## Table Schema

```sql
CREATE TABLE kyc_status (
  id                    BIGINT        NOT NULL AUTO_INCREMENT,
  group_id              VARCHAR(128)  NOT NULL,
  product_external_id   VARCHAR(128)  NOT NULL,
  document_capture_mode VARCHAR(32)   NOT NULL,
  flow_type             VARCHAR(64)   NOT NULL,
  overall_status        VARCHAR(32)   NOT NULL,
  created_time          DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_time          DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

  PRIMARY KEY (id),
  UNIQUE KEY uk_kyc_status_group_id (group_id),
  KEY vcases_product_group_idx (product_external_id, group_id),
  KEY vcases_status_updated_idx (overall_status, updated_time)
);
```

Key indexes involved in the deadlock:
- `uk_kyc_status_group_id (group_id)` — used by request threads
- `vcases_status_updated_idx (overall_status, updated_time)` — used by the scheduler

---

## The Deadlock Scenario

### Two Transactions

**Transaction 1 — Scheduler** (`failStaleCases`):
```sql
UPDATE kyc_status SET overall_status = 'FAILED'
WHERE overall_status = 'IN_PROGRESS' AND updated_time < :cutoff
```
Scans via `vcases_status_updated_idx`. Locks multiple rows as it scans.

**Transaction 2 — Request thread** (`recomputeOverall` → `updateOverall`):
```sql
UPDATE kyc_status SET overall_status = 'VERIFIED'
WHERE group_id = '019d71a0'
```
Finds the row via `uk_kyc_status_group_id`. Locks one specific row.

### Why Both Touch `vcases_status_updated_idx`

Both transactions change `overall_status`, which is a column in `vcases_status_updated_idx`. So both must modify that secondary index — delete the old entry and insert a new one.

### The Circular Wait

Consider row R: `id=37, group_id='019d71a0', overall_status='IN_PROGRESS', updated_time=16:48`

This row has entries in:
- PK: `(id=37)`
- `uk_kyc_status_group_id`: `(group_id='019d71a0')`
- `vcases_status_updated_idx`: `(IN_PROGRESS, 16:48)`

**Scheduler:**
1. Scans `vcases_status_updated_idx` → locks entry `(IN_PROGRESS, 16:48)` for row R
2. Needs to lock PK `(id=37)` to perform the update → **BLOCKED** (request holds it)

**Request thread:**
1. Finds row via `uk_kyc_status_group_id` → locks entry `('019d71a0')`
2. Locks PK `(id=37)`
3. Needs to delete old `vcases_status_updated_idx` entry `(IN_PROGRESS, 16:48)` → **BLOCKED** (scheduler holds it)

```
Scheduler:  holds vcases_status_updated_idx lock  →  waiting for PK lock
Request:    holds PK lock                         →  waiting for vcases_status_updated_idx lock
```

**Circular wait → DEADLOCK.** MySQL kills the scheduler's transaction.

### Realistic Timeline (reconstructed from production logs)

```
16:48:00.000  User starts KYC. kyc_status row created with overall_status=IN_PROGRESS.

16:59:59.500  User returns for liveness + face match (same groupId).
              Request thread enters processVerification → ensureCase →
              stages run in parallel on traceableExecutorService.

17:00:00.000  Scheduler cron tick fires KycStatusTimeoutScheduler.failStaleCases().
              The row is 12 min old (timeout = 10 min) → eligible for cleanup.
              Scheduler begins bulk UPDATE, scanning vcases_status_updated_idx
              and locking matching index entries (incl. row for groupId '019d71a0').

17:00:00.080  Request thread: stages complete, calls recomputeOverall →
              updateOverall(groupId='019d71a0', VERIFIED).
              Locates the row via uk_kyc_status_group_id, locks its PK entry,
              then tries to delete the old vcases_status_updated_idx entry.
              That entry is held by the scheduler → BLOCKS.

17:00:00.085  Scheduler tries to lock the row's PK entry (to write the UPDATE) →
              that PK is held by the request thread → BLOCKS.
              Wait-for cycle now exists between the two transactions.

17:00:00.120  InnoDB deadlock detector finds the cycle.
              Scheduler chosen as victim (typically the transaction with less work to undo).
              Scheduler transaction rolls back with SQLState 40001:
                "Deadlock found when trying to get lock; try restarting transaction"
              Request thread's UPDATE proceeds to commit successfully.
```

The chronology is what makes this rare in low-traffic windows but reproducible at peak: the scheduler has to be mid-scan exactly when a request thread for one of the rows it's about to update wakes up.

---

## Two Independent Problems

The original deadlock turned out to mask two distinct concurrency issues stacked on top of each other. We fix them with two different mechanisms because they have different causes.

### Problem 1: Multi-Pod Contention

**Context.** The service is deployed to a Kubernetes cluster with N pods. Spring's `@Scheduled` annotation fires on each pod independently. There is no out-of-the-box "leader" that suppresses execution on the non-leader pods.

**Problem.** On every cron tick, N identical bulk UPDATEs run simultaneously, all targeting the same set of stale rows. Even before the scheduler-vs-request-thread issue (Problem 2), this causes:
- Wasted DB work: N copies of the same UPDATE, only the first one of which actually changes anything.
- Pod-vs-pod row-lock contention: each pod's UPDATE locks the rows it scans; the others wait or deadlock among themselves.

**Solution.** ShedLock — a distributed lock backed by Redis. Exactly one pod's job execution is allowed to proceed per `name + lock window`. Others skip the method body silently. Annotation is on the scheduler method:

```java
@SchedulerLock(name = "KycStatusTimeoutScheduler_failStaleCases")
public void failStaleCases() { ... }
```

**Why ShedLock and not alternatives?**

| Alternative | Why we didn't pick it |
|---|---|
| **DB-based advisory lock** (e.g. `SELECT GET_LOCK(...)`) | Adds DB round-trip + a held connection across the whole job. Redis-backed lock is faster and isolates scheduling concerns from DB. |
| **Spring `@Profile`-based "scheduler pod"** | Operationally fragile — requires designating a special pod via deploy config; blast radius if that pod dies. ShedLock self-heals via the lock TTL. |
| **Kubernetes CronJob** | Splits the deployment topology (the cron becomes a separate K8s resource). More moving parts; harder to share configuration with the rest of the service. |

ShedLock requires only the existing Redis infrastructure and one annotation. Lowest-friction option.

### Problem 2: Scheduler vs Request Thread

**Context.** Even with ShedLock guaranteeing exactly one scheduler instance, the scheduler's bulk UPDATE still runs concurrently with normal verification request threads from the same pod (and others). Both paths write to `kyc_status`, but they reach the rows through different secondary indexes.

**Problem.** As walked through in [The Deadlock Scenario](#the-deadlock-scenario), the scheduler's bulk UPDATE locks `vcases_status_updated_idx` entries first then needs the PK; the request thread locks the PK first then needs `vcases_status_updated_idx`. Opposing lock-acquisition orders → circular wait → InnoDB kills one transaction. In our case, the scheduler is consistently the victim (smaller transaction by undo size), so the operational symptom is: the cleanup job silently fails and stale cases accumulate.

**Solution.** Replace the single bulk UPDATE with a two-step pattern using `FOR UPDATE SKIP LOCKED`:

```sql
-- Step 1: lock rows that aren't currently held by a request thread
SELECT id FROM kyc_status
WHERE overall_status = 'IN_PROGRESS' AND updated_time < :cutoff
FOR UPDATE SKIP LOCKED

-- Step 2: update only those rows (within the same transaction)
UPDATE kyc_status SET overall_status = 'FAILED' WHERE id IN (:ids)
```

The `SKIP LOCKED` modifier means: if a row already has an exclusive lock held by another transaction, **don't wait** — just leave it out of the result set. So contended rows are skipped on this tick and picked up on the next one. The resulting locks are taken in a deterministic order driven by `id`, eliminating the cross-index circular wait entirely.

**Why this approach?** See [All Considered Solutions for Problem 2](#all-considered-solutions-for-problem-2) below — `SKIP LOCKED` was chosen over catch-and-retry, row-by-row processing, and reusing a single index.

---

## All Considered Solutions for Problem 2

### Option 1: `SKIP LOCKED` (chosen)

Replace the single bulk UPDATE with a two-step approach:
```sql
-- Step 1: Lock only rows that aren't contended
SELECT id FROM kyc_status
WHERE overall_status = 'IN_PROGRESS' AND updated_time < :cutoff
FOR UPDATE SKIP LOCKED

-- Step 2: Update only those rows (within the same transaction)
UPDATE kyc_status SET overall_status = 'FAILED' WHERE id IN (:ids)
```

| Pros | Cons |
|------|------|
| No deadlock possible | Requires a native query (JPQL doesn't support SKIP LOCKED) |
| Non-contended rows still get cleaned up | Two queries instead of one |
| Contended rows picked up on next cron tick | Slightly more complex code |

### Option 2: Catch and Retry Next Run

Catch `CannotAcquireLockException` in the scheduler and log a warning. The next cron tick retries.

| Pros | Cons |
|------|------|
| Simplest — 3 lines of code | Entire batch fails, not just the contended row |
| No schema or query changes | All stale cases delayed until next run |

### Option 3: Row-by-Row Processing

SELECT stale IDs first (no lock), then UPDATE each individually in its own transaction. Only the contended row fails.

| Pros | Cons |
|------|------|
| Partial success (4 of 5 rows updated) | More DB round trips (N+1 queries) |
| Fine-grained error handling | Needs TransactionTemplate or separate bean (self-invocation issue) |
| No native queries needed | Significantly more code (~30 lines across 2 classes) |

### Option 4: Same Index for Both Paths

If both the scheduler and request thread access rows through the same index, they'd lock in the same order — no circular wait.

| Pros | Cons |
|------|------|
| Eliminates root cause | Not practical — scheduler queries by status+time, not groupId |

---

## Why Not Avoid the Native Query?

The `SKIP LOCKED` approach requires a native query because JPQL doesn't support `SKIP LOCKED`. Two alternatives were considered to avoid native SQL:

### Alternative A: Hibernate QueryHint

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
@Query("SELECT k FROM KycStatusEntity k WHERE ...")
List<KycStatusEntity> findStaleCasesForUpdate(...);
```

The magic value `-2` tells Hibernate to append `SKIP LOCKED`. This avoids a native query but:
- **Magic number** — `-2` is meaningless without reading Hibernate source. The native query literally says `FOR UPDATE SKIP LOCKED`.
- **Returns full entities** — loads entire `KycStatusEntity` objects into the persistence context. The native query returns just `List<Long>` IDs.
- **Hibernate-specific** — the `-2` convention is not JPA standard. If Hibernate changes the mapping, it silently breaks.

### Alternative B: Row-by-Row with Pessimistic Lock

Instead of the database skipping locked rows, the application tries each row individually and catches lock failures:

```java
// Scheduler (no @Transactional)
public void failStaleCases() {
    List<Long> ids = repository.findStaleCaseIds(IN_PROGRESS, cutoffTime);
    for (Long id : ids) {
        timeoutHelper.tryFailCase(id);  // separate bean for @Transactional proxy
    }
}

// Separate @Component
@Transactional
public boolean tryFailCase(Long id) {
    try {
        KycStatusEntity entity = repository.findById(id).orElse(null); // @Lock(PESSIMISTIC_WRITE)
        if (entity != null && entity.getOverallStatus() == IN_PROGRESS) {
            entity.setOverallStatus(FAILED);
            return true;
        }
        return false;
    } catch (PessimisticLockingFailureException e) {
        return false; // row locked, skip
    }
}
```

No native queries, no magic hints, but:
- **Requires a separate bean** — Spring's `@Transactional` proxy can't intercept self-calls within the same class
- **N+1 DB round trips** — each row: `BEGIN → SELECT FOR UPDATE → UPDATE → COMMIT`
- **Extra state checks** — between the initial SELECT (no lock) and the per-row lock, the row might have been updated by a request thread

### Scalability Comparison

| Stale rows | SKIP LOCKED | Row-by-row |
|------------|-------------|------------|
| 10 | 2 queries, ~1ms | 11 queries, 10 txns, ~50ms |
| 500 | 2 queries, ~5ms | 501 queries, 500 txns, ~2-3s |
| 10,000 | 2 queries, ~50ms | 10,001 queries, 10,000 txns, ~30-60s |

At scale, row-by-row also causes:
- **Connection pool pressure** — holds a HikariCP connection for the entire loop duration
- **Transaction log bloat** — 10,000 individual `COMMIT` operations, each forcing a `fsync` to the InnoDB redo log

### Decision

The 3-line native query (`SELECT id ... FOR UPDATE SKIP LOCKED`) is the right trade-off. It's clear, standard MySQL 8.0 syntax, scales to any volume with constant 2-query overhead, and avoids the complexity of a separate helper bean or Hibernate-specific magic values.

---

## Implementation Summary

### ShedLock (Problem 1)

- **Dependencies:** `shedlock-spring` + `shedlock-provider-redis-spring` v6.9.2 (declared in `pom.xml`).
- **Config:** `@EnableSchedulerLock(defaultLockAtMostFor = "PT10S")` on the Spring Boot application class.
- **Lock provider:** `RedisLockProvider` bean wired against the existing `RedisConnectionFactory`.
- **Annotation:** `@SchedulerLock(name = "KycStatusTimeoutScheduler_failStaleCases")` on `KycStatusTimeoutScheduler.failStaleCases()`.
- **How it works:** On each cron tick, every pod attempts to atomically set a Redis key with `SET NX PX <ttl>`. Exactly one pod's `SET` succeeds; that pod runs the method body. The others get a no-op return from the ShedLock interceptor. The key auto-expires after the TTL (10s here) so a pod crashing mid-job doesn't leave the lock stuck forever.
- **Why TTL = 10s:** the job typically finishes in < 1s. 10s gives a safe margin for slow runs without blocking a future tick for unreasonably long if a pod crashes.

### SKIP LOCKED (Problem 2)

Implementation lives in `audit/KycStatusRepository.java`:

- **`findStaleCaseIdsForUpdate(currentStatus, cutoffTime)`** — native query: `SELECT id FROM kyc_status WHERE overall_status = ? AND updated_time < ? FOR UPDATE SKIP LOCKED`. Returns `List<Long>`.
- **`failStaleCasesByIds(ids, targetStatus)`** — JPQL: `UPDATE KycStatusEntity k SET k.overallStatus = :target WHERE k.id IN :ids`.

Both are called from `KycStatusTimeoutScheduler.failStaleCases()` within the same `@Transactional` boundary. The locks taken by the SELECT survive into the UPDATE because they're held by the same transaction — the UPDATE doesn't re-lock the rows, it just writes them.

**Safety net:** `catch (CannotAcquireLockException)` wraps the call. With `SKIP LOCKED`, rows that *can* be locked are skipped, not errored — so this catch is mostly defensive (e.g., for unexpected wait-timeout scenarios). When triggered, it logs at WARN level rather than ERROR, since the next cron tick will clean up the affected rows.

---

## How to Verify

1. **ShedLock:** Deploy to 2+ pods. Check logs — only one pod should log "Transitioned N stale KYC cases". Others should be silent (ShedLock skips without logging).

2. **SKIP LOCKED:** Trigger a verification request for a stale groupId at the same time the scheduler runs. The scheduler should skip that row (lower update count) and log normally. No deadlock errors.

3. **Safety net:** The `catch (CannotAcquireLockException)` should log a WARN instead of the previous ERROR stack trace.

---

## Pessimistic Locking on `kyc_operation_attempts`

This is a separate concurrency problem from the scheduler-vs-request deadlock above. It does not involve the scheduler at all — it's purely about parallel verification stages racing each other to update the same operation row. The mechanism that fixes it (row-level `SELECT ... FOR UPDATE` via `@Lock(PESSIMISTIC_WRITE)`) is unrelated to `SKIP LOCKED` and ShedLock.

### Context: who writes to `kyc_operation_attempts`

Each KYC verification request can produce multiple writes to `kyc_operation_attempts`:

1. **Initial stage write** — a verification stage (OCR, ID-verification, liveness, face match, …) finishes and calls `KycStorageFacade.saveOperationAttempt(...)`. This INSERTs a row keyed by `(group_id, transaction_id, operation)` with a starting status (typically `VERIFIED`).
2. **Post-check escalations** — after the initial stages, secondary checks run: fraud shield, ID-face duplicate check, ASG-blocking escalation. Each may *worsen* the status of an existing operation row. They call `KycStorageFacade.updateStatusToWorseOf(...)`.
3. **Concurrent first-time inserts** — two stages for the same `(group_id, transaction_id, operation)` can finish at nearly the same time and both attempt to INSERT.

`StageExecutor` runs the verification stages **in parallel** on `traceableExecutorService` (CompletableFuture-based; see `verify/stage/core/StageExecutor`). Multiple post-checks can complete and write to the same operation row at the same wall-clock instant.

### Problem: lost-update race in read-modify-write paths

`KycStorageFacade.escalateIfWorse:264-283` and `KycStorageFacade.updateStatusToWorseOf:332-353` both follow the read-modify-write pattern:

```java
KycOperationEntity entity = findByGroupIdAndTransactionIdAndOperation(...);  // SELECT
TxStatus merged = worseOf(entity.getStatus(), incomingStatus);
if (!merged.equals(entity.getStatus())) {
  finalizeExecution(...);                                                    // UPDATE
}
```

The "right" final status is the worst seen across all checks (precedence: `FAILED > IN_PROGRESS > ERROR > REVIEW > VERIFIED`), independent of which check finished last. Without coordination between concurrent escalators, this guarantee breaks.

**Concrete failure scenario.** Starting from `status=VERIFIED`, two post-checks race — fraud shield wants ERROR, face duplicate wants REVIEW. The correct final state is ERROR (worse of the two).

| Step | T1 (incoming=ERROR) | T2 (incoming=REVIEW) |
|---|---|---|
| 1 | SELECT → reads VERIFIED | SELECT → reads VERIFIED |
| 2 | `worseOf(VERIFIED, ERROR)` = ERROR | `worseOf(VERIFIED, REVIEW)` = REVIEW |
| 3 | UPDATE → ERROR, commit | (still computing) |
| 4 | done | UPDATE → REVIEW, commit (last write wins) |

Final state: REVIEW. Wrong. T2 overwrote T1's escalation because both read the same starting value (VERIFIED) — the worst-seen rule was violated. This is a classic **lost update** anomaly.

### Solution: `@Lock(LockModeType.PESSIMISTIC_WRITE)`

Annotation lives on the repository finder method:

```java
// audit/KycOperationRepository.java:23-25
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<KycOperationEntity> findByGroupIdAndTransactionIdAndOperation(
    String groupId, String transactionId, String operation);
```

This makes JPA emit `SELECT ... FOR UPDATE` instead of a plain SELECT. InnoDB takes an **exclusive row lock** on the matched row, found via the unique index `uk_koa_group_tx_op (group_id, transaction_id, operation)`. Any other transaction that tries to read-with-FOR-UPDATE or write the same row blocks until the holder commits or rolls back.

Same race scenario with the lock in place:

| Step | T1 | T2 |
|---|---|---|
| 1 | SELECT FOR UPDATE → locks row, reads VERIFIED | SELECT FOR UPDATE → **blocks at SELECT** |
| 2 | UPDATE → ERROR, commit, release lock | unblocks, reads ERROR (T1's committed value) |
| 3 | done | `worseOf(ERROR, REVIEW)` = ERROR; no UPDATE needed; commit |

Final state: ERROR. Correct. The lock forces T2 to take its turn, so T2 sees T1's update before computing its own merge.

### Why `PESSIMISTIC_WRITE` and not `PESSIMISTIC_READ`?

**Context.** JPA exposes two pessimistic flavors:

| Mode | SQL emitted | Compatibility |
|---|---|---|
| `PESSIMISTIC_READ` | `SELECT ... FOR SHARE` | other shared readers can hold the lock simultaneously; blocks writers |
| `PESSIMISTIC_WRITE` | `SELECT ... FOR UPDATE` | exclusive — blocks all other readers and writers |

`PESSIMISTIC_READ` is cheaper when readers are *just reading* (it permits parallel reads). It becomes dangerous when readers also intend to write.

**Problem.** Every reader in the escalator code path follows up with an UPDATE. If we used `PESSIMISTIC_READ`, two concurrent escalators could both acquire the shared lock (compatible), then both try to upgrade to exclusive for the UPDATE — and both block on the other's still-held shared lock. This is the **lock upgrade deadlock**:

| Step | T1 | T2 |
|---|---|---|
| 1 | SELECT FOR SHARE → holds shared | SELECT FOR SHARE → holds shared (granted, compatible with T1's) |
| 2 | wants UPDATE → upgrade to exclusive → **blocked** by T2's shared lock | wants UPDATE → upgrade to exclusive → **blocked** by T1's shared lock |
| 3 | (waiting) | (waiting) |

Neither can drop its shared lock (the transaction isn't done), neither can upgrade (the other still holds shared). InnoDB detects the wait-for cycle and aborts one transaction with MySQL error 1213. The caller has to retry, which means added complexity (retry loop, idempotency consideration, possible duplicated work).

**Solution.** `PESSIMISTIC_WRITE` takes the exclusive lock up front. There is no upgrade phase, so there is no upgrade deadlock. Concurrent escalators serialize cleanly through the lock: one gets it, finishes, releases; the next gets it, sees the committed value, finishes.

**Why this is the right choice here.** The lock window is microseconds (one SELECT + one UPDATE on a single row found by a unique index). Serializing escalators on the same row is acceptable; the throughput cost is negligible at our QPS, and the alternative is retry logic everywhere or accepting silent lost updates. The general rule: **if an UPDATE can follow the SELECT, always use `PESSIMISTIC_WRITE`.** `PESSIMISTIC_READ` is only correct when the read is genuinely read-only.

### Locking semantics — what to know to reason about this code

These three points trip people up frequently. Worth keeping in mind when reading or modifying any code that uses `@Lock`:

1. **The lock is acquired at SELECT time, not at UPDATE time.** The word "WRITE" in `PESSIMISTIC_WRITE` describes the lock **mode** (exclusive), not when the lock activates. As soon as `findByGroupIdAndTransactionIdAndOperation(...)` returns, the row is exclusively locked. This is what gives the lock its protective effect — the read and the subsequent write are inside the same critical section.

2. **The lock is held until the surrounding transaction commits or rolls back, not until the UPDATE.** If `worseOf` decides the new status is not worse and we skip the UPDATE, the lock is **still held** until `@Transactional` exits. That's by design — the read is what's being protected, not the write. Holding the lock for "read but no write" is fine because the window is short.

3. **The lock is per-row, scoped via the unique index `uk_koa_group_tx_op`.** Different `(group_id, transaction_id, operation)` combinations match different rows and lock independently. Two escalators on different operations of the same transaction (or different transactions of the same group) don't serialize against each other.

### Three layers of defense in `saveOperationAttempt`

**Context.** `KycStorageFacade.saveOperationAttempt:222-262` is an upsert: insert a new operation row, or escalate the existing one if it already exists. The implementation has three distinct mechanisms, each addressing a different concurrency concern. None of them is redundant — removing any one of them breaks the function under concurrent load.

```java
if (existsByGroupIdAndTransactionIdAndOperation(...)) {     // line 237 — fast path A
  escalateIfWorse(...);
  return;
}
// build entity ...
try {
  saveAndFlush(entity);                                     // line 252 — first-write path
} catch (DataIntegrityViolationException e) {
  escalateIfWorse(...);                                     // line 260 — fallback path B
}
```

The three mechanisms and what each protects against:

1. **`existsBy` check (line 237)** — purely an *optimization*, not a correctness mechanism. The common case for post-checks is "the row was already inserted by the initial stage." Without `existsBy`, every post-check would attempt an INSERT that fails on the unique constraint, then catch and fall back to escalate. The `existsBy` check skips that wasted INSERT and exception. Removing it would still produce correct results — just slower.

2. **The INSERT (line 252)** — required for the *first* attempt at this `(group_id, transaction_id, operation)`. Without an INSERT path, `escalateIfWorse` would `orElseThrow` on a missing row. Some code path has to actually create the row.

3. **The catch on `DataIntegrityViolationException` (line 253-261)** — handles the **TOCTOU race** between the `existsBy` check and the `saveAndFlush`. Between those two statements, a concurrent thread can insert the row. Our INSERT then collides with the unique constraint and Hibernate raises `DataIntegrityViolationException`. Without the catch, the exception bubbles up, the `@Transactional` rolls back, and the caller sees an unhandled error. The catch turns "I lost the insert race" into "ok, I'll escalate the row that the winner inserted."

**The `PESSIMISTIC_WRITE` lock does not make the catch redundant.** The lock and the catch protect against fundamentally different races:

| Race | Coordinated by |
|---|---|
| Two concurrent INSERTs of the same `(group_id, transaction_id, operation)` | Unique constraint `uk_koa_group_tx_op` raises `DataIntegrityViolationException`; the catch routes the loser to `escalateIfWorse` |
| Two concurrent escalators on the same existing row | `SELECT ... FOR UPDATE` (the `@Lock`) serializes them |

The lock cannot help with the first race: there is no row to lock against until one INSERT succeeds. The catch cannot help with the second race: an UPDATE under a shared starting value doesn't violate any constraint, it just silently overwrites.

### Why not `INSERT ... ON DUPLICATE KEY UPDATE`?

**Context.** MySQL provides an atomic upsert via `INSERT ... ON DUPLICATE KEY UPDATE`. This would do insert-or-escalate in a single statement with no race window — the DB engine handles the "row exists or doesn't" check internally and atomically.

**Why we don't use it.** Commit `97734ca8` (`refactor: replace native upsert query with JPA-based insert-or-escalate`) shows the team explicitly **moved away** from `ON DUPLICATE KEY UPDATE` to the current JPA-based pattern. Reasons (from that commit and surrounding context):

- The "worse than" comparison (precedence ordering across `VERIFIED < REVIEW < ERROR < IN_PROGRESS < FAILED`) is awkward to express in pure SQL when `status` is a VARCHAR enum. Doing it with `CASE` expressions makes the SQL hard to read; doing it with a numeric `status_rank` column requires a schema change.
- JPA doesn't natively map `ON DUPLICATE KEY UPDATE`, so it must be a `@Query(nativeQuery = true)`. Mixing native SQL with JPA-managed entities adds maintenance friction.
- The current shape (fast-path existsBy + INSERT + catch + locked escalate) keeps the precedence logic in Java where it's easy to read and unit-test.

So the verbosity is intentional — readability and testability over a marginally tighter race window.

---

## Quick Reference: Concurrency Mechanisms in This Codebase

| Mechanism | Where | Protects against |
|---|---|---|
| ShedLock (`@SchedulerLock`) | `KycStatusTimeoutScheduler` | Multiple pods running the same cron job |
| `SELECT ... FOR UPDATE SKIP LOCKED` | Scheduler bulk update | Scheduler vs request-thread cross-index deadlock on `kyc_status` |
| `@Lock(PESSIMISTIC_WRITE)` | `KycOperationRepository.findByGroupIdAndTransactionIdAndOperation` | Lost-update race between concurrent escalators on `kyc_operation_attempts` |
| Unique constraint + `try/catch DataIntegrityViolationException` | `saveOperationAttempt` | Two concurrent first-time INSERTs of the same operation row |

