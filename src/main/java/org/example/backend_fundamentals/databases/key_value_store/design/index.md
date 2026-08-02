---
order: 20
search: false
---

# Design

## Design a distributed key-value store

Assumptions:

- values are small, usually less than 10 KB
- total data is too large for one machine
- low-latency `get` and `put`
- high availability during node failures
- tunable consistency
- automatic node add/remove

## High-level design

```text
Client
  -> Coordinator node
  -> Consistent hash ring
  -> N replica nodes
  -> return after R or W quorum
```

The coordinator does not need to be a special central server. Any node can act as coordinator for a request. That avoids a single point of failure.

## Data placement

1. Hash the key onto the ring.
2. Walk clockwise to find the first physical node.
3. Continue walking to choose `N` unique physical nodes for replication.
4. Use virtual nodes so distribution is smooth and weighted by capacity.

## Write path

```text
put(k, v)
  -> coordinator picks N replicas
  -> each replica appends to commit log
  -> each replica writes memtable
  -> coordinator returns after W ACKs
  -> memtable eventually flushes to SSTable
```

## Read path

```text
get(k)
  -> coordinator asks R replicas
  -> replicas check memtable/cache
  -> Bloom filter narrows SSTables
  -> SSTable read
  -> coordinator reconciles versions
  -> return latest/merged value
```

## Consistency choice

For an AP-style design, use eventual consistency with quorums:

- `N=3, W=1, R=1`: fastest, weakest
- `N=3, W=2, R=2`: stronger common default
- `N=3, W=3, R=1`: fast reads, slow writes
- `N=3, W=1, R=3`: fast writes, slow reads

If the product cannot tolerate stale or conflicting values, a Dynamo-style AP store is the wrong default. Pick a CP database or strongly consistent configuration.

## Failure handling

- **Gossip:** nodes share heartbeat/membership updates.
- **Sloppy quorum:** use healthy substitute nodes when assigned replicas are down.
- **Hinted handoff:** substitutes return missed writes to the owner after recovery.
- **Merkle tree repair:** compare replica ranges and sync only differences.
- **Cross-DC replication:** survive full data-center outages.

## Interview wrap-up

This design scales writes/storage by partitioning keys with consistent hashing, improves availability by replicating each key to `N` nodes, tunes latency vs consistency with `R/W` quorums, handles temporary failures with hinted handoff, and repairs permanent drift with anti-entropy/Merkle trees.

## Quick recall

**Q. What should you deep dive first?**  
A. Partitioning, replication/quorums, failure repair, then read/write path.

**Q. What is the main tradeoff?**  
A. Availability/latency vs consistency/conflict complexity.
