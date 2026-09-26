---
order: 20
search: false
---

# Answers: Sharding and Resharding

## Solution: choose-a-key - Route an order service

Use customer ID when customer-scoped reads and writes dominate and its distribution is measured as acceptable. It
is stable, lets the router target one shard for the normal paths, and co-locates that customer’s orders. It is not
an automatic guarantee of balance: a few high-volume customers still need hotspot monitoring.

Maintain an order-ID-to-customer-ID (or directly order-ID-to-shard) lookup owned and scaled as a separate access
path. The lookup resolves the routing key before the normal targeted query. A daily global revenue report is a
deliberate fan-out aggregation: run shard-local aggregates in parallel, merge their results, and design the report
for partial failure/retry rather than pretending it is a single-shard request.

## Solution: hot-tenant - Repair a skewed shard

Adding ordinary shards leaves the popular tenant on its original shard, so its write load remains concentrated.
One option is to introduce a deterministic sub-partition suffix for that tenant, such as tenant-42 plus a hash of
an order ID. Writes spread across the suffixes; reads for all tenant data fan out to those known partitions and
merge. That read cost is the explicit trade-off.

Keep data required by the dominant single-order transaction together: the order, its order items, and any local
write-side state. Do not scatter closely coupled data merely to make a chart look balanced; use a separate read
model for cross-partition views where necessary.

## Solution: live-reshard - Move a key range safely

Provision the destination, bulk-copy the range, and feed it ordered changes while the source remains authoritative.
For cutover, fence or stop source writes for that range, record a checkpoint, catch the destination up through that
checkpoint, and atomically switch router ownership. Keep the source through a rollback window, with reverse
replication or an explicit reconciliation path if writes must be accepted after cutover.

A bulk copy alone misses writes that committed after the copy read a row. Zero observed lag before a live router
switch is not enough because a late source write can still arrive. If validation fails before authority changes,
route to the still-authoritative source and catch the destination up. Cut over only when the destination has the
verified range through the fenced checkpoint and the router can send every affected key consistently to it.

## Quick recall

**Q. Why does consistent hashing not eliminate a migration process?**
A. It limits which keys change ownership; those keys still need durable copy, catch-up, verification, and safe
routing cutover.
