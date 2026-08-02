---
order: 10
search: false
---

# Exercise

## Exercise: distributed-key-value-store - Design a Dynamo-Style Key-Value Store

Design a distributed key-value store with:

- `put(key, value)`
- `get(key)`
- values under 10 KB
- high availability
- tunable consistency
- automatic scale-out

Tasks:

1. Clarify CAP goal: CP or AP?
2. Pick `N`, `W`, and `R` for:
   - lowest latency
   - stronger consistency
   - fast reads
   - fast writes
3. Explain data partitioning with consistent hashing and virtual nodes.
4. Explain how replicas are selected.
5. Walk through the write path.
6. Walk through the read path.
7. Explain how conflicts are detected.
8. Explain how temporary and permanent failures are handled.
9. Improve the summary table with one domain-specific row, such as KYC result lookup, cab nearby search, autocomplete, or search indexing.

## Acceptance criteria

A good answer includes:

- consistent hashing and virtual nodes
- replication factor `N`
- quorum values `W` and `R`
- commit log, memtable, SSTable
- Bloom filters on read path
- vector clocks or another conflict strategy
- gossip, hinted handoff, Merkle tree repair
- a clear warning that KV stores are not a universal answer for search/geospatial queries

## Quick recall

**Q. Why is cab nearby search not just key-value lookup?**  
A. It needs spatial proximity. Use geohash/S2/H3/quadtree, then store or route cells as keys if useful.

**Q. Why is text search not just key-value lookup?**  
A. It needs term-to-document lookup, ranking, analyzers, and inverted indexes.
