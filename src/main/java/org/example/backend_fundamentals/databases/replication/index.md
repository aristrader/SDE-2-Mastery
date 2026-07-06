---
order: 50
---

# Database Replication

Keep multiple copies of the same data on different servers. `App → DB` becomes `App → Primary → {Replica A, B, C}`. Buys you higher availability, disaster recovery, read scalability, and reduced load on a single node.

## Leader / follower (primary / replica)

Writes go to the **primary**; reads can be served from **replicas**. Flow: write hits primary → primary persists it → replicas copy the change. The standard read-scaling phrase: *"reads scale horizontally using replicas."* Writes do **not** scale this way — every write still funnels through the single primary.

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

**Synchronous** — primary waits for replica acknowledgement before returning success:

```text
write → primary persists → replica persists → replica ACKs → client gets success
```
Strong consistency, minimal data loss, higher write latency. Crucially, "sync" does **not** have to mean *all* replicas — it can be one, a majority, or all, depending on config. The write isn't committed until the *required* number of replicas ACK.

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

**Semi-synchronous** — the common compromise: primary waits for **at least one** replica (primary + 1) before returning success. Shrinks the data-loss window without paying the full all-replicas latency cost.

> **The PACELC connection:** This trade-off is exactly what the PACELC theorem describes. In the absence of network partitions (the 'E' in PACELC), a replicated system must choose between Latency (async replication) and Consistency (sync replication).

## Failover and promotion

Primary crashes → a replica is promoted → traffic redirected. **Manual** failover: an engineer promotes. **Automatic** failover: monitoring detects the failure, an election runs, a replica is promoted.

**Which replica wins?** Not arbitrary. Replication ships an **ordered log**, and each replica tracks its position via a **Log Sequence Number (LSN) / GTID / binlog position** ("I've applied up to position X"). The **most-advanced replica** (highest position) is promoted, minimizing lost writes.

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

**Collaborative editing (Google Docs):** uses **Operational Transformation (OT)** or **CRDTs**, not naive LWW, so concurrent inserts both survive ("Hello Beautiful Big World"). Edits only disappear when the *same* location is edited simultaneously and network lag forces a rebase.

## Leaderless replication and quorums

No primary — all nodes are equal (Cassandra, Dynamo-style systems). Benefits: high availability, no leader bottleneck. Challenge: consistency and conflict resolution move to read/write time.

- **Replication factor N** — each key stored on N nodes (e.g. N = 3).
- **Quorum write W** — write succeeds once W nodes ACK (N=3, W=2 → 2 of 3).
- **Quorum read R** — read contacts R nodes and picks the latest version it sees.

**Why `R + W > N`:** it forces the read set and write set to **overlap on at least one node**, so a read always sees at least one copy of the latest write:

```text
N=3, W=2, R=2  →  2+2 > 3
write touches {A,B};  read touches {B,C}  →  B overlaps → latest visible
```
Stale reads still happen when the quorum is too weak (N=3, W=1, R=1: write reaches only A, read from C is stale). And even `R+W>N` isn't a hard guarantee under network partitions, concurrent writes, or clock skew.

**Anti-entropy / Read Repair:** If a node goes offline and misses updates, the system must synchronize it when it returns. This is often done proactively via background *anti-entropy* processes or reactively via *read repair* (when a read detects a stale replica, it forces an update).

## Write-Ahead Log (WAL)

Before modifying the actual data pages, the change is appended to the WAL; the data pages are updated afterward. On crash, the WAL is **replayed** to recover committed work.

- **Protects against:** process crash, OS crash, power failure (anything where the disk survives).
- **Does NOT protect against:** total machine loss or disk destruction — if the machine dies, its WAL dies with it.
- **Therefore the WAL itself must be replicated** — log shipping to replicas is how durability survives a lost node. (This is also the substrate replication rides on: replicas apply the leader's log.)

## MySQL replication specifics

Supports primary-replica, asynchronous replication, semi-synchronous replication, and **binlog** (binary log) replication. Classic MySQL does **not** use a leaderless quorum architecture — that's the Cassandra/Dynamo model, not InnoDB's default.

## Quick recall

**Q. Replication lag vs application latency?**
A. Lag is replica staleness (primary updated, replica not yet); application latency is the user's request time. Async keeps requests fast but leaves replicas momentarily stale.

**Q. Sync vs async vs semi-sync trade-off?**
A. Sync = strong consistency, higher latency, minimal data loss. Async = fast writes, possible data-loss window. Semi-sync = wait for at least one replica — a middle ground.

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


