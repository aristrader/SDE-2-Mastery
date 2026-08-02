---
order: 60
---

# Sharding, Data Partitioning & Consistent Hashing

Partitioning breaks a database into smaller parts spread across machines for manageability, performance, and availability. Sharding is the horizontal-partitioning flavor that scales writes/storage across servers. Consistent hashing is how you partition without remapping everything when the cluster changes size.

## Data partitioning: horizontal vs vertical

- **Horizontal partitioning (= sharding):** split **rows**. Each partition has the *same schema/columns* but a disjoint subset of rows (users 1–2 on shard 1, users 3–4 on shard 2).
- **Vertical partitioning:** split **columns**. `User(id, name, email, address)` → partition A `(id, name)`, partition B `(id, email, address)`. (Don't confuse with normalization — vertical partitioning is a physical split for I/O, not a dependency-driven one.)

## Sharding

A database architecture pattern based on horizontal partitioning: one logical table's rows are split across multiple shards, every shard has the **same schema** and a **different subset** of the data. It exists because vertical scaling (bigger CPU/RAM/disk) gets expensive and hits a ceiling — sharding scales **horizontally** by adding servers.

**Key production insight:** a shard is usually an **entire independent database cluster** (its own primary + replicas), not just a table partition on one box.

### Replication vs Sharding
Many people confuse these, but they solve different problems and are usually used together:

| Replication | Sharding |
|------------|-----------|
| Copies data | Splits data |
| Improves availability | Improves scalability |
| Same dataset everywhere | Different data on each node |
| Good for reads | Good for storage and throughput |

So **sharding scales writes + storage**; **replication scales reads + availability** — they're orthogonal and used together.

## Partitioning strategies (criteria)

| Strategy | How | Trade-off |
|----------|-----|-----------|
| **Hash** | `shard = hash(key) % N` | Even distribution; but changing N remaps most keys |
| **List** | predefined value lists (shard 1 = {India, Pakistan}) | Explicit control; manual, can skew |
| **Range** | contiguous, non-overlapping ranges (ids 1–1000 → shard 1) | Great for range scans; monotonic keys create hot shards |
| **Composite** | combine methods (range, then hash within each range) | Flexibility at the cost of complexity |

## Choosing a shard key

The shard key is the field that decides data placement (`user_id`, `tenant_id`, `customer_id`). A good key is:

- **Evenly distributed** — avoids hot shards.
- **Stable** — doesn't change (you don't want rows migrating).
- **Present in common queries** — so the router can route without extra lookups.

**Bad keys:** `country` / `city` / `gender` — low cardinality and skewed. `country` makes India a massive hot shard while Iceland is nearly empty. `user_id % N` is popular precisely because it spreads load evenly.

## Application-level vs database-level sharding

