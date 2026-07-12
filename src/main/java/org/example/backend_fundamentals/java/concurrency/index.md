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
| 6 | `atomic_classes` | Atomic counters, CAS, read-and-reset, and LongAdder. |
| 7 | `concurrent_collections` | ConcurrentHashMap, CopyOnWriteArrayList, and map atomic operations. |
| 8 | `wait_notify` | Low-level monitor coordination and why higher-level APIs usually win. |
| 9 | `locks` | Explicit lock APIs and try-lock patterns. |
| 10 | `deadlock_livelock_starvation` | Progress failures, lock ordering, and pool starvation. |
| 11 | `executor_service` | Thread pools instead of manual thread creation. |
| 12 | `completable_future` | Async composition, split into basics, composition, failure/timeouts, and backend workflows. |
| 13 | `interview_drill_bank` | Compact oral drills after the hands-on pages. |

## Quick recall

- **Need safe shared mutation?** Use a concurrency primitive, not hope.
- **Need visibility only?** `volatile` may fit.
- **Need compound read-modify-write safety?** Use synchronization, locks, atomics, or concurrent collections.
- **Need many tasks?** Use an executor, not one manual thread per task.
- **Need a shared map?** Use `ConcurrentHashMap` operations atomically; do not split check and update.
- **Need async workflow?** Classify dependency vs independence before choosing a CompletableFuture method.
