---
order: 20
search: false
---

# volatile Solutions

## Solution: volatile-stop-signal - Stop Signal

```java
final class Worker implements Runnable {
    private volatile boolean running = true;

    void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            // do work
        }
    }
}
```

This is a good `volatile` use because the operation is a simple read/write flag, not a compound update.
