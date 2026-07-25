---
order: 90
---

# Locks

Interview priority: know `synchronized` first. Explicit locks matter only when the interviewer asks, "Why not just use `synchronized`?"

## What to study

| Topic | Interview depth | What to remember |
| --- | --- | --- |
| `ReentrantLock` | High | Manual lock/unlock, always unlock in `finally`. |
| `tryLock()` | High | Try without waiting forever; useful for timeout/backoff/deadlock avoidance. |
| `lockInterruptibly()` | Medium | A waiting thread can respond to cancellation/interruption. |
| `Condition` | Medium | Like `wait/notify`, but one lock can have multiple wait queues. |
| Fair lock | Low | Fair reduces starvation risk but costs throughput. Default unfair is usually better. |
| `ReentrantReadWriteLock` | Medium | Many readers together, one writer exclusively; useful only for read-heavy state. |
| `StampedLock` | Low | Optimistic reads; advanced and not reentrant. Recognize it, do not deep-study first. |

## `synchronized` vs `ReentrantLock`

| Need | Use |
| --- | --- |
| Simple mutual exclusion | `synchronized` |
| Auto-release on block exit | `synchronized` |
| Timed/non-blocking lock attempt | `ReentrantLock.tryLock()` |
| Interrupt while waiting for lock | `ReentrantLock.lockInterruptibly()` |
| Multiple wait conditions | `ReentrantLock` + `Condition` |

Default answer: use `synchronized` unless you need one of the extra lock features.

## `ReentrantLock` pattern

```java
private final ReentrantLock lock = new ReentrantLock();

void update() {
    lock.lock();
    try {
        updateSharedState();
    } finally {
        lock.unlock();
    }
}
```

Why `finally`: if the protected code throws and `unlock()` is skipped, the lock remains held and other threads can block forever.

## `tryLock()`

Use `tryLock()` when waiting forever is not acceptable.

```java
boolean acquired = lock.tryLock(200, TimeUnit.MILLISECONDS);
if (!acquired) {
    return false;
}

try {
    updateSharedState();
    return true;
} finally {
    lock.unlock();
}
```

Key point: call `unlock()` only if this thread acquired the lock.

## `Condition`

`Condition` is the explicit-lock version of `wait()` / `notifyAll()`.

Use it when different threads wait for different reasons under the same lock:

| Example wait reason | Condition |
| --- | --- |
| Producer waits because buffer is full | `notFull` |
| Consumer waits because buffer is empty | `notEmpty` |

Rules:

- call `await()` only while holding the lock
- `await()` releases the lock while waiting, then reacquires it before returning
- check the condition in a `while` loop, not `if`
- signal the matching condition after changing shared state

For most interview answers, say: "In real code I would usually prefer `BlockingQueue` over writing this manually."

## `ReentrantReadWriteLock`

Use when reads are common and writes are rare.

| Operation | Lock |
| --- | --- |
| Read shared state | read lock |
| Modify shared state | write lock |

It does not always improve performance. If writes are frequent or critical sections are tiny, normal locking can be simpler and faster.

Important trap: read-to-write upgrade can deadlock. Do not hold the read lock and then wait for the write lock.

## What not to over-study

| Topic | Why |
| --- | --- |
| `StampedLock` internals | Rare in SDE-2 interviews; recognize optimistic read only. |
| Lock downgrade code | Nice-to-know, not first-pass interview material. |
| Custom producer-consumer with `Condition` | Understand the idea; prefer `BlockingQueue` in real code. |
| Fair lock tuning | Usually a performance/detail question, not core concurrency. |

## Quick recall

**Q. When should you prefer `synchronized`?**  
A. Simple critical sections where you only need mutual exclusion and visibility.

**Q. Why use `ReentrantLock`?**  
A. For `tryLock`, timeout, interruptible waiting, fairness option, or multiple `Condition`s.

**Q. Why must `unlock()` be in `finally`?**  
A. Exceptions must not leave the lock permanently held.

**Q. What does `tryLock()` solve?**  
A. It lets code give up or back off instead of waiting forever.

**Q. What is a `Condition`?**  
A. A separate wait queue attached to a `Lock`.

**Q. When does `ReentrantReadWriteLock` help?**  
A. Read-heavy, write-rare shared state.
