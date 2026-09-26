---
order: 10
search: false
---

# Practice: Replication Guarantees

For each answer, name the reader-visible consequence, the trade-off, and the recovery action—not only the
replication mode.

## Exercise: read-after-write - Route a profile update

A user changes a profile photo. The write commits on the primary, but the next request is routed to a replica
that might lag.

1. Explain the exact stale-read timeline.
2. Choose one bounded strategy for the next read: primary pinning, a client-visible version/LSN token, or an
   explicitly stale-tolerant UI. State the cost.
3. Explain why a configured synchronous acknowledgement policy does not by itself make arbitrary replica reads fresh.

## Exercise: safe-failover - Recover a single-leader database

The primary has failed while two replicas have different log positions.

1. State how log position helps select a candidate and what loss it can still leave under asynchronous
   replication.
2. Add the missing safety step that prevents the old primary from accepting writes after a network partition.
3. Describe what happens to a write acknowledged only by the old primary.

## Exercise: quorum-boundary - Explain `R + W > N`

For `N = 3`, compare `W = 1, R = 1` with `W = 2, R = 2`.

1. Show the set overlap in the second case.
2. State what the overlap gives a coordinator and what it does **not** prove under concurrent conflicting writes.
3. Name the repair path for a replica that missed a write while offline.

## Quick recall

**Q. Does synchronous replication automatically give linearizable reads from every replica?**
A. No. The acknowledgement policy and the read-routing/visibility policy are separate decisions.
