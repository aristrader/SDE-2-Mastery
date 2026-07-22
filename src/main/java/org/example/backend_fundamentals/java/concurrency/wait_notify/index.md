---
order: 80
---

# wait, notify, notifyAll

`wait()`, `notify()`, and `notifyAll()` are monitor coordination APIs. They are older and lower-level than `BlockingQueue`, `CountDownLatch`, `Semaphore`, executors, or `CompletableFuture`, but you still need to understand them because they explain old code, thread dumps, and the design of higher-level concurrency tools.

Use them only when the coordination is naturally tied to one monitor-protected condition. In backend application code, a higher-level utility is usually clearer and safer.

Interviewers still ask this topic because it proves you understand what a monitor is, why `wait()` releases a lock, why `sleep()` does not, and why condition checks must be written carefully. The expected answer is usually conceptual; production code should normally use higher-level concurrency utilities.

## Mental model

Think of a monitor as a room with one key. Only the thread with the key can be inside the room.

```java
synchronized (lock) {
    // only one thread can be here for this lock
}
```

If a thread enters the room and the thing it needs is not ready, it must not keep standing inside the room forever. If it keeps the key, no other thread can enter and make progress.

That is what `wait()` solves:

```java
synchronized (lock) {
    while (!foodReady) {
        lock.wait();
    }

    eatFood();
}
```

Layman version:

```text
foodReady is false
I cannot continue
I will leave the room
I will return the key
I will sleep until someone says something changed
```

That last part matters: `wait()` does not just sleep. It sleeps **and releases the monitor**.

Another thread can then enter the same room and change the condition:

```java
synchronized (lock) {
    foodReady = true;
    lock.notifyAll();
}
```

Layman version:

```text
I changed the shared state
Everyone waiting for this lock should wake up
They should come back and check their condition again
```

A monitor has two jobs:

- Mutual exclusion: only one thread can own the monitor and run inside `synchronized (lock)` at a time.
- Coordination: threads can wait for a condition associated with that same monitor.

The condition is the real business state, such as "buffer is not empty" or "queue is not full". A notification is only a signal that the condition may have changed.

Full flow:

```text
Thread A enters synchronized(lock)
Thread A checks foodReady
foodReady is false
Thread A calls wait()
Thread A releases lock and enters WAITING

Thread B enters synchronized(lock)
Thread B sets foodReady = true
Thread B calls notifyAll()
Thread B exits synchronized(lock)

Thread A wakes up
Thread A tries to reacquire lock
Thread A reacquires lock
wait() returns
Thread A checks foodReady again
Thread A continues
```

The wake-up is not permission to continue blindly. It is only a hint to reacquire the monitor and check the condition again.

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

Short version:

```text
sleep() = pause, but keep the key
wait()  = pause, and give up the key
```

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

In the food example, waking up does not prove the food is still available. Another thread may have entered first and consumed it. That is why the waiting thread checks the condition again after reacquiring the lock.

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

Step-by-step:

```text
Consumer calls take()
Slot is empty
Consumer calls wait()
Consumer releases this monitor

Producer calls put(10)
Slot becomes full
Producer calls notifyAll()
Producer exits put()

Consumer wakes
Consumer reacquires this monitor
Consumer sees slot is full
Consumer takes 10
Consumer sets slot empty again
Consumer calls notifyAll()
```

This is the same room/key idea:

- empty slot means consumers wait
- full slot means producers wait
- changing the slot wakes the other side
- waking only means "check again"

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

## Interview version

This is commonly asked in Java interviews, especially around thread lifecycle, `synchronized`, and producer-consumer.

Strong short answer:

> `wait()` makes the current thread release the object's monitor and enter `WAITING` until another thread calls `notify()` or `notifyAll()` on the same object. It must be called while holding that monitor. After waking, the thread must reacquire the monitor before `wait()` returns, so the condition must be checked in a `while` loop. `notify()` wakes one arbitrary waiter; `notifyAll()` wakes all waiters. In normal application code, prefer higher-level utilities like `BlockingQueue`.

Common follow-up questions:

- `wait()` vs `sleep()`: `wait()` releases the monitor; `sleep()` keeps locks already held.
- Why inside `synchronized`: the thread must own the same monitor it waits on or notifies.
- Why `while`, not `if`: wake-up is only a hint; the condition may still be false.
- Why `notifyAll()` is safer: `notify()` may wake the wrong waiter when multiple conditions share one monitor.
- What state: plain `wait()` puts the thread in `WAITING`; timed `wait(ms)` puts it in `TIMED_WAITING`.

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
