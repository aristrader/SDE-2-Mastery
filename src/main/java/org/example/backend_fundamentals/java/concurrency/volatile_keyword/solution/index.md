---
title: Solutions
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

## Solution: broken-volatile-counter - Broken Volatile Counter

```java
private static volatile int count;

Runnable task = () -> {
    for (int i = 0; i < 100_000; i++) {
        count++;
    }
};
```

`count++` still reads, increments, and writes as separate steps.

```java
AtomicInteger count = new AtomicInteger();

Runnable task = () -> {
    for (int i = 0; i < 100_000; i++) {
        count.incrementAndGet();
    }
};
```

`AtomicInteger` gives an atomic read-modify-write operation. A `synchronized` increment method would also work when more state must be guarded together.

## Solution: immutable-config-snapshot - Immutable Config Snapshot

```java
record AppSettings(String region, int timeoutMillis, boolean featureEnabled) {
}

final class SettingsHolder {
    private volatile AppSettings settings;

    SettingsHolder(AppSettings initial) {
        this.settings = initial;
    }

    AppSettings get() {
        return settings;
    }

    void reload(AppSettings updated) {
        settings = updated;
    }
}
```

The reference update is visible, and the object is immutable. Readers see one complete version or another.
