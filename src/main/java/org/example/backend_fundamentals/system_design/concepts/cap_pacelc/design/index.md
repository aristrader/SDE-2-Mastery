---
order: 20
search: false
---

# Design

## Partition-time decision

```mermaid
flowchart TD
    P[Network partition: replica cannot reach quorum] --> I{Would stale or conflicting data break an invariant?}
    I -- Yes --> CP[Reject or return pending\nuntil quorum can decide]
    I -- No --> AP[Serve local value\nrecord version for reconciliation]
    CP --> R[Retry with idempotency key after recovery]
    AP --> M[Converge using explicit merge/conflict rule]
```

This is a decision per operation, not a permanent label for an entire application. The reservation write
may be CP while a catalogue read is AP.

## Healthy-path decision

Even without a partition, synchronous replication can improve read-after-write guarantees at added latency.
Asynchronous replication lowers the latency but exposes a replication-lag window. That is the PACELC
"Else" trade-off.
