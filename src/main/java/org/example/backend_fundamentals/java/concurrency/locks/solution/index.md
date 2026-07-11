---
order: 20
search: false
---

# Locks Solutions

## Solution: trylock-timeout - tryLock Timeout

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

`finally` prevents deadlocks caused by exceptions skipping `unlock()`.
