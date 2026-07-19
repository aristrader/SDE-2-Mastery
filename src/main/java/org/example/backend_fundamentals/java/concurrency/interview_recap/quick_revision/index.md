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

- **Counter fix?** `AtomicInteger` or one lock.
- **Stop flag fix?** `volatile boolean`.
- **Multi-field invariant?** Use a lock.
