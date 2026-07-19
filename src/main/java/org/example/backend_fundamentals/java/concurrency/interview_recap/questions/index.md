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
