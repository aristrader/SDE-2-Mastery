---
order: 20
search: false
---

# Answers: Replication Guarantees

## Solution: read-after-write - Route a profile update

The primary can acknowledge the photo change at T1 while the replica still has the old photo until T2. A read
routed between T1 and T2 is stale even though the write succeeded. A small, explicit strategy is to pin that
user's next reads to the primary for a short consistency window. The cost is less read offload; a version/LSN
token can make the window more precise but couples the application to a supported replica-caught-up check.

Synchronous acknowledgement reduces the chance that an acknowledged write disappears on failover. Its configured
receipt/flush/apply point does not automatically make every replica current, so a read routed to a non-required or
not-yet-applied replica can still return the old photo.

## Solution: safe-failover - Recover a single-leader database

In a shared single-leader history, the replica with the furthest eligible log position contains the longest
known prefix of writes and is the best promotion candidate. With asynchronous replication, an acknowledged write
may exist only on the failed primary and therefore be absent from every candidate: that is the RPO loss window.

Fence the old primary before admitting writes to the new one—for example, remove its lease/quorum authority and
block it at the proxy/storage layer. If it later returns, rejoin it as a follower and reconcile from the elected
leader; never let it resume as a second writer. A write known only to the old primary is either recovered from
that node through an explicit reconciliation process or reported as lost; it cannot silently be assumed present.

## Solution: quorum-boundary - Explain \`R + W > N\`

With `N = 3`, `W = 1, R = 1` permits a write to A and a later read from C, so no participant overlaps. With
`W = 2, R = 2`, any two-node write set and any two-node read set share at least one node. That shared replica
can return the acknowledged version to the coordinator, which reconciles the replies using the system's version
and conflict rules.

Overlap is not a proof that every client sees one global order during a partition or that two concurrent writes
cannot conflict. A replica that was offline converges through hinted handoff/read repair where applicable and,
reliably across cold data, scheduled anti-entropy repair.

## Quick recall

**Q. What is the failover order?**
A. Fence the old writer, promote an eligible advanced replica, redirect traffic, then rejoin/reseed the old node
as a follower.
