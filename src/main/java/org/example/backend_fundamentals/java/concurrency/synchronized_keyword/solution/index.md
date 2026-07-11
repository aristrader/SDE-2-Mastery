---
order: 20
search: false
---

# synchronized Solutions

## Solution: synchronized-counter - Synchronized Counter

```java
final class Counter {
    private int value;

    synchronized void increment() {
        value++;
    }

    synchronized int value() {
        return value;
    }
}
```

For instance synchronized methods, the monitor is `this`. Only one thread can execute a synchronized instance method on the same object at a time.
