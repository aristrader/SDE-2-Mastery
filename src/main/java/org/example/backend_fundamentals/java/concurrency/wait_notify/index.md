---
order: 80
---

# wait, notify, notifyAll

`wait()`, `notify()`, and `notifyAll()` are monitor coordination APIs. They are older and lower-level than `BlockingQueue`, `CountDownLatch`, `Semaphore`, executors, or `CompletableFuture`, but you still need to understand them because they explain old code, thread dumps, and the design of higher-level concurrency tools.

Use them only when the coordination is naturally tied to one monitor-protected condition. In backend application code, a higher-level utility is usually clearer and safer.

## Mental model

A monitor has two jobs:

- Mutual exclusion: only one thread can own the monitor and run inside `synchronized (lock)` at a time.
- Coordination: threads can wait for a condition associated with that same monitor.

The condition is the real business state, such as "buffer is not empty" or "queue is not full". A notification is only a signal that the condition may have changed.

```text
Thread A owns monitor -> condition false -> wait()
wait() releases monitor and suspends Thread A
Thread B enters monitor -> changes condition -> notifyAll()
Thread A wakes -> competes to reacquire monitor -> rechecks condition
```

## The contract

`wait()`, `notify()`, and `notifyAll()` must be called while holding the same object's monitor.

```java
synchronized (lock) {
    lock.wait();
}
```

Calling them without owning the monitor throws `IllegalMonitorStateException`.

`wait()` does three things as one coordinated operation:

1. Releases the monitor.
2. Suspends the current thread.
3. When woken, reacquires the same monitor before returning.

That release is the key difference from `Thread.sleep()`. `sleep()` pauses while keeping any lock the thread already holds. `wait()` temporarily gives up the monitor so another thread can enter and change the condition.

## Always wait in a `while`

Correct pattern:

```java
synchronized (lock) {
    while (!ready) {
        lock.wait();
    }

    useReadyState();
}
```

The `while` loop is not defensive decoration. It is required because waking up does not prove the condition is true.

Reasons:

- Spurious wakeup: the JVM permits `wait()` to return without a matching notification.
- Lost race after wake-up: another woken thread can reacquire the monitor first and consume the condition.
- `notifyAll()`: many threads may wake, but only some of them may have a true condition.
- Mixed conditions: the notification may have been meant for another kind of waiter using the same monitor.

Wrong pattern:

```java
synchronized (lock) {
    if (!ready) {
        lock.wait();
    }

    // Bug: condition may still be false here.
    useReadyState();
}
```

Notification means "wake up and check again", not "your condition is definitely true".

## Producer-consumer example

This single-slot buffer has two conditions:

- `put()` waits while the slot is full.
- `take()` waits while the slot is empty.

```java
final class SingleSlotBuffer<T> {
    private T value;
    private boolean available;

    synchronized void put(T newValue) throws InterruptedException {
        while (available) {
            wait();
        }

        value = newValue;
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

- Both methods synchronize on the same monitor: `this`.
- The condition fields are read and written only while holding that monitor.
- `wait()` releases the monitor, so the other method can enter and change the condition.
- `notifyAll()` wakes waiters after the condition changes.
- The `while` loops re-check the condition after wake-up.

## `notify()` vs `notifyAll()`

`notify()` wakes one arbitrary thread waiting on that monitor. You do not choose which thread.

`notifyAll()` wakes every thread waiting on that monitor. They still must reacquire the monitor one at a time, so `notifyAll()` does not mean every thread proceeds.

Use `notifyAll()` by default when multiple conditions share one monitor. In the buffer example, some waiters may be producers waiting for "not full" and others may be consumers waiting for "not empty". A single `notify()` can wake the wrong type of waiter, which checks its condition, goes back to waiting, and leaves the right waiter asleep.

Use `notify()` only when you can prove all waiters are interchangeable and one waiter is enough.

## Missed signal trap

This is broken:

```java
if (!ready) {
    synchronized (lock) {
        lock.wait();
    }
}
```

The condition check happens outside the monitor. Another thread can set `ready = true` and call `notifyAll()` after the `if` check but before this thread calls `wait()`. The notification is missed, and this thread may wait forever.

The condition check and `wait()` must be inside the same synchronized block:

```java
synchronized (lock) {
    while (!ready) {
        lock.wait();
    }
}
```

## Interruption

`wait()` throws `InterruptedException` if the waiting thread is interrupted. That is a cancellation signal, not a random error to hide.

Best reusable-library style: declare `throws InterruptedException` and let the caller decide whether to retry, return, or shut down.

```java
T take() throws InterruptedException {
    synchronized (lock) {
        while (!available) {
            lock.wait();
        }
        return value;
    }
}
```

If you cannot throw it, restore the flag and stop the current operation:

```java
catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    return;
}
```

Swallowing the exception loses the shutdown request and can make applications hang during graceful shutdown.

## Prefer higher-level APIs

Raw wait/notify makes you own every detail: condition loops, notification timing, monitor choice, missed-signal prevention, interruption, shutdown, and fairness expectations.

For producer-consumer, prefer a `BlockingQueue`:

```java
BlockingQueue<Task> queue = new ArrayBlockingQueue<>(100);

queue.put(task);   // waits while full
Task next = queue.take(); // waits while empty
```

For "wait until N workers finish", prefer `CountDownLatch`. For "limit concurrent access", prefer `Semaphore`. For "run tasks", prefer `ExecutorService`.

## Quick recall

**Q. What monitor must you own before calling `wait()`?**
A. The same object's monitor: inside `synchronized (lock)`, call `lock.wait()`.

**Q. What does `wait()` do to the monitor?**
A. It releases the monitor while waiting, then reacquires it before returning.

**Q. Why use `while`, not `if`, around `wait()`?**
A. Wake-up is only a hint. Spurious wakeups, races after wake-up, and `notifyAll()` all require re-checking the condition.

**Q. Why is `notifyAll()` usually safer than `notify()`?**
A. `notify()` wakes one arbitrary waiter and may wake the wrong condition type. `notifyAll()` lets every waiter re-check its own condition.

**Q. How do missed signals happen?**
A. The condition is checked outside the same synchronized block as `wait()`, so a notification can happen before the thread actually starts waiting.

**Q. Does `wait()` behave like `sleep()`?**
A. No. `wait()` releases the monitor; `sleep()` keeps any locks already held.

**Q. What should code do with `InterruptedException` from `wait()`?**
A. Prefer throwing it. If you cannot, restore the interrupt flag and stop the current operation.
