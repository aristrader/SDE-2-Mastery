---
order: 80
---

# wait, notify, notifyAll

`wait()` and `notifyAll()` are low-level monitor coordination APIs. Learn them so thread dumps and old code make sense; prefer `BlockingQueue`, executors, latches, semaphores, or higher-level APIs in normal application code.

## Contract

`wait()` must be called while holding the object's monitor.

```java
synchronized (lock) {
    while (!condition) {
        lock.wait();
    }
    useState();
}
```

Calling `wait()` releases the monitor and suspends the current thread. When the thread wakes, it must reacquire the monitor before continuing.

Use `while`, not `if`, because:

- wakeups can be spurious
- another thread may acquire the lock first and consume the condition
- `notifyAll()` may wake several threads, but only one can make progress

## Producer-Consumer Shape

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

`notifyAll()` is usually safer than `notify()` when multiple condition types share one monitor, such as "not full" and "not empty."

## Interruption

For reusable coordination utilities, prefer declaring `throws InterruptedException` and let the caller decide whether to retry, exit, or shut down.

If you catch `InterruptedException` and cannot throw it, restore the interrupt flag:

```java
catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    return;
}
```

## Why Higher-Level APIs Usually Win

The raw version requires careful condition loops, notification, shutdown, poison pills, interruption behavior, and lock discipline.

For a bounded producer-consumer queue, prefer:

```java
BlockingQueue<Task> queue = new ArrayBlockingQueue<>(100);
```

## Quick recall

**Q. What does `wait()` do to the monitor?**
A. It releases the monitor while waiting, then reacquires it before returning.

**Q. Why must condition checks use `while`?**
A. The condition may be false after wakeup due to spurious wakeups or another thread winning the lock first.

**Q. Why use `notifyAll()` instead of `notify()`?**
A. It avoids waking the wrong kind of waiter when multiple conditions share a lock.

**Q. Why is raw wait/notify rare in backend code?**
A. Higher-level concurrency utilities encode the coordination with fewer ways to get it wrong.

**Q. What should code do when it catches `InterruptedException` and cannot rethrow?**
A. Restore the interrupt status with `Thread.currentThread().interrupt()`.
