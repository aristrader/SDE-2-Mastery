---
order: 50
---

# Database Replication

Keep multiple copies of the same data on different servers. `App → DB` becomes `App → Primary → {Replica A, B, C}`. Replication can improve availability, disaster recovery, and read scalability, but every design must still answer: **when may a read be stale, and what happens if the writer fails?**

## Leader / follower (primary / replica)

Writes go to the **primary**; reads can be served from **replicas**. Flow: write hits primary → primary persists it → replicas copy the change. The standard read-scaling phrase: *"reads scale horizontally using replicas."* Writes do **not** scale this way — every write still funnels through the single primary.

Typical routing:

```text
INSERT/UPDATE/DELETE → primary
SELECT               → replica, when slightly stale reads are acceptable
```

This works well for read-heavy products. Most consumer systems read far more than they write, so adding replicas can remove a lot of load from the primary without changing the write path.

## Replication lag (and why it is not application latency)

Replication lag is the window where the primary has a new value but a replica still has the old one:

```text
T0  write arrives
T1  primary updated      ← read-from-primary sees NEW value
T2  replica updated      ← between T1 and T2, read-from-replica sees OLD value
```

Classic symptom: user updates their profile picture, then a read routed to a lagging replica shows the old picture (read-after-write violation).

**Misconception:** *async replication means the user's request is slow.*
**Correction:** async replication means *replication* lag, not *application* lag. The user gets a fast response; the staleness is on the replica, not in the request path.

## Synchronous vs asynchronous vs semi-synchronous

**Synchronous** — primary waits for the configured remote persistence or apply acknowledgement before returning success:

```text
write → primary persists → replica persists → replica ACKs → client gets success
```
This reduces acknowledged-write loss on failover, at the cost of remote-network latency and reduced write availability when the required standby is unavailable. Crucially, "sync" does **not** automatically mean linearizable reads: a later read routed to a lagging non-required replica can still be stale. It can be one, a majority, or all replicas depending on configuration; define the acknowledgement point (received, flushed, or applied) for the actual engine.

**Asynchronous** — primary returns success immediately, replicas catch up later:

```text
write → primary persists → client gets success → (later) replica updates
```
Fast writes, but a **data-loss window**:

```text
Balance 100 → 80
1. primary stores 80
2. client told SUCCESS
3. replication hasn't happened yet
4. primary crashes
→ replicas still hold 100; the acknowledged update is lost
```
The user was told "success" but the write is gone — the canonical async risk.

**Semi-synchronous** — a compromise in systems such as MySQL: the source waits for a configurable number of replicas to acknowledge receipt and durable relay-log recording. With MySQL's configurable wait point, it can wait before the source commit (`AFTER_SYNC`) or after that commit (`AFTER_COMMIT`); in both modes, the client response waits for the acknowledgement. This is stronger than asynchronous shipping but is not the same as waiting for the replica to apply and commit the transaction. A timeout can also make a source fall back to asynchronous mode, so monitor that mode change rather than assuming one static guarantee.

> **The PACELC connection:** This trade-off is exactly what the PACELC theorem describes. In the absence of network partitions (the 'E' in PACELC), a replicated system must choose between Latency (async replication) and Consistency (sync replication).

## Failover and promotion

Primary crashes → a replica is promoted → traffic redirected. **Manual** failover: an engineer promotes. **Automatic** failover: monitoring detects the failure, an election runs, a replica is promoted. The old primary must be fenced—prevented from accepting writes—before it can rejoin; otherwise a network partition can create two writable leaders (**split brain**).

Simple failure path:

```text
Primary dies
        ↓
Most advanced replica is promoted
        ↓
Applications send writes to the new primary
        ↓
A replacement replica is added and catches up
```

**Which replica wins?** Not arbitrary. In a single-leader history, replication ships an **ordered log**, and each replica tracks its position via a **Log Sequence Number (LSN) / GTID / binlog position** ("I've applied up to position X"). A failover controller chooses an eligible, most-advanced candidate to minimize the recovery point objective (RPO), then ensures the old leader cannot continue writing. Position helps choose a candidate; it is not by itself a safe election protocol.

**Misconception:** *replicas can hold arbitrary disjoint sets of writes (A has W1, B has W2, C has W3).*
**Correction:** the log is ordered, so a replica can't have W3 without W1 and W2. Real divergence looks like a prefix difference:

```text
A = W1, W2, W3   (most advanced → promoted)
B = W1, W2
C = W1
```

## Multi-leader (multi-master) replication

Multiple leaders accept writes — e.g. US / EU / Asia leaders. Benefit: local low-latency writes for global users. Cost: **write conflicts**.

