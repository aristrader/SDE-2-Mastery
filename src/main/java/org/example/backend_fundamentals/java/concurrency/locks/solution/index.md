---
order: 20
search: false
---

# Locks Solutions

## Solution: trylock-timeout - tryLock Timeout

Use `tryLock(timeout, unit)` when the caller should give up instead of waiting forever:

```java
ReentrantLock lock = new ReentrantLock();
boolean acquired = lock.tryLock(100, TimeUnit.MILLISECONDS);
if (!acquired) {
    return;
}
try {
    // protected work
} finally {
    lock.unlock();
}
```

The important rule is: unlock only if this thread actually acquired the lock.

Full method shape:

```java
boolean process() throws InterruptedException {
    boolean acquired = lock.tryLock(100, TimeUnit.MILLISECONDS);
    if (!acquired) {
        return false;
    }

    try {
        updateSharedState();
        return true;
    } finally {
        lock.unlock();
    }
}
```

`finally` prevents deadlocks caused by exceptions skipping `unlock()`. Without it, one exception can leave the lock held forever and every later caller can block.

Why use this instead of `synchronized`:

- `synchronized` waits until the monitor is available.
- `ReentrantLock.tryLock(timeout, unit)` lets the caller bound how long it waits.
- `tryLock` is useful for avoiding deadlock-prone waits or returning a clean "busy, try again later" result.

Common traps:

- Do not call `unlock()` if `tryLock` returned `false`.
- Do not swallow `InterruptedException`; either propagate it or restore interruption with `Thread.currentThread().interrupt()`.
- Do not hold the lock around slow I/O unless the shared-state invariant really requires it.
