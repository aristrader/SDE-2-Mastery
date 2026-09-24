---
order: 40
---

# Transactions: ACID, Isolation, and Recovery

An order transfer must not debit one account without crediting the other. It must also stay correct when two requests arrive together and when the database crashes. A **transaction** is the database's boundary for that one business change: it either commits all of its changes or makes none of them visible.

This page is about one database. When one outcome spans independent databases or services, read [distributed transactions](../distributed_transactions/) after this local model. For Spring proxy and propagation mechanics, read [Spring transactions](../../spring/spring_core/transactions/).

## Start with the invariant, then choose the guarantee

An **invariant** is a rule that must be true in every committed state: a balance is not negative, an order has one unique reference, or at least one clinician remains on call. The four ACID properties protect different ways that rule can fail.

| Property | What it promises | What it does not promise |
| --- | --- | --- |
| Atomicity | All statements in the transaction commit, or all roll back. | That concurrent requests made a safe decision. |
| Consistency | A commit preserves declared constraints and the application's invariant. | That replicas have caught up; that is CAP/distributed consistency. |
| Isolation | Concurrent transactions behave according to the selected visibility/conflict rules. | That every engine gives the same behaviour for the same named level. |
| Durability | After a successful commit acknowledgement, recovery can preserve the committed change after a crash. | That the change is already visible on every asynchronous replica. |

The database can enforce primary-key, unique, foreign-key, `NOT NULL`, and `CHECK` constraints. It cannot infer every business rule. For example, `balance >= 0` needs a constraint or an update predicate; “one doctor must stay on call” needs a transaction design that coordinates the rows the rule depends on.

**Do not confuse the two C's.** ACID consistency means a transaction takes the database from one valid state to another. CAP consistency concerns what different nodes can observe. A transaction may preserve its local invariant even while a read replica is behind.

## Normal path: a transfer earns its commit

```sql
BEGIN;
UPDATE accounts SET balance = balance - 100
WHERE id = 'A' AND balance >= 100;
UPDATE accounts SET balance = balance + 100
WHERE id = 'B';
COMMIT;
```

Require each update to affect exactly one row; otherwise roll back. That means a missing destination account cannot commit a debit by itself. The debit predicate also makes “never overdraw A” database-enforceable without a prior Java read.

The usual lifecycle is:

```text
ACTIVE → statements succeed → COMMIT requested → COMMITTED → resources released
   │                                  │
   └── error/deadlock/cancel → ROLLBACK ─────────────────────┘
```

“All SQL statements returned successfully” is not the same thing as a committed result. The engine records enough recovery information before it acknowledges the commit. Write-ahead logging (WAL) records the change before the corresponding data page must reach disk, so crash recovery can replay committed changes. In PostgreSQL, transaction status keeps incomplete transaction versions invisible; later cleanup removes them rather than performing an ARIES-style undo pass. Exact flush and recovery mechanics are engine and configuration specific.

## Isolation: name the anomaly before choosing a level

Isolation controls what one transaction can observe while others run. Anomaly names are useful, but named levels are **minimum contracts, not portable implementations**. PostgreSQL, for example, treats `READ UNCOMMITTED` as `READ COMMITTED`, and its `REPEATABLE READ` is stronger than the SQL minimum. Verify the selected engine's documentation before relying on a matrix.

| Situation | Naive failure | Typical control |
| --- | --- | --- |
| Read an uncommitted change that later rolls back | Dirty read | Read committed or stronger. |
| Read a row twice and get two committed values | Non-repeatable read | Stable snapshot / repeatable-read semantics. |
| Re-run a predicate and see a newly matching row | Phantom | Serializable semantics or an engine-specific range/predicate strategy. |
| Read a value, calculate in Java, then overwrite a newer value | Lost update | Atomic SQL, a lock, or optimistic version check. |
| Two transactions update different rows after both validate a cross-row rule | Write skew | Serializable transaction or explicit coordination of the invariant. |

### The four standard names, used safely

| Requested level | Mental model | Interview boundary |
| --- | --- | --- |
| Read uncommitted | May expose uncommitted writes in engines that implement it. | Do not assume it behaves identically across engines. |
| Read committed | An ordinary query sees data committed before that statement began. | A later statement can see a newer value; a blocked `UPDATE`/`DELETE` may re-evaluate its predicate against a newly committed row version. |
| Repeatable read | The transaction gets a stable read view in many engines. | Snapshot isolation can still permit write skew; engine behaviour varies. |
| Serializable | The result is equivalent to some one-at-a-time ordering, or the engine aborts a transaction. | The application must retry an aborted transaction from the beginning. |

### Deep dive: the “one doctor remains” rule

**Problem.** Two doctors are on call. A doctor may go off call only while another remains.

**Naive failure.** Transaction A reads “B is on call” and turns A off. At the same time, B reads “A is on call” and turns B off. They update different rows, so ordinary write conflict detection may not see a collision. Both commits leave nobody on call: the invariant failed even though every individual update was valid.

**Mechanism.** Use a serializable transaction, or explicitly lock one stable coordination resource and recheck the rule after acquiring it. Serializable engines detect a schedule that cannot be explained as one-at-a-time execution and abort a participant; an explicit lock makes competing flows wait before their final decision.

**Trade-off.** Serializable work can abort under contention; locking reduces parallelism and long transactions make waits worse. Do not raise an isolation level as a reflex when a single conditional update or a narrow coordination lock states the invariant more directly.

**Recovery.** Retry the *whole* transaction after a serialization failure or deadlock, with a small bounded backoff. Re-running only its final `UPDATE` reuses an invalid earlier decision. The operation must be idempotent or protected by a request key so a client retry cannot create a second business action.

