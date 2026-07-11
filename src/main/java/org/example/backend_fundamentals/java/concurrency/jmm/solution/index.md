---
order: 20
search: false
---

# JMM Solutions

## Solution: visibility-flag - Visibility Flag

```java
class Worker {
    private volatile boolean running = true;

    void stop() {
        running = false;
    }

    void run() {
        while (running) {
            // work
        }
    }
}
```

`volatile` makes writes visible across threads and prevents the loop from relying on a stale cached value. It does not make `count++` atomic.
