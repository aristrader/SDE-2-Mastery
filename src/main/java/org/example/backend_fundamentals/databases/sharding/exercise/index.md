---
order: 10
search: false
---

# Practice: Sharding and Resharding

Start every answer with the dominant request shape. A placement strategy is useful only when its routing,
operational cost, and recovery boundary are explicit.

## Exercise: choose-a-key - Route an order service

Most requests are “list one customer’s orders” and “create an order for one customer.” Support agents sometimes
search by order ID; finance runs a daily global revenue report.

1. Choose a shard key and justify it against distribution, stability, and the common requests.
2. Route an order-ID lookup without turning every request into scatter-gather.
3. Explain which request remains deliberately multi-shard and how its result should be assembled.

## Exercise: hot-tenant - Repair a skewed shard

One enterprise tenant creates 70% of all write traffic. The tenant key is otherwise the natural shard key.

1. Explain why more ordinary shards do not automatically repair this hotspot.
2. Give one containment strategy and its read/query cost.
3. State what data should remain co-located despite the split.

## Exercise: live-reshard - Move a key range safely

A range shard must be split while writes continue.

1. Give the migration stages from destination provisioning through source retirement.
2. Name the race created by a bulk copy alone.
3. State the rollback point and the evidence required before routing cutover.

## Quick recall

**Q. What does a shard key answer that an index does not?**
A. Which machine or cluster owns the data; an index then finds the row inside that selected shard.
