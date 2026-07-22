---
order: 90
---

# Locks

Start with `synchronized`. Reach for explicit lock classes only when you need a capability `synchronized` does not provide.

`java.util.concurrent.locks` gives you lock objects with methods: acquire, release, try, time out, interrupt, and create separate condition queues.

## `synchronized` vs explicit locks

`synchronized`:

- built into the JVM
- automatically releases the monitor on method/block exit
- gives mutual exclusion and memory visibility
- supports one monitor wait set through `wait()` / `notifyAll()`

`ReentrantLock`:

- must be manually unlocked
- supports `tryLock()`
- supports timed lock attempts
- supports interruptible lock acquisition
- supports multiple `Condition` objects
- can be fair or unfair

Rule: use `synchronized` for simple critical sections. Use `ReentrantLock` when the extra lock features solve a real problem.

## `ReentrantLock`

### Basic pattern

Always unlock in `finally`.

```java
ReentrantLock lock = new ReentrantLock();

lock.lock();
try {
    updateSharedState();
} finally {
    lock.unlock();
}
```

With `synchronized`, the JVM releases the monitor automatically when the block exits. With `ReentrantLock`, forgetting `unlock()` leaves the lock held forever and can freeze every thread that later needs it.

Wrong:

```java
lock.lock();
updateSharedState(); // if this throws, unlock is skipped
lock.unlock();
```

### Reentrancy

`ReentrantLock` is reentrant, like `synchronized`. If the current thread already owns the lock, it can acquire it again. The lock tracks a hold count and releases only when the thread calls `unlock()` the same number of times.

This allows one locked method to call another locked method on the same object without self-deadlocking.

## `tryLock()`

`tryLock()` attempts to acquire the lock without waiting forever.

```java
if (lock.tryLock()) {
    try {
        updateSharedState();
    } finally {
        lock.unlock();
    }
} else {
    return busyResponse();
}
```

Timed form:

```java
if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
    try {
        updateSharedState();
    } finally {
        lock.unlock();
    }
} else {
    return timedOut();
}
```

This is useful when waiting forever would be worse than backing off: avoiding deadlock, returning a graceful "busy" response, or enforcing latency budgets.

## Interruptible locking

A thread waiting to enter a `synchronized` block cannot be interrupted out of that monitor wait.

`ReentrantLock.lockInterruptibly()` can be interrupted while waiting:

```java
lock.lockInterruptibly();
try {
    updateSharedState();
} finally {
    lock.unlock();
}
```

Use it when cancellation matters: shutdown, request timeout, or background worker stop.

## Fairness

```java
ReentrantLock unfair = new ReentrantLock();
ReentrantLock fair = new ReentrantLock(true);
```

Unfair lock:

- default
- higher throughput
- allows barging: a new or just-running thread may acquire before an older waiter
- can starve a waiting thread under heavy contention

Fair lock:

- roughly FIFO under contention
- reduces starvation risk
- lower throughput because it wakes waiters more strictly

Use unfair by default. Use fair only when starvation is a real observed problem or correctness requirement.

## `Condition`

`Condition` is the explicit-lock version of monitor wait/notify, but with one major improvement: one lock can have multiple condition queues.

Single-slot buffer shape:

```java
final class Buffer<T> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    private T value;
    private boolean available;

    void put(T next) throws InterruptedException {
        lock.lock();
        try {
            while (available) {
                notFull.await();
            }

            value = next;
            available = true;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    T take() throws InterruptedException {
        lock.lock();
        try {
            while (!available) {
                notEmpty.await();
            }

            T result = value;
            value = null;
            available = false;
            notFull.signal();
            return result;
        } finally {
            lock.unlock();
        }
    }
}
```

`await()` is like `wait()`:

- caller must hold the lock
- it releases the lock while waiting
- it reacquires the lock before returning
- it must be used in a `while` loop

Separate conditions let producers signal consumers (`notEmpty`) without waking producers waiting for `notFull`.

## `ReentrantReadWriteLock`

Read/write locks split access into two modes:

- Read lock: many readers can hold it together if no writer holds the write lock.
- Write lock: exclusive; no readers or other writers can hold the lock.

```java
ReadWriteLock rw = new ReentrantReadWriteLock();
Lock read = rw.readLock();
Lock write = rw.writeLock();

Config getConfig() {
    read.lock();
    try {
        return config;
    } finally {
        read.unlock();
    }
}

void replaceConfig(Config next) {
    write.lock();
    try {
        config = next;
    } finally {
        write.unlock();
    }
}
```

Use it for read-heavy, write-rare state: configuration snapshots, lookup tables, metadata caches.

Do not assume it always improves performance. If writes are frequent or critical sections are tiny, the bookkeeping overhead can outweigh parallel reads.

## Downgrade vs upgrade

Downgrade is supported: write lock to read lock.

```java
write.lock();
try {
    refresh();
    read.lock(); // acquire read while still holding write
} finally {
    write.unlock(); // now only read lock remains
}

try {
    useRefreshedState();
} finally {
    read.unlock();
}
```

Upgrade is not supported: read lock to write lock.

```java
read.lock();
try {
    write.lock(); // can deadlock
} finally {
    read.unlock();
}
```

Why upgrade can deadlock: two readers may both hold the read lock and both wait for all readers to leave before acquiring the write lock. Each is waiting for the other.

## `StampedLock`

`StampedLock` adds optimistic reads.

```java
long stamp = lock.tryOptimisticRead();
Point snapshot = readPoint();

if (!lock.validate(stamp)) {
    stamp = lock.readLock();
    try {
        snapshot = readPoint();
    } finally {
        lock.unlockRead(stamp);
    }
}
```

Optimistic read means "read without blocking, then validate that no write happened during the read."

Use it only for very read-heavy structures where the extra complexity is justified. `StampedLock` is not reentrant. If a thread holding it tries to acquire it again, it can deadlock itself.

## Choosing the lock

| Need | Tool |
|---|---|
| Simple critical section | `synchronized` |
| Try without waiting forever | `ReentrantLock.tryLock()` |
| Cancel while waiting for lock | `lockInterruptibly()` |
| Multiple wait conditions | `ReentrantLock` + `Condition` |
| Many parallel readers, rare writers | `ReentrantReadWriteLock` |
| Extreme read-heavy optimistic reads | `StampedLock` |

## Quick recall

**Q. Why must `ReentrantLock.unlock()` be in `finally`?**
A. If the critical section throws and unlock is skipped, the lock stays held and other threads can wait forever.

**Q. What does `tryLock()` give you that `synchronized` does not?**
A. A non-blocking or timed attempt to acquire the lock, so code can back off instead of waiting indefinitely.

**Q. What is `lockInterruptibly()` for?**
A. Letting a waiting thread respond to cancellation or shutdown while waiting for a lock.

**Q. Why use multiple `Condition` objects?**
A. To wait and signal separate conditions, such as `notFull` and `notEmpty`, without waking unrelated waiters.

**Q. Fair vs unfair lock?**
A. Fair reduces starvation with FIFO-like ordering but costs throughput. Unfair is the default and usually preferred.

**Q. When does `ReentrantReadWriteLock` help?**
A. Read-heavy, write-rare workloads where read critical sections are large enough to benefit from parallel readers.

**Q. Why is read-to-write upgrade dangerous?**
A. Multiple readers can each wait for the others to release before acquiring write, causing deadlock.

