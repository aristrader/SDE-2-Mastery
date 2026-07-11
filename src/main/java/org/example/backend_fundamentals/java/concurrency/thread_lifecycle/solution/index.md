---
order: 20
search: false
---

# Thread Lifecycle Solutions

## Solution: thread-state-observation - Observe Thread States

```java
Object lock = new Object();

Thread sleeper = new Thread(() -> {
    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

System.out.println(sleeper.getState()); // NEW
sleeper.start();
Thread.sleep(50);
System.out.println(sleeper.getState()); // usually TIMED_WAITING
sleeper.join();
System.out.println(sleeper.getState()); // TERMINATED
```

Thread states are snapshots. The scheduler can move a thread between states before you print.
