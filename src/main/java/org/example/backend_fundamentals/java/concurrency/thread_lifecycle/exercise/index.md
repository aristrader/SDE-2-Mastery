---
order: 10
search: false
---

# Thread Lifecycle Practice

## Exercise: thread-state-observation - Observe Thread States

### Goal
Connect thread lifecycle names to real code behavior.

### Task
Create one thread that sleeps briefly, one that waits for a lock, and one that completes quickly.

Print each thread's state before start, shortly after start, and after `join()`.

### Checks
- Explain `NEW`, `RUNNABLE`, `TIMED_WAITING`, `BLOCKED`, and `TERMINATED`.
- Explain why exact timing can vary between runs.
