---
order: 20
search: false
---

# Technical Writing Solutions

## Solution: pr-description-migration - Refactor a High-Risk Pull Request Description

### 1. Four Critical Operational Hazards Omitted
1. **Full Table Rewrite Under `ACCESS EXCLUSIVE` Lock:** In PostgreSQL, altering a column type from `INTEGER` to `NUMERIC(18, 4)` requires converting integer bit representations to arbitrary-precision numerics. This triggers a full table rewrite under an `ACCESS EXCLUSIVE` lock. On a 45-million-row table, this lock blocks all concurrent `SELECT`, `INSERT`, `UPDATE`, and `DELETE` queries for minutes, rapidly exhausting HikariCP connection pools and taking down the service.
2. **Unhandled Serialization Failures (`40001`):** Upgrading transaction isolation to `SERIALIZABLE` on the payment settlement path causes PostgreSQL to abort concurrent overlapping transactions with `ERROR: 40001: could not serialize access due to read/write dependencies among transactions`. Without application-level idempotency and automated retry loops with backoff, legitimate user payments will fail.
3. **Dual-Write Anomaly (Kafka Inside DB Transaction):** Publishing to Kafka inside a Spring `@Transactional` method is an anti-pattern. If the database commit fails or times out after the Kafka message is acknowledged, an event is emitted for a transaction that never persisted (phantom event). Furthermore, Kafka network latency holds the relational transaction and database row locks open, degrading database throughput.
4. **Lack of Rolling Deployment Compatibility:** During a zero-downtime rolling deployment, old and new application instances run simultaneously. If the database schema or Kafka message payload changes in an incompatible format, old instances will crash or produce corrupt entries.

### 2. Database Lock and Migration Strategy
Direct DDL alters must be replaced by the **Expand-Contract (Parallel Run) Pattern**:
- **Phase 1 (Expand):** Add a new nullable column `account_balance_v2 NUMERIC(18, 4)`. Adding a nullable column without a default takes milliseconds and requires only a brief metadata lock.
- **Phase 2 (Dual-Write):** Update application code to write to both `account_balance` and `account_balance_v2`, while still reading from `account_balance`.
- **Phase 3 (Backfill):** Run an asynchronous background batch job (e.g., in chunks of 5,000 rows with pauses) to populate `account_balance_v2` for historical rows.
- **Phase 4 (Contract - Read Cutover):** Switch application reads to `account_balance_v2`.
- **Phase 5 (Contract - Cleanup):** Deprecate and drop `account_balance` in a subsequent release.

### 3. Exemplary Production PR Description

```markdown
### 1. Context & Motivation
Under high concurrent payment settlement, race conditions caused duplicate debit ledger entries (Issue #842). This PR enforces strict serializability on ledger mutations and expands balance precision to support sub-cent fractional currency fees.

### 2. Changes Made
- Added `LedgerService.settlePayment()` with idempotent payment key locking.
- Wrapped transaction with Spring `@Retryable` specifically for `PSErrorCode.SERIALIZATION_FAILURE` (max 3 attempts, exponential backoff).
- Extracted Kafka event publishing outside the database transaction using Spring's `TransactionSynchronization.afterCommit()`.

### 3. Database & Migration Safety
- **Schema Migration:** Added Flyway script `V42__add_account_balance_v2.sql`.
- **Locking Analysis:** Adds nullable column `account_balance_v2 NUMERIC(18, 4)`. No full table rewrite; metadata lock duration < 50ms.
- **Expand-Contract:** Application currently dual-writes to both columns. A separate asynchronous batch migration script (`BackfillBalanceJob`) will backfill historic records before read cutover in release v2.5.

### 4. Rollback Plan
- **Safe to Rollback:** Yes. The new column is nullable, and old application versions ignore `account_balance_v2`.
- **Kill Switch:** Dynamic feature flag `ledger.serialization-retry.enabled` (Consul/AppConfig) can disable serialization retries if retry amplification occurs.
- **Rollback Trigger:** Trigger rollback if HikariCP connection pool usage exceeds 85% or p99 settlement latency exceeds 300ms for > 3 minutes.

### 5. Verification & Testing
- Added unit tests for serialization failure retry logic (`LedgerServiceRetryTest`).
- Verified zero message loss via testcontainers integration test simulating database rollback during payment settlement.
- Executed migration script on staging database (45M rows); schema alter took 38ms without blocking active read/write traffic.
```

