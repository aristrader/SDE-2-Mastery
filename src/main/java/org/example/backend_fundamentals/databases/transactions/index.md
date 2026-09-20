---
order: 40
---

# Transactions, ACID Consistency & Distributed Commit

A transaction is a group of operations treated as one unit — either all happen or none do. This doc covers what ACID *Consistency* actually means (the most-misunderstood letter), the transaction lifecycle, and how commit works across multiple databases/services (2PC, Saga). For Spring's `@Transactional` and propagation, see `spring/transactions/Transactions.md`.

## Why transactions exist

```sql
BEGIN;
UPDATE accounts SET balance = balance - 100 WHERE id = 'A';
UPDATE accounts SET balance = balance + 100 WHERE id = 'B';
COMMIT;
```

Without atomicity, a crash after the debit but before the credit makes ₹100 vanish. The transaction guarantees both updates commit together or neither does.

## ACID Consistency — what it actually means

**Misconception:** *Consistency (the C in ACID) means all replicas hold the same data.*
**Correction:** that's **CAP / distributed consistency** (`system_design/cap_pacelc/CapPacelc.md`). ACID consistency is different.

**ACID Consistency:** a transaction moves the database from one **valid state** to another valid state, preserving all declared rules and constraints. The database *rejects* any transaction that would violate them:

- **CHECK constraint:** `salary INT CHECK (salary > 0)` → `INSERT … VALUES (1, -5000)` is rejected.
- **Foreign key:** inserting an employee in non-existent department 99 is rejected.
- Also primary-key, unique, not-null constraints, and business invariants (e.g. "balance ≥ 0").

The other three letters support this:
- **Atomicity** — all-or-nothing; a crash mid-transaction rolls back (no half-applied transfer).
- **Isolation** — concurrent transactions don't see each other's partial state (degree depends on isolation level).
- **Durability** — once committed, data survives a crash (via the WAL — see `databases/replication/Replication.md`).

**ACID consistency vs CAP consistency:** ACID = "constraints stay valid within a transaction"; CAP = "every node sees the latest write." Different concepts that share a word.

## Transaction states (lifecycle)

```text
Success:  ACTIVE → PARTIALLY COMMITTED → COMMITTED → TERMINATED
Failure:  ACTIVE → FAILED → ABORTED → TERMINATED
```

- **Active** — statements executing (`BEGIN`).
- **Partially committed** — all SQL ran, but durability work is still pending (writing WAL, flushing buffers to storage). Not yet safe.
- **Committed** — changes durably persisted.
- **Failed** — something blocked completion: duplicate PK, FK violation, deadlock, disk failure.
- **Aborted** — `ROLLBACK`; all effects undone.
- **Terminated** — final state, resources released.

The gap between *partially committed* and *committed* is exactly where durability is earned — a crash there must roll back, because the client was never told "success."

## Concurrent writes — preventing stale decisions

The engineering question is: **what if two requests read the same current value, make different decisions,
and both try to save?**

Suppose a record starts as `VERIFIED`. Two checks finish at nearly the same time:

```text
Check A reads VERIFIED and wants ERROR.
Check B reads VERIFIED and wants REVIEW.

Check A saves ERROR.
Check B saves REVIEW afterwards.
```

The final value becomes `REVIEW`, even though `ERROR` should win. This is a **lost update**: Check B made
its decision using an old value.

For a read-modify-write flow, lock when reading the record, not only when writing it:

```java
@Transactional
public void applyStatus(long id, Status incoming) {
  StatusRecord record = repository.findByIdForUpdate(id).orElseThrow();

  record.setStatus(worseOf(record.getStatus(), incoming));
}
```

`findByIdForUpdate(...)` uses `SELECT ... FOR UPDATE` (commonly exposed through JPA as
`@Lock(PESSIMISTIC_WRITE)`). The first transaction locks the record. A second transaction waits, then reads
the first transaction's committed result before calculating its own merge.

### What `@Transactional` does — and does not do

`@Transactional` makes the method's database work all-or-nothing: all writes commit together, or an
unhandled runtime exception rolls them all back by default. It does **not** automatically choose a locking
strategy or prevent lost updates.

For a mandatory concurrent update, the usual pair is:

```text
@Transactional       → keep related reads and writes in one atomic unit
SELECT ... FOR UPDATE → make competing writers wait before using stale data
```

Keep that transaction short: do not hold a database lock while making an HTTP call or doing slow work.

### Why deadlocks can still happen

InnoDB does not literally lock a column. It locks **index records**. Its primary-key index stores the row
data, so application code can usually think of this as a row lock. Updating an indexed field also changes
its secondary-index entry.

Deadlock requires a cycle:

```text
Transaction A locks X, then waits for Y.
Transaction B locks Y, then waits for X.
```

This can happen when two write paths find and update the same logical record through different indexes. The
database detects the cycle, aborts one transaction, and rolls it back.

