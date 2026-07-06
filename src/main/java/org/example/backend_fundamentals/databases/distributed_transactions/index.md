---
order: 10
---

# Distributed Transactions — 2PC, 3PC & Saga

A single-DB transaction is easy: one authority owns the WAL, locks, recovery, and commit protocol. The moment a transaction spans multiple databases/services/shards, no single authority exists, and you need a protocol to keep *all-succeed-or-all-fail* across nodes. This is the deep dive; for single-DB ACID and the transaction lifecycle see `databases/transactions/Transactions.md`.

## Why it's hard

A checkout touching Order DB + Inventory DB + Payment DB can land in `Order ✓ / Inventory ✓ / Payment ✗` — inconsistent. Cross-node atomicity fights four realities a single DB doesn't have:

- **Network failures** — messages lost between nodes.
- **Partial crashes** — A alive, B dead.
- **Slow responses** — a node that's just slow looks like a dead one.
- **No global clock** — no node can perfectly order events across machines.

The bank-transfer failure modes make the stakes concrete: debit-succeeds/credit-fails *destroys* money; credit-succeeds/debit-fails *creates* money. You need both or neither.

## Two-Phase Commit (2PC)

A **coordinator** drives **participants** through two rounds:

**Phase 1 — Prepare:** coordinator asks "can you commit?" Each participant executes locally, **locks resources, writes its state to a durable log, but does NOT commit**, then votes YES/NO.

**Phase 2 — Commit:** if *all* voted YES, coordinator broadcasts COMMIT and everyone commits. If *any* voted NO, coordinator broadcasts ROLLBACK.

**Why it's correct:** nobody commits during Phase 1 — a YES vote is only a *promise* ("I'm ready"). The actual commit happens only after the coordinator confirms global agreement, so participants can't diverge.

## The 2PC blocking problem

The easy failure is a participant dying *before* voting YES — the coordinator just times out and rolls back. The dangerous case is different:

```text
A → YES, B → YES, C → YES     (all now in PREPARED: executed, locks held, not committed)
Coordinator CRASHES
→ A, B, C all wait. COMMIT or ROLLBACK? Nobody knows.
```

- **Participants can't decide for themselves.** If A committed and B rolled back, atomicity breaks. They must reach the *same* decision, so they block.
- **Is it blocked forever?** Theoretically yes — if the coordinator never returns, participants stay PREPARED with **locks held**, resources unavailable.
- **Real recovery:** the coordinator writes its decision to durable storage *before* acting (`PREPARE_LOG`, then `GLOBAL_COMMIT`). On restart it reads the log, recovers state, and resumes — re-notifying participants.
- **Coordinator dies *after* deciding commit:** say it logged `GLOBAL_COMMIT`, told A and B (who commit), then died before telling C. Temporarily inconsistent (C still waiting), but on restart the coordinator reads `GLOBAL_COMMIT` and re-sends COMMIT to C → system converges.
- **Participants persist PREPARED too** (`txn 123, state=PREPARED` on disk) so a crashed-and-restarted participant remembers it's still waiting.
- **If the coordinator never returns:** a human DBA inspects logs and resolves manually.

**Why engineers dislike 2PC:** not incorrectness — it's that **locks are held across the network** for the whole protocol. Transactions wait, availability drops, throughput suffers.

## Sharding makes transactions distributed

**Misconception:** *a sharded DB is still one database, so why would I need distributed transactions?*
**Correction:** a shard is often a **separate physical database server**. Two users on the *same* shard → an ordinary local transaction. Two users on *different* shards → a transaction spanning two physical DBs, which needs coordination — effectively 2PC:

```text
coordinator.begin()
shardA.prepare(debit);  shardB.prepare(credit)
if all_yes:  shardA.commit(); shardB.commit()
else:        rollback()
```

**Misconception:** *Postgres supports transactions, so I'm fine.* Only *within one instance*. Across multiple Postgres nodes, Postgres alone can't guarantee atomicity. Systems like **CockroachDB, Google Spanner, and YugabyteDB** expose one logical `BEGIN…COMMIT` but internally use **consensus protocols (like Paxos or Raft)**, distributed-commit protocols, and timestamp ordering — the complexity still exists; the DB just hides it.

## Three-Phase Commit (3PC)

3PC inserts a **PRE-COMMIT** phase to attack 2PC's blocking:

```text
PREPARE  →  PRE-COMMIT  →  COMMIT
```

In 2PC, the PREPARED state doesn't tell a participant whether the coordinator *intended* commit or rollback — so on coordinator death they're stuck. In 3PC, receiving **PRE-COMMIT** tells participants "global agreement exists, commit is coming." If the coordinator then disappears, participants can **time out and commit on their own**, reducing blocking.