- **Application-level:** the app computes the shard (`shard = user_id % 3`) and queries only that DB; the database is unaware it's sharded. Works with *any* database, flexible — but the app must own routing logic + shard locations (more complexity). Connecting one app to many databases is normal and fully supported.
- **Database-level:** the app issues a normal query; a **router** decides the shard. The app never knows where data lives. Examples: **MongoDB `mongos`** (routes `db.users.find({userId:123})` to the right shard), **Vitess `VTGate`** for MySQL (the app thinks it's one database).

Note: making `users_1`, `users_2`, `users_3` tables *in the same Postgres instance* is **not** sharding — they still share one CPU/RAM/disk/connection pool. Real sharding spreads data across machines.

## Query routing and the non-shard-key problem

If the query carries the shard key, routing is direct: `GET /users/123` → router → shard → table → index → row.

If it doesn't (e.g. login by email when the shard key is `user_id`), the router can't tell which shard holds it. Two options:

1. **Scatter-gather** — query *all* shards and combine (also how `SELECT COUNT(*) FROM users` works, since no shard has everyone). Expensive; latency = slowest shard.
2. **Lookup table** — a secondary index `email → user_id`; resolve the key first, then route normally. Caveat: the lookup table itself can become huge (5B users = 5B mappings) → mitigate with caching, replication, or sharding the lookup service. Best fix overall: pick a shard key present in most queries.

## Shard key vs index — different layers

**Misconception:** *the shard key behaves like an index.*
**Correction:** they solve different problems. **Shard key → which machine.** **Index → which row inside that machine.** Library analogy: shard key picks the building; the index picks the shelf and book. A full lookup chains them: `(lookup table →) shard key → router → shard → index → row`.

## When to shard (and the costs)

**Use when:** a single DB is the bottleneck — write/storage scaling, more concurrent connections, geographic data separation, fast scaling on existing hardware.

**Costs:** operational complexity; **cross-shard joins** (users on shard A, orders on shard B) need multi-server queries and perform poorly; multi-shard **transactions** become complex (may require two-phase commit); and **rebalancing** when load skews (shard A 80%, B/C 10%) means expensive data migration. Shard last — after indexing, caching, read replicas, and vertical scaling.

### Sharding failure modes interviewers expect

- **Resharding:** a shard outgrows disk/CPU or the hash function no longer spreads load well. You must change placement and move data.
- **Hotspot key:** one key or tenant gets extreme traffic and overloads its shard even if total data is balanced.
- **Cross-shard joins:** once related rows live on different machines, joins become scatter-gather or require denormalized read models.
- **Lookup path drift:** queries that do not include the shard key become slow unless you maintain a secondary lookup table.

The practical answer is not "shard everything." It is: pick a shard key that matches dominant access patterns, keep data needed together on the same shard when possible, and denormalize intentionally for hot reads.

## Consistent hashing

**The modulo problem:** with `hash(key) % N`, going from N=4 to N=5 remaps **most** keys → mass data migration + cache misses. Consistent hashing fixes this.

- **Hash ring:** map the hash space onto a circle. Hash each *server* onto the ring (A=100, B=400, C=700).
- **Key placement:** hash the key, walk **clockwise**, the first server encountered owns it (key 450 → server C).
- **Adding a node:** insert D at 500 → only keys in (400, 500] move (from C to D); everything else stays. **Removing a node:** only that node's keys move to the next clockwise node. Cluster-size changes touch ~1/N of keys, not all.
- **Virtual nodes:** hashing each physical server to *one* point causes skew (if C lands at 900 it owns a huge arc). Give each server many positions (A1, A2, A3, …) scattered around the ring → far more even load and smoother rebalancing.

### Affected range on add/remove

When a node is added, the affected keys are the range between the new node and the previous node counter-clockwise. Those keys move to the new node.

When a node is removed, the affected keys are the range owned by the removed node. Those keys move to the next node clockwise.

This is the interview explanation behind "only a fraction of keys move."

### What consistent hashing is and is not for

| Use case | Fit? | Better framing |
|----------|------|----------------|
| Distributed cache key placement | Yes | Keeps cache remap small when nodes change |
| Dynamo/Cassandra-style partitioning | Yes | Places key ranges/partitions on storage nodes |
| Load balancer affinity | Yes | Keeps a client/key near the same backend with limited remap |
| CDN/server routing | Sometimes | Routing/load distribution use case |
| Text search | No | Use inverted index / search engine |
| Autocomplete | No | Use trie/FST/prefix index + ranking |
| Nearby cab/driver search | No by itself | Use geohash/S2/H3/quadtree for spatial partitioning; consistent hashing may distribute geospatial cells across machines afterward |

**Used in:** Redis Cluster-style caches, distributed caches, Dynamo/Cassandra-style stores, load balancers, sticky routing, some CDN/server-routing systems. (Cache-lens treatment: `system_design/components/caching/index.md`.)

## Quick recall

**Q. Sharding vs vertical partitioning?**
A. Sharding = horizontal partitioning (split rows, same schema across shards). Vertical partitioning = split columns into separate stores.

**Q. What's a shard, physically?**
A. Usually an independent DB cluster (primary + replicas), not just a table partition — so sharding scales writes/storage while replication scales reads/availability.

**Q. What makes a good shard key, and why is `country` bad?**
A. Even distribution, stable, present in common queries. `country` is skewed/low-cardinality → one massive hot shard (India) and near-empty ones (Iceland).

**Q. How do you query by a non-shard-key field?**
A. Scatter-gather across all shards (expensive) or a lookup table mapping the field → shard key, then route normally.

**Q. Shard key vs index?**
A. Shard key finds the machine; index finds the row within that machine — different layers.

**Q. Why consistent hashing over `hash % N`, and what do virtual nodes add?**
A. `hash % N` remaps most keys when N changes; a hash ring moves only ~1/N keys on add/remove. Virtual nodes (many positions per server) smooth out uneven distribution.

**Q. Does consistent hashing solve cab nearby search?**
A. No. Nearby search needs spatial indexing such as geohash/S2/H3; consistent hashing may only distribute those cells across nodes.

**Q. What are the classic sharding pain points?**
A. Resharding, hotspot keys, cross-shard joins/transactions, and queries that lack the shard key.