If both updates are mandatory, make them acquire locks in one consistent order:

```text
1. Find candidate IDs without locking.
2. Lock the actual records by primary-key ID with FOR UPDATE.
3. For multiple records, lock IDs in ascending order.
4. Recheck the condition after acquiring the lock.
```

The recheck matters because a row may change between the first lookup and the locked read.

### When `SKIP LOCKED` is the better answer

Some background work can safely be delayed:

```sql
SELECT id
FROM work_items
WHERE state = 'PENDING'
FOR UPDATE SKIP LOCKED;
```

Busy records are skipped rather than waited for. The worker processes rows it successfully claimed; another
run picks up skipped rows later. This avoids a wait cycle and lets the batch make partial progress.

Do not use `SKIP LOCKED` for mandatory operations such as transferring money or reserving the last item. In
those cases, wait for the lock, then read the latest state and decide again.

### Related races need different tools

| Situation | Correct primary defense |
|---|---|
| Two updates read the same old record, then overwrite each other | `SELECT ... FOR UPDATE` / `PESSIMISTIC_WRITE` around the read-modify-write sequence |
| Two transactions lock shared records in opposite order | Acquire records in a consistent order; retry the deadlock victim if needed |
| Several application pods run the same scheduled job | Distributed scheduler lock or one external scheduler leader |
| Two requests both see “no row exists” and insert the same key | Unique database constraint; handle the duplicate conflict by loading/retrying safely |

## Distributed transactions

Within one database, commit is easy: one log, one lock manager, one recovery system. Across services it's hard:

```text
Order DB · Inventory DB · Payment DB   →   need ALL commit or ALL rollback
```

A checkout that creates the order, reserves stock, then *fails* on payment leaves an inconsistent state. Multiple machines + network failures + timeouts + retries make coordination genuinely difficult.

### Two-Phase Commit (2PC)

A coordinator drives two rounds:

1. **Prepare** — coordinator asks every participant "can you commit?" Each votes Yes (and durably prepares) or No.
2. **Commit** — if *all* voted Yes, coordinator says "commit"; everyone commits. If *any* voted No, coordinator says "rollback" everywhere.

2PC gives atomicity across nodes but is **slow and fragile**: it holds locks across the network for the whole protocol, and if the coordinator crashes after Prepare, participants are **blocked** holding locks (the classic 2PC blocking problem).

### Why microservices avoid distributed ACID — Saga

Distributed ACID across services is slow, complex, and fragile, so teams use the **Saga pattern**: a sequence of *local* transactions, each with a **compensating transaction** that undoes it. If a later step fails, run the compensations for the completed steps instead of rolling back one giant distributed transaction:

```text
Create order ✔   →   Charge card ✔   →   Reserve inventory ✘
Compensate:  Refund payment, Cancel order
```

This trades atomicity for availability and eventual consistency — the system passes through visible intermediate states, then converges. (Saga / Outbox / compensating transactions revisited in Part 7 and the design-patterns material.)

**Deep dive:** the 2PC blocking problem, 3PC, sharding-makes-transactions-distributed, Saga compensation-failure handling, Kafka delivery semantics, and idempotency/effectively-once are covered in `databases/distributed_transactions/DistributedTransactions.md`.

## Quick recall

**Q. What does the C in ACID mean?**
A. A transaction takes the DB from one valid state to another, preserving all constraints/invariants (PK, FK, unique, CHECK, business rules) — the DB rejects violating transactions. Not "replicas agree."

**Q. ACID consistency vs CAP consistency?**
A. ACID = constraints stay valid through a transaction. CAP = all nodes see the latest write. Same word, different concepts.

**Q. What's the "partially committed" state?**
A. All statements executed but durability work (WAL write, buffer flush) is still pending — a crash here rolls back; only after durability is it "committed."

**Q. How does 2PC work and what's its weakness?**
A. Prepare (collect Yes/No votes) then Commit (or rollback if any No). Weakness: holds locks across the network and blocks participants if the coordinator crashes after prepare.

**Q. Why do microservices prefer Saga over distributed transactions?**
A. Distributed ACID/2PC is slow, fragile, and lock-heavy; Saga uses local transactions + compensating actions for failures, trading atomicity for availability and eventual consistency.

**Q. What happens to a transaction on deadlock mid-way?**
A. It fails → aborts → rolls back entirely; statements that "succeeded" earlier are undone because COMMIT was never reached.

**Q. Does `@Transactional` alone prevent a lost update?**
A. No. It gives atomic commit/rollback. Use a pessimistic lock or an optimistic version check when two
requests can read and then modify the same state.

**Q. When is `SKIP LOCKED` appropriate?**
A. For deferrable background work: skip busy records and retry later. Mandatory balance, inventory, or
payment changes should wait, then recheck the latest state.