Two data-distribution models:
- **Full replication everywhere** — every region stores the whole dataset. Reads are local everywhere, but conflicts are more likely.
- **Regional ownership** — each user's writes happen in their owning region (US users → US region). Far fewer conflicts.

**Conflict resolution** when two regions edit the same field (`John → Johnny` in US, `John → Jonathan` in EU):
- **Last-write-wins (LWW)** — later timestamp wins; simple, but one update silently disappears (and clock skew makes "later" fuzzy).
- **Merge** — non-overlapping changes combine (one edit changed phone, another changed address → both kept).
- **Human resolution** — git-style conflict surfaced to a person.

**Collaborative editing (for example, Google Docs):** commonly uses **Operational Transformation (OT)** or **CRDTs**, not naive LWW, so independent concurrent inserts can both survive—for example, concurrent additions can produce “Hello Beautiful Big World” rather than silently dropping one. Same-location edits still need a deterministic rebase/resolution rule. These are specialised conflict-resolution models; do not claim that ordinary multi-leader database replication automatically gives document-editor semantics.

## Leaderless replication and quorums

No primary — all nodes are equal (Cassandra, Dynamo-style systems). Benefits: high availability, no leader bottleneck. Challenge: consistency and conflict resolution move to read/write time.

- **Replication factor N** — each key stored on N nodes (e.g. N = 3).
- **Quorum write W** — write succeeds once W nodes ACK (N=3, W=2 → 2 of 3).
- **Quorum read R** — read contacts R nodes and picks the latest version it sees.

**Why `R + W > N`:** for an acknowledged write followed by a later read at those consistency levels, it forces the read set and write set to **overlap on at least one node**:

```text
N=3, W=2, R=2  →  2+2 > 3
write touches {A,B};  read touches {B,C}  →  B overlaps → latest visible
```
The coordinator still needs version/conflict metadata to reconcile replies. A weaker quorum can return stale data (N=3, W=1, R=1: write reaches only A, read from C is stale). Even `R+W>N` alone is not a blanket linearizability guarantee under concurrent writes, partitions, or clock-based conflict resolution.

**Anti-entropy / Read Repair:** If a node goes offline and misses updates, the system must synchronize it when it returns. This is often done proactively via background *anti-entropy* processes or reactively via *read repair* on a read path. Read repair is best-effort and only covers data that is actually read; scheduled anti-entropy repair closes the wider convergence gap.

## Write-Ahead Log (WAL)

Before modifying the actual data pages, the change is appended to the WAL; the data pages are updated afterward. On crash, the WAL is **replayed** to recover committed work.

- **Protects against:** process crash, OS crash, power failure (anything where the disk survives).
- **Does NOT protect against:** total machine loss or disk destruction — if the machine dies, its WAL dies with it.
- **Therefore the WAL itself must be replicated** — log shipping to replicas is how durability survives a lost node. (This is also the substrate replication rides on: replicas apply the leader's log.)

## MySQL replication specifics

Supports primary-replica, asynchronous replication, semi-synchronous replication, and **binlog** (binary log) replication. Classic MySQL does **not** use a leaderless quorum architecture — that's the Cassandra/Dynamo model, not InnoDB's default. MySQL semisynchronous acknowledgement normally means the replica has received and durably logged events, not that it has applied them; use fully synchronous group/cluster semantics only when those are actually configured.

## Quick recall

**Q. Replication lag vs application latency?**
A. Lag is replica staleness (primary updated, replica not yet); application latency is the user's request time. Async keeps requests fast but leaves replicas momentarily stale.

**Q. Sync vs async vs semi-sync trade-off?**
A. Sync can shrink acknowledged-write loss/RPO at higher latency; read freshness still needs routing. Async is fast but has a data-loss window. Semi-sync waits for configured replica receipt/logging—a middle ground.

**Q. Which replica gets promoted on failover?**
A. The most-advanced one, by ordered-log position (LSN/GTID/binlog). Logs are ordered, so divergence is a prefix difference, not arbitrary disjoint writes.

**Q. Why must `R + W > N` in a quorum system?**
A. It guarantees the read and write node sets overlap, so a read sees at least one node with the latest write — reducing (not perfectly eliminating) stale reads.

**Q. How do leaderless systems handle nodes that missed updates while offline?**
A. Through background anti-entropy processes or reactive read repair (updating the stale node when a read detects the discrepancy).

**Q. What does a WAL protect against, and what's its limit?**
A. Protects against process/OS/power crashes via replay; does not protect against losing the whole machine/disk — so the WAL itself must be replicated.

**Q. How does multi-leader handle write conflicts?**
A. LWW (later timestamp wins, loses data), merge (combine non-overlapping changes), or human resolution; collaborative editors use OT/CRDTs so concurrent edits both survive.
