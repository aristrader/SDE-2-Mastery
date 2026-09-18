---
order: 20
search: false
---

# Design

## Design drill: recover ownership of one partition

Design a three-member replicated partition group. One leader accepts writes, two followers replicate, and a
router sends each key to the current leader. A leader may fail or become unreachable from part of the group.

```mermaid
flowchart TD
    H[Members stop hearing from leader] --> S[Record suspected failure]
    S --> Q{Majority available?}
    Q -->|yes| E[Elect follower with newer term]
    E --> F[Fence old term and publish routing update]
    F --> W[New leader accepts writes]
    Q -->|no| R[Reject or hold writes safely]
```

The key design decision is not the heartbeat itself. It is the promotion authority: only a quorum may move
ownership. The leader's acknowledgement rule separately decides whether a recently accepted write can be
lost if it had not reached enough replicas.

## Interview prompt

"A partition leader is unreachable. How do you avoid both a long outage and two leaders accepting writes?"

Answer with failure suspicion, quorum-based election, a newer term/epoch, routing refresh, and a clear
acknowledgement/durability boundary.
