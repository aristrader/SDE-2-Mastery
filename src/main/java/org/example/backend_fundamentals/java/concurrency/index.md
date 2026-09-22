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

**Q. What is the first concurrency question?**
A. Identify the shared state and its invariant; then ask whether you need visibility, atomicity, mutual exclusion, ordering, or several of them.

**Q. When might `volatile` fit?**
A. A visibility-only protocol such as a stop flag or safely published immutable snapshot—not compound mutation.

**Q. How do you protect read-modify-write state?**
A. Put the whole invariant behind one monitor/lock, an atomic operation, or a concurrent collection operation that owns it.

**Q. What is the default task-execution boundary?**
A. An executor with explicit capacity, queue, rejection, timeout, and shutdown behavior—not one new thread per task.

**Q. What decides a CompletableFuture method?**
A. The dependency graph: transform one result, compose a dependent async result, or combine independent results.
