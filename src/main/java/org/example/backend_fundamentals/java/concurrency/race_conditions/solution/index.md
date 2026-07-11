---
order: 20
search: false
---

# Race Conditions Solutions

## Solution: lost-update-counter - Lost Update Counter

```java
AtomicInteger count = new AtomicInteger();

Runnable task = () -> {
    for (int i = 0; i < 100_000; i++) {
        count.incrementAndGet();
    }
};
```

`count++` is read, add, write. Two threads can read the same old value and overwrite each other. `AtomicInteger.incrementAndGet()` makes the update atomic.