## The right protection for read-modify-write

`@Transactional` makes related database work atomic; it does not automatically prevent a stale read-calculate-write decision. Consider two status checks:

```text
A reads VERIFIED; decides ERROR.
B reads VERIFIED; decides REVIEW.
A writes ERROR and commits.
B writes REVIEW using its old decision.
```

The final `REVIEW` is a **lost update** if `ERROR` should win. Pick the smallest mechanism that captures the rule.

| Rule shape | Prefer | Why |
| --- | --- | --- |
| Increment/decrement one value | One atomic SQL statement | The database evaluates `amount = amount + :delta` while writing. |
| One mutable record with expected rare conflict | Optimistic version (`WHERE version = :oldVersion`) | One writer wins; the loser detects zero updated rows and retries. |
| Several reads/writes must make one decision | Lock the coordination row before reading, then recheck | A competing writer waits and sees the latest committed state. |
| Cross-row invariant | Serializable transaction or an explicit stable coordination lock | Updating different rows alone does not protect the combined rule. |

```java
@Transactional
public void applyStatus(long id, Status incoming) {
  StatusRecord record = repository.findByIdForUpdate(id).orElseThrow();
  record.setStatus(worseOf(record.getStatus(), incoming));
}
```

Here `findByIdForUpdate` commonly maps to `SELECT ... FOR UPDATE` / JPA `PESSIMISTIC_WRITE`. The lock is acquired by the query but lives until this transaction commits or rolls back. The second caller then reads the first caller's result before calculating its own merge. Keep HTTP calls and slow work outside that lock window.

An atomic update is simpler when it truly expresses the rule:

```sql
UPDATE balances
SET amount = amount + :delta
WHERE id = :id AND amount + :delta >= 0;
```

It is not a replacement for locking when the decision needs several rows or non-SQL business logic.

## Failure and recovery: deadlock, contention, and worker claims

### Deadlock

**Problem.** A transfer touches two accounts.

**Naive failure.** A locks account X then waits for Y, while B locks Y then waits for X. Neither can proceed.

**Mechanism.** The engine detects the cycle and aborts one victim; it rolls that transaction back. Some engines lock index records or ranges rather than an abstract “row,” so the exact lock footprint is engine/query-plan specific.

**Trade-off.** A lock gives clear coordination but lowers concurrency; broad predicates and long transactions expand the contested footprint.

**Recovery.** Always obtain multiple records in a deterministic order—for example, ascending account ID—then recheck the condition under the lock. Keep transactions short and retry only transient deadlock/serialization failures with a bounded policy. A retry is normal control flow, not a reason to expose a 500 immediately.

### `SKIP LOCKED` is for deferrable work

```sql
SELECT id
FROM work_items
WHERE state = 'PENDING'
ORDER BY id
FOR UPDATE SKIP LOCKED
LIMIT :batchSize;
```

**Problem.** Several workers need to claim independent jobs without queueing behind one slow job.

**Naive failure.** Waiting on the first locked row wastes workers and can create a convoy.

**Mechanism.** `SKIP LOCKED` skips rows another transaction currently holds; a worker processes only rows it successfully claimed.

**Trade-off.** It is an intentionally inconsistent queue view: a busy item is not returned in that scan. It is not appropriate for a mandatory reservation, balance change, or “last seat” decision.

**Recovery.** In the same transaction, change each claimed row to an in-progress state, then commit promptly; a later worker can scan skipped work. Add a lease/timeout and retry policy for work abandoned after a worker crash.

## Boundary: one database versus many authorities

One database owns its log, lock manager, and recovery. Several databases/services do not. **Two-phase commit (2PC)** asks every participant to durably prepare, then tells all to commit only if every participant voted yes. It provides a single outcome, but prepared participants can hold resources while a coordinator is unavailable.

A **Saga** instead chains local transactions and later performs compensating business actions when a later step fails. It trades immediate all-or-nothing atomicity for availability and eventual convergence. Compensation is not an invisible rollback: a refund is a new business event and must handle retries/idempotency. The full problem—outbox delivery, compensation failure, choreography, and orchestration—belongs in [distributed transactions](../distributed_transactions/), not inside a local transaction.

## Interview delivery

“First I state the invariant and keep all database changes for that invariant in one short transaction. `@Transactional` gives atomic commit/rollback, but I separately choose concurrency control: an atomic conditional update for one-row arithmetic, a version check for rare collisions, or a lock/serializable transaction when the decision spans shared state. I expect deadlock or serialization aborts and retry the whole idempotent operation. For another service's database, I do not pretend a local transaction reaches it; I use an explicit distributed protocol or a Saga with compensation.”

## Quick recall

**Q. What does ACID consistency mean?**
A. Every committed transaction preserves declared constraints and application invariants; it is not replica agreement.

**Q. Why is `@Transactional` insufficient for a lost update?**
A. It makes this caller's writes atomic, but it does not stop another caller from calculating from an older read.

**Q. What should happen after a serialization failure?**
A. Retry the entire idempotent transaction from its first read, usually with a bounded backoff.

**Q. Why can repeatable read still be insufficient for a business rule?**
A. A stable snapshot can still allow write skew when concurrent transactions change different rows that share one invariant.

**Q. When is `SKIP LOCKED` appropriate?**
A. For deferrable worker claims; not for a mandatory business decision that must wait and re-evaluate.

**Q. What makes 2PC different from a local transaction?**
A. It coordinates independent authorities, so failures can block prepared participants and hold resources across the network.