---

## Solution: incident-status-cadence - Structure Real-Time Outage Communication

### 1. Three Severe Communication Failures
1. **Unquantified Impact and Symptoms:** Stating "DB seems slow" lacks technical precision and business context. Responders and stakeholders do not know which endpoints are affected, the error rate, or whether customer data/transactions are corrupted.
2. **Speculative and Destructive Action ("Might restart the pods"):** Restarting pods before capturing diagnostics destroys thread dumps, connection pool state, and memory metrics essential for identifying the bottleneck. Furthermore, restarting 50 pods against an overloaded database triggers a thundering-herd connection storm upon container reboot, turning a partial slowdown into a complete outage.
3. **Absence of Operational Cadence:** The message provides no estimated next check-in time or escalation lead, which forces leadership and support teams to repeatedly interrupt responders with status inquiries.

### 2. T+15 Initial Incident Update

> **[INCIDENT-UPDATE #1] SEV-1: Elevated 504 Timeouts on Checkout Service**
>
> - **Current Status:** Investigating
> - **Customer Impact:** Order checkout error rate is currently 18.4% (baseline < 0.05%). Approximately 2,100 users experienced checkout failure since 14:10 UTC. Existing placed orders and inventory reservations are unaffected.
> - **Immediate Action Underway:** On-call team is analyzing active database locks and thread dumps on the primary PostgreSQL cluster. Database CPU is at 98% with 100% HikariCP connection pool utilization.
> - **Mitigation In Progress:** Investigating slow queries via `pg_stat_activity`. Rollback for release v2.4 (deployed at 13:55 UTC) is prepared.
> - **Next Update:** 14:35 UTC (or sooner if mitigation occurs).
> - **Incident Commander:** @swapnil | Bridge: `#incident-checkout-p1`

### 3. T+30 Progress and Mitigation Update

> **[INCIDENT-UPDATE #2] SEV-1: Mitigation Applied — 504 Timeouts Recovering**
>
> - **Current Status:** Mitigating / Monitoring
> - **Customer Impact:** Checkout error rate dropped from 18.4% to 0.4% as of 14:33 UTC. Full recovery expected within 5 minutes.
> - **Root Cause Identified:** A heavy, unindexed background analytics query (`SELECT * FROM order_ledger WHERE created_at...`) deployed in v2.4 acquired table-level shared locks, exhausting the connection pool for transactional checkout requests.
> - **Mitigation Actions Completed:**
>   1. Terminated all running instances of the analytics query via `pg_terminate_backend()`.
>   2. Disabled feature flag `analytics.background_export.enabled` in Consul, stopping subsequent query dispatch.
>   3. Verified database CPU dropped from 98% to 26%; connection pool queue length returned to 0.
> - **Rollback Decision:** Full deployment rollback was cancelled because toggling the feature flag successfully mitigated the lock contention.
> - **Next Update:** 14:50 UTC with final recovery confirmation and postmortem scheduling.

---

## Solution: rfc-goals-and-alternatives - Eliminate Buzzwords and Define Boundaries

### 1. Plain Problem Statement
"The checkout endpoint (`POST /v1/orders`) synchronously calls `InventoryService.reserve()` over HTTP. When `InventoryService` experiences high latency or downtime, `OrderService` worker threads become blocked waiting for HTTP responses, causing connection pool exhaustion and cascading HTTP 504 timeouts. During peak load (>2,500 TPS), a 400ms latency degradation in `InventoryService` causes checkout availability to drop below our 99.95% SLO."

### 2. Three Goals and Three Non-Goals

**Goals:**
1. Decouple order acceptance from inventory reservation so order ingestion maintains 99.99% availability during downstream inventory service degradation or outages.
2. Bound checkout response latency (`POST /v1/orders`) to p99 < 80ms under 3,000 TPS by removing synchronous downstream HTTP network hops from the critical request path.
3. Guarantee at-least-once delivery of inventory reservation requests using the Transactional Outbox pattern, preventing order-inventory inconsistency across application crashes.

**Non-Goals:**
1. Providing instantaneous synchronous confirmation of final warehouse inventory reservation to the client (checkout transitions to an asynchronous `ORDER_ACCEPTED` state with status polling or push notifications).
2. Refactoring inventory reservation logic inside `InventoryService` or redesigning its database schema.
3. Migrating any other inter-service communications (e.g., Payment or Notification service) to asynchronous messaging in this milestone.

### 3. Structured Trade-Off Comparison

| Evaluation Metric | Option A: Transactional Outbox + Kafka | Option B: Sync HTTP with Resilience4j Circuit Breaker |
|---|---|---|
| **Consistency Model** | Eventual consistency; order accepted before inventory is definitively reserved. | Strong consistency; order is confirmed only after inventory is reserved. |
| **Availability Impact** | Maximum. Order checkout succeeds even if `InventoryService` or Kafka cluster is temporarily unreachable. | Moderate. When Circuit Breaker opens, fallback must reject orders or route to a secondary queue. |
| **Operational Complexity** | High. Requires maintaining outbox publisher (CDC/Debezium or polling relay), Kafka partitions, consumer lag monitoring, and idempotent consumers. | Low. Reuses existing HTTP infrastructure, client timeouts, and in-memory circuit breaker state. |
| **Failure Blast Radius** | Isolated. A slow consumer causes Kafka consumer lag without impacting order intake throughput. | High. A slow downstream service risks thread exhaustion across upstream callers before circuit breakers trip. |
| **Recommendation** | **Adopt Option A.** The business cost of lost orders during flash sales outweighs the complexity of eventual consistency and idempotent consumer deduplication. |

---

## Solution: code-comment-audit - Distinguish Intent and Invariants from Noise

### 1. Comment-by-Comment Audit
1. `// Check if user is null`: **Redundant Noise.** The code `if (userId == null || userId.isBlank())` is completely self-evident. Delete comment.
2. `// Increment counter by one`: **Redundant Noise.** `requestCounter.incrementAndGet()` clearly conveys an atomic increment. Delete comment.
3. `// Check if request count exceeds limit`: **Unclear Code Symptom.** The code uses a raw comparison. Replace with a well-named domain predicate: `if (isRateLimitExceeded(currentRequests))`. Delete comment.
4. `// Return false because rate limit is exceeded`: **Redundant Noise.** Returning `false` from a rate limiter method is standard and obvious. Delete comment.
5. `// Workaround for Redis driver race condition in netty-transport 4.1.92...`: **Valid Intent/Invariant (Retain).** This explains *why* standard interrupt propagation is bypassed. Without this comment, a future engineer would refactor the code to eliminate the "unnecessary" guarded block and inadvertently reintroduce socket connection leaks under thread interruption.

### 2. The Guiding Rule
> "Code tells the machine and the reader *how* and *what*; comments must explain *why* and *under what constraints*."

A comment is technically justified only when:
- It explains an external constraint (hardware, vendor bug, protocol quirk, legal requirement).
- It documents a non-obvious mathematical invariant, happens-before memory barrier, or performance trade-off.
- The rationale cannot be expressed through expressive method names, custom types, or enum constants.

### 3. Refactored Java Implementation

```java
public boolean tryAcquire(String userId) {
    if (userId == null || userId.isBlank()) {
        throw new IllegalArgumentException("User ID must not be blank");
    }

    long currentRequests = requestCounter.incrementAndGet();
    if (isRateLimitExceeded(currentRequests)) {
        return false;
    }

    // Workaround for Redis driver race condition in netty-transport 4.1.92 (Issue #1084):
    // Connection pool closes the socket prematurely during thread interruption.
    // We execute the sync pipeline in a guarded block rather than propagating interrupt.
    return executeWithGuardedSocket(userId);
}

private boolean isRateLimitExceeded(long count) {
    return count > MAX_REQUESTS_PER_MINUTE;
}
```

---

## Quick recall

**Q. Why must Kafka publishing happen outside the DB transaction?**
A. To prevent dual-write anomalies (phantom events if DB rolls back) and avoid holding open database row locks while waiting on network I/O.

**Q. Why should on-call engineers avoid premature pod restarts during an outage?**
A. Restarts destroy diagnostic state (thread dumps, heap metrics) and trigger a thundering-herd connection storm upon rebooting against an already overloaded database.

**Q. What is the primary purpose of Non-Goals in an RFC?**
A. To explicitly bound scope, eliminate assumptions, and protect engineering teams from open-ended architectural bloat.

**Q. When should an inline code comment be deleted?**
A. When it simply paraphrases what the code syntax does, rather than explaining non-obvious intent, concurrency contracts, or external bug workarounds.
