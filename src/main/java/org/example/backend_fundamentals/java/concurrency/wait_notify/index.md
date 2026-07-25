---
order: 80
---

# wait, notify, notifyAll

Study this to understand monitors and old Java code. In real backend code, prefer `BlockingQueue`, `CountDownLatch`, `Semaphore`, executors, or `CompletableFuture`.

Interview target: explain why `wait()` releases the monitor, why `sleep()` does not, and why `wait()` must be inside a `while` loop.

## Mental model

A monitor is like one key for one room:

```java
synchronized (lock) {
    // only the thread holding lock's monitor can be here
}
```

If the condition is not ready, the thread must not keep holding the key. `wait()` releases the monitor and sleeps until another thread signals that state may have changed.

```java
synchronized (lock) {
    while (!ready) {
        lock.wait();
    }

    useReadyState();
}
```

Notification is only a hint: "something changed, check again."

## Contract

`wait()`, `notify()`, and `notifyAll()` must be called while holding the same object's monitor.

```java
synchronized (lock) {
    lock.wait();
}
```

Calling them without owning the monitor throws `IllegalMonitorStateException`.

| API | What it does |
| --- | --- |
| `wait()` | Releases monitor, suspends current thread, reacquires monitor before returning. |
| `notify()` | Wakes one arbitrary waiter on that monitor. |
| `notifyAll()` | Wakes all waiters on that monitor; they still reacquire one at a time. |
| `sleep()` | Pauses current thread but keeps any locks already held. |

## Always wait in `while`

Correct:

```java
synchronized (lock) {
    while (!ready) {
        lock.wait();
    }
    useReadyState();
}
```

Wrong:

```java
synchronized (lock) {
    if (!ready) {
        lock.wait();
    }
    useReadyState(); // condition may still be false
}
```

Why `while` is required:

- spurious wakeups are allowed
- `notifyAll()` can wake many threads
- another thread may consume the condition first
- the notification may be for a different condition on the same monitor

## Producer-consumer shape

This is the classic interview example. Know the shape; do not hand-roll this in normal service code.

```java
final class SingleSlotBuffer<T> {
    private T value;
    private boolean available;

    synchronized void put(T next) throws InterruptedException {
        while (available) {
            wait();
        }
        value = next;
        available = true;
        notifyAll();
    }

    synchronized T take() throws InterruptedException {
        while (!available) {
            wait();
        }
        T result = value;
        value = null;
        available = false;
        notifyAll();
        return result;
    }
}
```

Why it works:

- both methods use the same monitor: `this`
- condition fields are checked while holding the monitor
- `wait()` releases the monitor so the other side can change state
- `notifyAll()` wakes waiters after state changes
- `while` re-checks the condition after wake-up

## `notify()` vs `notifyAll()`

Use `notifyAll()` by default when multiple conditions may share one monitor.

Example: producers wait for "not full"; consumers wait for "not empty". `notify()` can wake the wrong type. That thread checks, sees its condition is still false, and waits again while the useful waiter remains asleep.

Use `notify()` only when all waiters are interchangeable and waking one is enough.

## Missed signal trap

Broken:

```java
if (!ready) {
    synchronized (lock) {
        lock.wait();
    }
}
```

The check happens outside the monitor. Another thread can set `ready = true` and notify before this thread actually waits. Then this thread can wait forever.

Correct:

```java
synchronized (lock) {
    while (!ready) {
        lock.wait();
    }
}
```

## Interruption

`wait()` throws `InterruptedException`. Treat it as cancellation.

Prefer:

```java
void take() throws InterruptedException {
    synchronized (lock) {
        while (!available) {
            lock.wait();
        }
    }
}
```

If you cannot throw it:

```java
catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    return;
}
```

## Prefer higher-level APIs

| Need | Prefer |
| --- | --- |
| Producer-consumer queue | `BlockingQueue` |
| Wait until N workers finish | `CountDownLatch` |
| Limit concurrent access | `Semaphore` |
| Run tasks | `ExecutorService` |

## What not to over-study

| Topic | Why |
| --- | --- |
| Complex custom buffers | `BlockingQueue` is normally the right tool. |
| Proving `notify()` correctness | Use `notifyAll()` unless all waiters are interchangeable. |
| Fairness/order of awakened threads | JVM does not guarantee the exact waiter order. |

## Quick recall

**Q. What monitor must you own before `wait()`?**  
A. The same object's monitor: inside `synchronized (lock)`, call `lock.wait()`.

**Q. What does `wait()` do to the monitor?**  
A. Releases it while waiting, then reacquires it before returning.

**Q. Why `while`, not `if`?**  
A. Wake-up is only a hint; the condition may still be false.

**Q. Why is `notifyAll()` usually safer?**  
A. `notify()` may wake the wrong waiter when multiple conditions share one monitor.

**Q. `wait()` vs `sleep()`?**  
A. `wait()` releases the monitor. `sleep()` keeps locks already held.

**Q. What should code do with `InterruptedException`?**  
A. Prefer throwing it; otherwise restore the interrupt flag and stop current work.
