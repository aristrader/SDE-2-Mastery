---
order: 60
---

# Scaling Reads

Use this pattern when read QPS, query latency, or repeated retrieval of the same data is the bottleneck. Work from the query outward instead of starting with Redis.

## Escalation path

| Pressure | First move | Next move | Trap |
|---|---|---|---|
| Slow selective query | Index the actual predicate/sort; fix N+1 and query shape | Denormalized/materialized read model | An index adds write/storage cost and may not serve a different query |
| Read-heavy durable data | Read replicas | Route non-critical reads to replicas | Replica lag can violate read-after-write |
| Repeated/hot data | Cache-aside with TTL and invalidation/versioning | CDN for static/public edge content | Cache invalidation, stampede, and hot keys |
| Search/filter over text | Search index | Precompute or asynchronously index | Search results are normally eventually consistent |

## Cache-aside flow

```text
read -> cache hit -> return
     -> cache miss -> database -> cache with TTL -> return

write -> database commit -> invalidate/version cache key -> future read repopulates
```

Define the tolerated staleness before introducing a cache. Use request coalescing, a short lock, or stale-while-revalidate to prevent a hot key from causing a database stampede on expiry.

## Interview delivery

State the read pattern and consistency policy: "Most timeline reads tolerate a few seconds of staleness, so I use cache-aside with a bounded TTL. Profile/settings reads immediately after an update go to primary or use a version-aware read."

## Quick recall

**Q. Why not put everything in a cache?**
A. Cache is volatile and stale by design; it needs invalidation, capacity planning, and a durable source of truth.

**Q. What is replica lag?**
A. A replica may not yet contain a primary write, so a user can read an old value immediately after updating it.
