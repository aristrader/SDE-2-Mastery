---
order: 100
---

# Deadlock, Livelock, Starvation

These are progress failures. The code may be "thread-safe" in the narrow data-race sense and still fail to make progress.

## Deadlock

Deadlock means threads wait forever for each other's locks.

```text
Thread A: holds lock-1, waits for lock-2
Thread B: holds lock-2, waits for lock-1
```

Classic prevention: acquire multiple locks in one consistent global order.

```java
Account first = source.id() < destination.id() ? source : destination;
Account second = source.id() < destination.id() ? destination : source;

synchronized (first) {
    synchronized (second) {
        transferMoney(source, destination, amount);
    }
}
```

This removes circular wait because every thread asks for the same pair of locks in the same order.

## Livelock

Livelock means threads keep running and reacting, but no useful work completes.

Example: two retrying workers repeatedly back off at the same time, collide again, and back off again. Mitigation is usually jitter, random backoff, or clearer ownership.

## Starvation

Starvation means one task never gets the CPU, lock, or worker it needs because other work keeps winning.

Backend examples:

- a cleanup task never runs because request tasks keep occupying the pool
- nested submissions block all workers while the tasks they wait for are queued
- low-priority work never obtains a heavily contended unfair lock

## Thread-Pool Starvation Deadlock

A two-thread pool can deadlock without explicit Java monitors:

```text
Task A runs on worker 1, submits C to same pool, waits for C
Task B runs on worker 2, submits D to same pool, waits for D
C and D are queued, but no worker is free
```

Avoid blocking inside tasks on other tasks submitted to the same small pool. Compose async work, use separate executors, or size and bound the pool deliberately.

## Diagnosis

Use thread dumps for deadlocks and lock contention. Look for:

- `BLOCKED` threads waiting on monitors
- cycles in "waiting to lock" / "locked" output
- all worker threads `WAITING` on futures from the same executor
- repeated dumps showing no progress

## Quick recall

**Q. What is deadlock?**
A. Threads wait forever in a cycle of lock ownership and lock waiting.

**Q. Best prevention for classic multi-lock deadlock?**
A. Consistent lock ordering.

**Q. Deadlock vs livelock?**
A. Deadlock waits forever; livelock runs forever but keeps failing to progress.

**Q. Starvation vs deadlock?**
A. In starvation, other work continues progressing while one task is perpetually denied a resource.

**Q. Why can nested submissions starve a small pool?**
A. Workers block waiting for queued work, but no worker is available to run that queued work.

**Q. What should you capture before restarting a hung Java process?**
A. Thread dumps, ideally several spaced apart, so the progress failure is diagnosable.
