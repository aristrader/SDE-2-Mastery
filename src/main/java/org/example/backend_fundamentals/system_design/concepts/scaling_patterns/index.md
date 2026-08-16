---
order: 100
---

# HLD Scaling Patterns: A Mental Map

Use this page after drawing a simple functional design. Do not add every pattern. Name the pressure, apply the smallest fitting remedy, then state the cost it introduces.

For focused interview playbooks, see [scaling reads](/system_design/patterns/scaling_reads/), [scaling writes](/system_design/patterns/scaling_writes/), [long-running tasks](/system_design/patterns/long_running_tasks/), [contention](/system_design/patterns/contention/), and [proximity services](/system_design/patterns/proximity_services/).

## The decision path

```text
What is saturated or slow?
  -> stateless request service: load balance and scale horizontally
  -> repeated reads: cache, replica, precompute, or a search index
  -> durable writes: partition, batch only non-live work, or use a write-optimized store
  -> bursty or slow work: queue, worker pool, deadline, retry policy
  -> conflicting commands: conditional state transition, unique constraint, or short lease
  -> locality query: partition/index by locality, then refine candidates
  -> one region is overloaded or far away: regional ownership and replication
```

## Service scaling

| Signal | Primary move | Trade-off / interview trap |
|---|---|---|
| CPU, connections, or request latency rises on one app tier | Stateless service behind a load balancer; autoscale from utilization and latency | Move session and workflow state out of process first. More instances alone do not fix a shared database bottleneck. |
| One operation is much heavier than the rest | Split it into a separately scalable worker/service | Do this for an actual scaling or ownership boundary, not merely to create microservices. |
| External dependency is slow | Timeout, bounded retry with jitter, circuit breaker, bulkhead, and a product fallback | A timeout is an unknown outcome for a write; do not blindly retry a create/charge request. |

## Database and read-load scaling

| Signal | Primary move | Trade-off / interview trap |
|---|---|---|
| Same data is read repeatedly | Cache-aside with a TTL; invalidate or version after writes | Cache is not the source of truth. Explain tolerated staleness and stampede protection. |
| Read-heavy durable data | Read replicas | Replica lag can break read-after-write. Route critical reads to primary or use a consistency policy. |
| Query scans too much data | Correct index, materialized view, precomputed result, or search index | Do not say "add an index" without naming the query and its access pattern. |
| One database cannot take write/storage volume | Partition/shard by a stable, high-cardinality key | A bad key creates hot partitions; cross-shard joins and resharding cost complexity. |
| Need latest ephemeral state at very high QPS | In-memory/key-value store | Plan expiry, recovery, and what durable record remains authoritative. |

## Queue and asynchronous-work scaling

Use a queue when work is slow, bursty, retryable, or does not need an immediate final answer.

| Situation | Pattern | Guardrail |
|---|---|---|
| Traffic burst exceeds worker capacity | Durable queue plus workers that scale on lag | Carry a deadline. A queue absorbs a burst; it does not make latency disappear. |
| Request should return quickly while work continues | `202 Accepted`, status resource, webhook/polling | Persist the job state before enqueueing or use an outbox. |
| Database commit and event publish must agree | Transactional outbox plus relay/CDC | Consumers still need idempotency because delivery is normally at least once. |
| Analytics, history, email, media processing | Async event stream or queue | Keep live user decisions on the synchronous path only when freshness/consistency requires it. |

Do **not** queue freshness-sensitive state merely to lower write QPS. For example, a ride-matching system keeps the latest driver location in a fast store and streams history asynchronously; batching live GPS positions into a relational database produces stale matches.

## Concurrency and correctness

| Conflict | Default mechanism | When a lease/lock helps |
|---|---|---|
| One row/item may be changed once | Conditional update, optimistic version, or unique constraint | Usually no distributed lock needed. The durable transition is the authority. |
| Short-lived exclusive work across instances | Lease with TTL, such as Redis `SET NX PX` | Use for an offer/ownership window; validate state again when committing. |
| Client or queue retries | Idempotency key plus persisted result/status | Make the operation replay-safe before adding retries. |
| Multi-step workflow crosses services | Explicit state machine, outbox, and compensation/reconciliation | A cron job is a repair mechanism, not the primary correctness mechanism. |

Mental model:

```text
fast temporary coordination -> lease
final business decision -> durable conditional state transition
recovery after crash/timeout -> idempotency + reconciliation
```

## Geographic and locality scaling

```text
location -> cell/index partition -> nearby candidates -> exact filter -> expensive ranking
```

- Use a Geohash, H3, S2, or another spatial index to avoid scanning every point.
- Always inspect neighbouring cells; every cell system has boundaries.
- Use cheap filtering first, then exact distance, then expensive routing/ML only for a small candidate set.
- Treat a geospatial cell as a **data-indexing** choice and a city/region as a **workload-ownership** choice. They solve different problems.
- Split or salt hot partitions only when measurements show one key/cell dominates; this makes aggregation and lookup more complex.

## Regions, availability, and failure domains

| Need | Pattern | Cost |
|---|---|---|
| Low latency and local coordination | Route to a nearby region/cell and keep the hot path local | Cross-region users and failover need explicit ownership rules. |
| Survive an instance failure | Redundant stateless instances, health checks, and load balancing | Scaling capacity is not automatically data redundancy. |
| Survive data-node loss | Replication, failover, backups, and a tested recovery plan | Replication can still lose recent writes or serve stale reads depending on the model. |
| Graceful outage behavior | Degrade non-critical features, return pending/known state, or use cached data | Never serve a dangerous stale answer merely to appear available. |

## Deep-dive selection in an interview

Pick two, not ten:

1. Identify the workload that dominates by QPS, data volume, latency, or correctness risk.
2. Quantify only the estimate that changes the choice.
3. Explain the exact read/write path and the failure behavior.
4. State one trade-off and when you would upgrade the design.

Examples:

- Uber: live geospatial writes and double assignment.
- News feed: celebrity fan-out and read latency.
- YouTube: upload/transcoding backlog and global playback.
- Web crawler: per-host politeness and frontier scheduling.
- Notification service: provider throughput, retries, and deduplication.

## Quick recall

**Q. When should I introduce a queue?**  
A. When work is slow, bursty, retryable, or can finish asynchronously; define the deadline and idempotency behavior too.

**Q. What fixes a double-booking or double-assignment race?**  
A. A durable conditional transition or constraint decides the winner. A short lease can coordinate the attempt.

**Q. How do I reduce database load?**  
A. Start from the access pattern: cache/reuse repeated reads, replicas for read scale, indexes for scans, partitioning for write/storage scale, and async processing for non-live work.

**Q. Is a region the same as a shard?**  
A. No. A region is a latency/failure domain; a shard is a data partition. They may align but solve different problems.

**Q. What is the senior scaling answer?**  
A. Name the bottleneck, make the smallest targeted change, describe its trade-off and failure behavior.
