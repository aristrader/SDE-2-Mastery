---
order: 70
---

# Concurrency

Concurrency is the Java runtime model for doing more than one thing at a time. Read it after OOP, collections, and JVM basics: the hard parts are shared mutable state, visibility, ordering, and choosing the right abstraction.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `thread_lifecycle` | Thread states and basic lifecycle vocabulary. |
| 2 | `race_conditions` | Atomicity, visibility, and ordering failures. |
| 3 | `jmm` | Why writes are not automatically visible across threads. |
| 4 | `volatile_keyword` | Visibility without mutual exclusion. |
| 5 | `synchronized_keyword` | Mutual exclusion and monitor semantics. |
| 6 | `locks` | Explicit lock APIs and try-lock patterns. |
| 7 | `executor_service` | Thread pools instead of manual thread creation. |
| 8 | `completable_future` | Async composition. |

## Quick recall

- **Need safe shared mutation?** Use a concurrency primitive, not hope.
- **Need visibility only?** `volatile` may fit.
- **Need compound read-modify-write safety?** Use synchronization, locks, atomics, or concurrent collections.
- **Need many tasks?** Use an executor, not one manual thread per task.
