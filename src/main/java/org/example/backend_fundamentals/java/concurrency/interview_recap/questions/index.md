---
title: Interview Questions
order: 20
search: false
---

# Concurrency Interview Questions

## Question 1: Why is `volatile int count; count++` broken?

Answer shape: `volatile` gives visibility and ordering, but `count++` is read, increment, write. Use `AtomicInteger` or `synchronized`.

Related full practice: [volatile exercise](../../volatile_keyword/exercise/).

## Question 2: When is `volatile` enough?

Answer shape: one shared variable, plain read/write, no compound invariant. A stop flag or immutable config reference replacement is fine.

## Question 3: What is wrong with `Executors.newFixedThreadPool(200)` as a default backend answer?

Answer shape: it hides an unbounded queue. Under load, tasks can pile up. A better answer names pool size, bounded queue, rejection policy, timeouts, shutdown, and exception handling.

## Question 4: Why is `ConcurrentHashMap.get()` followed by `put()` not enough for one-time initialization?

Answer shape: each call is individually safe, but the gap is still a check-then-act race. Use an atomic mapping operation such as `computeIfAbsent`, and keep the mapping function short and free of blocking/external side effects.

## Quick recall

**Q. What edge makes a `volatile` handoff visible?**
A. A write to a volatile field happens-before a later read of that same field.

**Q. What is the cancellation rule after catching `InterruptedException`?**
A. Propagate it when possible; otherwise restore the interrupt status and stop/clean up according to the task’s cancellation policy.

**Q. When should backend code avoid the common ForkJoinPool?**
A. For blocking database, HTTP, or long-running work; use an executor sized and isolated for that workload.
