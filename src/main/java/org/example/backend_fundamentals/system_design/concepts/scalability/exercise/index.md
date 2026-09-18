---
order: 10
search: false
---

# Exercise

## Exercise: scale-by-pressure - Choose the next component

For each observed symptom, name the smallest design change, the new trade-off, and one metric that proves
whether it worked.

1. API CPU and connection pools saturate while the database remains healthy.
2. A primary database spends most of its time serving repeated, stale-tolerant reads.
3. A slow thumbnail-generation task holds HTTP request threads during traffic spikes.
4. One database can no longer hold the required write rate or storage volume.
5. A single application instance fails and the remaining instances become overloaded.

## Answer shape

Use stateless application replicas, cache-aside or replicas, a durable queue with idempotent workers,
sharding by a stable high-cardinality key, and enough healthy capacity plus failure-aware routing,
respectively. A strong answer also names the boundary each change creates: shared state, staleness,
acceptance versus completion, cross-shard work, or failover headroom.

## Quick recall

**Q. What makes a scaling answer credible?**

**A.** A measured pressure, one targeted change, its trade-off, and the failure behavior it introduces.
