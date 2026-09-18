---
order: 10
search: false
---

# Exercise

## Exercise: cluster-failover - Explain the safe response to a missing leader

A router sends a key range to Leader A. A follower B has replicated most, but not necessarily all, of A's
recent writes. The remaining cluster members stop receiving A's heartbeats.

1. Why is a missed heartbeat insufficient reason by itself to let B accept writes?
2. What must happen before the router sends new writes to B?
3. What protects the cluster if A returns after B was promoted?
4. What is the caller told if no quorum can choose a successor?

## Answer shape

Treat the heartbeat as suspicion. A quorum elects B in a newer term/epoch, routing moves to B, and A is
fenced or must rejoin as a follower. Without quorum, return a retryable or pending failure instead of
creating two writable owners. State separately whether the configured replication acknowledgement can lose
the most recent write.

## Quick recall

**Q. Why are leader election and replication separate concerns?**

**A.** Replication provides a candidate copy; election decides which copy may safely own future writes.
