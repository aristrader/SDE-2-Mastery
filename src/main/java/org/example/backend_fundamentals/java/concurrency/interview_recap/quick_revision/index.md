---
title: Quick Revision
order: 10
search: false
---

# Concurrency Quick Revision

| Topic | One-line recall |
| --- | --- |
| Race condition | Result depends on unsafe thread interleaving. |
| `synchronized` | Mutual exclusion plus unlock-to-lock happens-before. |
| `volatile` | Visibility and ordering for one variable, not atomic compound updates. |
| JMM | Defines when writes become visible across threads. |
| `AtomicInteger` | Lock-free atomic read-modify-write for one value. |
| Lock | Explicit mutual exclusion with options like fairness and conditions. |
| ExecutorService | Task execution plus lifecycle; queue and rejection policy matter. |
| ConcurrentHashMap | Concurrent map for shared access; not a replacement for all invariants. |
| Deadlock | Threads wait forever on locks held by each other. |

## Quick recall

**Q. Counter fix?**
A. `AtomicInteger` for one supported atomic value operation, or one lock for a larger invariant.

**Q. Stop-flag fix?**
A. `volatile boolean`, provided the protocol is only a visible read/write signal.

**Q. Multi-field invariant?**
A. Protect all related reads and writes with the same lock or redesign ownership so the state changes together.
