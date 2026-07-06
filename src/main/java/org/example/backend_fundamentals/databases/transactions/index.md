---
order: 80
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


