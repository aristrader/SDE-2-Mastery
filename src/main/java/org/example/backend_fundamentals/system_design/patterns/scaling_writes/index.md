---
order: 70
---

# Scaling Writes

Use this pattern when a durable write path reaches throughput, storage, or lock-contention limits. Separate a temporary burst from sustained load: a queue can absorb the first, but sustained demand requires capacity or a data-layout change.

## Decision path

| Pressure | Primary move | Trade-off |
|---|---|---|
| One table mixes unrelated workloads | Vertical partition by feature/data type | Cross-feature queries become more expensive |
| One node cannot hold/write the data | Shard by stable, high-cardinality key | Cross-shard transactions, resharding, hot keys |
| Short write burst | Durable queue, worker pool, deadline | Queue lag increases user-visible completion time |
| Per-operation overhead dominates and freshness permits | Batch writes | Larger failure/retry unit and delayed visibility |
| Overload threatens the system | Rate limit or shed/defer low-priority writes | Explicit product degradation |

## Partition-key test

A good key distributes load, keeps the common query local, and is stable. `userId` often works for user-owned data; region/cell can work for local matching. A low-cardinality or skewed key, such as a popular product category, creates hot partitions.

```text
incoming write -> choose partition by key -> durable store
                         |
                         -> temporary burst: queue and consumers for that partition
```

Partitioning data, assigning worker ownership, and deploying to regions are distinct decisions. Explain which problem each addresses rather than treating all three as "sharding."

## Quick recall

**Q. When does a queue solve write scaling?**
A. It smooths a temporary burst or moves asynchronous work off the request path; it cannot fix a permanently underprovisioned destination.

**Q. What makes a partition key bad?**
A. It is skewed, low-cardinality, unstable, or forces the main query to scatter-gather across partitions.
