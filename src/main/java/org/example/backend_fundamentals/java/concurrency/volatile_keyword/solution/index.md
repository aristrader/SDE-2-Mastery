---
title: Solutions
order: 20
search: false
---

# volatile Solutions

## Solution: volatile-stop-signal - Stop Signal

Use `volatile` when the shared state is a simple signal and the operation is "publish the latest value."

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

Why this works:

- The main thread calls `stop()` and writes `false`.
- The worker thread repeatedly reads `running`.
- Because `running` is volatile, the worker cannot legally keep using a permanently stale cached value.
- The next observed value after the write must be `false` or a later write.

This is a good `volatile` use because the operation is only a single read/write flag, not a compound update. There is no invariant such as "check balance then debit" or "read count then write count + 1."

Common trap: `volatile` does not forcibly kill the thread. The worker still has to reach the next loop condition. If `do work` blocks forever on I/O or sleeps for a long time, the stop signal will be delayed.

## Solution: broken-volatile-counter - Broken Volatile Counter

This is still broken:

```java
private static volatile int count;

Runnable task = () -> {
    for (int i = 0; i < 100_000; i++) {
        count++;
    }
};
```

`volatile` only gives visibility for the individual read and write. It does not combine the three steps inside `count++`.

The interleaving can be:

```text
count is 10
Thread A reads 10
Thread B reads 10
Thread A writes 11
Thread B writes 11
```

The final value is `11`, but two increments happened. One update was lost.

Use an atomic read-modify-write operation:

```java
AtomicInteger count = new AtomicInteger();

Runnable task = () -> {
    for (int i = 0; i < 100_000; i++) {
        count.incrementAndGet();
    }
};
```

`AtomicInteger.incrementAndGet()` performs the whole increment as one atomic operation. Internally it uses compare-and-set style coordination so another thread cannot silently overwrite the same old value.

A `synchronized` increment method would also work:

```java
final class Counter {
    private int count;

    synchronized void increment() {
        count++;
    }

    synchronized int value() {
        return count;
    }
}
```

Prefer `AtomicInteger` for one independent numeric counter. Prefer `synchronized` or a lock when several fields must be updated under one invariant.

## Solution: immutable-config-snapshot - Immutable Config Snapshot

The safe pattern is: make the configuration object immutable, then publish the latest reference with `volatile`.

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

Why this works:

- `AppSettings` is immutable, so a reader cannot observe a half-mutated settings object.
- `settings` is volatile, so a reload thread safely publishes the new reference.
- A request thread sees either the old complete settings object or the new complete settings object.

Do not mutate a shared settings object field-by-field like this:

```java
final class MutableSettings {
    String region;
    int timeoutMillis;
    boolean featureEnabled;
}
```

If one thread updates those fields separately, another thread may observe a mixed state such as new `region` with old `timeoutMillis`. A volatile reference does not make the inside of a mutable object automatically atomic.

Interview answer:

> Use a volatile reference to an immutable snapshot. Reload builds a complete new object and assigns it once. Readers read the volatile reference once and use that snapshot.
