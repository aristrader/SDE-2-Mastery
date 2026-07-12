---
order: 10
search: false
---

# Thread Lifecycle Practice

## Exercise: start-vs-run - start() vs run()

### Goal
Separate ordinary method calls from concurrent execution.

### Task
Create a `Thread` that prints the current thread name. First call `run()` directly, then change the program to call `start()`.

### Checks
- Explain which thread executes the task in each version.
- Explain why a `Thread` instance can be started only once.

## Exercise: join-not-sleep - join, Not Sleep

### Goal
Use real thread coordination.

### Task
Start a worker that writes a shared value. Make the main thread print the value only after the worker finishes.

### Checks
- Use `join()`, not arbitrary `Thread.sleep()` in main.
- Explain which thread blocks on `join()`.
- Explain the visibility guarantee after successful join.

## Exercise: thread-state-observation - Observe Thread States

### Goal
Connect thread lifecycle names to real code behavior.

### Task
Create one thread that sleeps briefly, one that waits for a lock, and one that completes quickly.

Print each thread's state before start, shortly after start, and after `join()`.

### Checks
- Explain `NEW`, `RUNNABLE`, `TIMED_WAITING`, `BLOCKED`, and `TERMINATED`.
- Explain why exact timing can vary between runs.
