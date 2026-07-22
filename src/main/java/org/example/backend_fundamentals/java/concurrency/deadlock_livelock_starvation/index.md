---
order: 100
---

# Deadlock, Livelock, Starvation

These are progress failures. Code can protect shared data correctly and still fail because threads stop making useful progress.

| Failure | What it looks like |
|---|---|
| Deadlock | Threads wait forever in a cycle. |
| Livelock | Threads keep running and reacting, but no useful work completes. |
| Starvation | Some work never gets the CPU, lock, or worker it needs while other work continues. |

## Deadlock

Deadlock means a set of threads are permanently waiting for each other.

Classic two-lock deadlock:

```text
Thread A: holds account-1 lock, waits for account-2 lock
Thread B: holds account-2 lock, waits for account-1 lock
```

Neither thread can proceed because each needs a resource held by the other.

## The four deadlock conditions

Deadlock requires all four conditions:

| Condition | Meaning |
|---|---|
| Mutual exclusion | At least one resource can be held by only one thread at a time. |
| Hold and wait | A thread holds one resource while waiting for another. |
| No preemption | The JVM cannot forcibly take the lock away safely. The owner must release it. |
| Circular wait | A cycle exists: A waits for B, B waits for C, C waits for A. |

Breaking any one condition prevents deadlock. In Java application code, the practical fix is usually breaking circular wait with consistent lock ordering.

## Lock ordering

Broken transfer shape:

```java
void transfer(Account from, Account to, Money amount) {
    synchronized (from) {
        synchronized (to) {
            move(from, to, amount);
        }
    }
}
```

If one thread transfers A -> B while another transfers B -> A, they can acquire the locks in opposite order and deadlock.

Fix: choose a global order and always acquire locks in that order.

```java
void transfer(Account from, Account to, Money amount) {
    Account first = from.id() < to.id() ? from : to;
    Account second = from.id() < to.id() ? to : from;

    synchronized (first) {
        synchronized (second) {
            move(from, to, amount);
        }
    }
}
```

Now every thread asks for the same pair of locks in the same order. A cycle cannot form for those two locks.

If IDs can be equal or missing, add a deterministic tie-breaker lock. Do not let equal ordering fall back to random object scheduling.

## Deadlock with explicit locks

`ReentrantLock.tryLock()` can avoid waiting forever, but only if the code backs out correctly.

```java
boolean transferWithTimeout(Account from, Account to, Money amount) throws InterruptedException {
    if (!from.lock().tryLock(100, TimeUnit.MILLISECONDS)) {
        return false;
    }

    try {
        if (!to.lock().tryLock(100, TimeUnit.MILLISECONDS)) {
            return false;
        }

        try {
            move(from, to, amount);
            return true;
        } finally {
            to.lock().unlock();
        }
    } finally {
        from.lock().unlock();
    }
}
```

The important part is not just `tryLock()`. It is releasing anything already acquired when the second acquisition fails.

## Livelock

Livelock means threads are active, but their reactions prevent progress.

Example:

```text
Two workers detect conflict.
Both politely back off.
Both retry at the same time.
Both conflict again.
Repeat forever.
```

No thread is blocked forever. CPU may be busy. Logs may show repeated retries. The system still completes no useful work.

Common fixes:

- randomized backoff or jitter
- clear ownership instead of symmetric politeness
- maximum retry count
- central queue or coordinator

Interview distinction: deadlock is "nobody can move"; livelock is "everybody moves but the work does not".

## Starvation

Starvation means one thread or task waits indefinitely because other work keeps winning the resource.

Backend examples:

- Request tasks fill a pool so a cleanup task never runs.
- A low-priority task is repeatedly skipped.
- An unfair lock is constantly reacquired by busy threads.
- Readers continuously acquire a read lock and a writer waits for a long time.

Starvation differs from deadlock because the system as a whole may still be doing work. One class of work is just never served.

## Thread-pool starvation deadlock

Deadlock can happen without `synchronized`.

```text
Pool size = 2

Task A runs on worker 1, submits C to same pool, waits for C.get()
Task B runs on worker 2, submits D to same pool, waits for D.get()

C and D are queued.
No worker is free to run them.
A and B wait forever.
```

This is common in backend code when tasks block on other tasks submitted to the same bounded executor.

Bad shape:

```java
ExecutorService pool = Executors.newFixedThreadPool(2);

pool.submit(() -> {
    Future<String> child = pool.submit(this::loadFromDatabase);
    return child.get();
});
```

Fix options:

- Do not block inside pool tasks on work submitted to the same pool.
- Compose async work instead of blocking.
- Use separate executors for parent orchestration and blocking child work.
- Size and bound pools deliberately, with backpressure.

## Diagnosis

Capture thread dumps before restarting a hung process.

Useful commands:

```bash
jcmd <pid> Thread.print
jstack <pid>
```

Look for:

- `BLOCKED` threads waiting to lock monitors
- "Found one Java-level deadlock" in thread dump output
- cycles: thread A locked object X and waits for Y, thread B locked Y and waits for X
- all executor workers waiting on `FutureTask.get()` or `CompletableFuture.join()`
- repeated dumps showing the same threads in the same waiting positions

Take more than one dump, spaced a few seconds apart. One dump shows a snapshot; repeated dumps show whether progress is happening.

## Prevention checklist

- Keep lock ordering consistent.
- Keep critical sections small.
- Avoid calling external services while holding a lock.
- Avoid nested blocking on the same executor.
- Use timeouts for remote calls and optional lock acquisition.
- Prefer higher-level concurrency primitives when they express the workflow directly.

## Quick recall

**Q. What is deadlock?**
A. Threads wait forever in a cycle of lock ownership and lock waiting.

**Q. What are the four deadlock conditions?**
A. Mutual exclusion, hold-and-wait, no preemption, and circular wait.

**Q. Best prevention for classic multi-lock deadlock?**
A. Consistent global lock ordering.

**Q. Deadlock vs livelock?**
A. Deadlock waits forever. Livelock keeps running but repeatedly fails to make useful progress.

**Q. Starvation vs deadlock?**
A. In starvation, other work continues while one task or class of tasks is perpetually denied a resource.

**Q. Why can nested submissions starve a small pool?**
A. Workers block waiting for queued child tasks, but no worker is available to run those child tasks.

**Q. What should you capture before restarting a hung Java process?**
A. Several thread dumps, spaced apart, so lock cycles or stuck executor workers are diagnosable.