**Why 3PC is rarely used:**
1. **More round trips** — three phases = more latency, messages, complexity.
2. **Strong timing assumptions** — it relies on bounded network delay, bounded timeouts, and reliable failure detection. Real networks provide none of these.
3. **Network partitions defeat it** — if A can reach the coordinator but B can't, B thinks the coordinator died while the coordinator thinks B died. **A slow node is indistinguishable from a dead node (the FLP problem)**, so timeout-based self-decisions can still produce inconsistency. 3PC trades 2PC's blocking for partition-time incorrectness — usually a bad trade. Modern systems bypass 3PC entirely by using consensus protocols like Paxos or Raft to handle leader failure instead of timeouts.

## Saga pattern

Modern systems avoid one giant distributed ACID transaction. A **Saga** is a sequence of **local** transactions, each with a **compensating action** that undoes it. On failure, run the compensations for the completed steps:

```text
Create Order ✓  →  Reserve Inventory ✓  →  Charge Payment ✗
Compensate:  Release Inventory,  Cancel Order
```

Saga does **not** give instant consistency — it gives **eventual consistency**. The system passes through visible intermediate states, then converges.

### When compensations themselves fail

This is the part people miss. If "Release Inventory" fails because the inventory service is down, the saga coordinator records the compensation as **pending** and retries:

```text
OrderId=123, Action=ReleaseInventory, Status=Pending   → retried by a worker later
```

A background worker loops: pick pending task → execute → mark complete, or retry-later on failure. If retries keep failing → **dead-letter queue + alert + human investigation**. Failures aren't eliminated; they're *managed*.

**Compensations must be idempotent** — safe to run many times. `inventory += 1` run twice corrupts state; `release_reservation(order_id)` that returns success if already released is safe to retry forever.

### Saga via Kafka (choreography)

Event-driven sagas: create order → publish event; inventory service consumes → publishes its event; payment service consumes. On payment failure, **compensation events** are published and order/inventory roll back via their own consumers. (This is *choreography* — no central orchestrator; each service reacts to events.)

## Kafka delivery semantics & idempotency

**Misconception:** *Kafka removes a message once a consumer receives it.*
**Correction:** Kafka is a **log**, not a traditional queue. Messages stay; **consumers track offsets**:

```text
offsets: 0 1 2 3 …   consumer stores "current offset"; committing offset=2 means "everything < 2 processed"
```

**Consumer crash window:** if a consumer processes a message but crashes *before committing the offset*, Kafka still thinks it's unprocessed and **redelivers** it → duplicate processing (`inventory 10→9`, crash, redeliver, `9→8` — wrong). The fix is **idempotency**: tag each message with a transaction id, store processed ids, and skip duplicates:

```text
if txn_id already processed: return success
else: process(); mark_processed()
```

**Payment example:** charge succeeds, service crashes before publishing `PaymentSuccessful`, retry → double charge. Fix with an **idempotency key** (`PAY_123`): the payment provider sees the duplicate key and returns the previous result instead of charging again.

### Exactly-once is (usually) a myth

**Misconception:** *exactly-once processing is generally guaranteed.*
**Correction:** usually you don't get true exactly-once across system boundaries. The standard recipe is:

```text
at-least-once delivery  +  idempotent processing  =  effectively-once
```

Messages may arrive multiple times; the system is designed so duplicates are harmless. And the broader lesson: **distributed systems don't eliminate failure windows — they're designed to recover around them.** Seeing failure windows everywhere is expected.

## Quick recall

**Q. What does 2PC's prepare phase actually do?**
A. Each participant executes locally, locks resources, durably logs its state, and votes YES/NO — but does NOT commit. Commit happens only in phase 2 after global agreement.

**Q. What's the 2PC blocking problem?**
A. After all vote YES, if the coordinator crashes, participants sit in PREPARED holding locks and can't self-decide (would break atomicity) — blocked until the coordinator recovers (via durable log) or a human intervenes.

**Q. Why is sharding relevant to distributed transactions?**
A. Shards are separate physical servers; a transaction across shards spans multiple DBs and needs a distributed-commit protocol — single-instance Postgres transactions don't cover it.

**Q. How does 3PC reduce blocking, and why is it rarely used?**
A. PRE-COMMIT tells participants commit was agreed, so they can time out and commit themselves. Rarely used: extra round trips, strong timing assumptions, and it can still be incorrect under partitions (slow node = dead node).

**Q. What does Saga guarantee, and how does it handle compensation failure?**
A. Eventual consistency via local transactions + compensating actions. Failed compensations are retried (pending state → worker → DLQ → human), and must be idempotent.

**Q. How do you get "exactly-once" in practice?**
A. You usually don't — use at-least-once delivery + idempotent processing (dedup by id / idempotency keys) = effectively-once.

**Q. Why does a Kafka consumer reprocess messages after a crash?**
A. Kafka retains messages and tracks offsets; a crash after processing but before committing the offset causes redelivery — so consumers must be idempotent.


