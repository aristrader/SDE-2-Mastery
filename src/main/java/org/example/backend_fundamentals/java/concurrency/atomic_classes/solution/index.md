---
order: 20
search: false
---

# Atomic Classes Solutions

## Solution: atomic-counter - Atomic Counter

```java
final class AtomicCounter {
    private final AtomicInteger value = new AtomicInteger();

    void increment() {
        value.incrementAndGet();
    }

    int get() {
        return value.get();
    }
}
```

`volatile` would make writes visible, but `count++` is still read-modify-write. `incrementAndGet()` returns the new value; `getAndIncrement()` returns the old value.

## Solution: bounded-stock-cas - Bounded Stock CAS

```java
final class Inventory {
    private final AtomicInteger stock = new AtomicInteger(1);

    boolean purchase() {
        while (true) {
            int current = stock.get();
            if (current <= 0) {
                return false;
            }
            if (stock.compareAndSet(current, current - 1)) {
                return true;
            }
        }
    }
}
```

A failed CAS means another thread changed `stock` after the read. The loop retries with the latest value.

## Solution: read-and-reset - Atomic Read And Reset

```java
final class MetricsCounter {
    private final AtomicInteger requests = new AtomicInteger();

    void increment() {
        requests.incrementAndGet();
    }

    int readAndReset() {
        return requests.getAndSet(0);
    }
}
```

`getAndSet(0)` is one atomic operation. `get()` followed by `set(0)` can erase increments that arrive between the two calls.

## Solution: two-counter-snapshot - Two Counter Snapshot

```java
record MetricsSnapshot(int requests, int failures) {
}

final class Metrics {
    private int requests;
    private int failures;

    synchronized void recordRequest() {
        requests++;
    }

    synchronized void recordFailure() {
        failures++;
    }

    synchronized MetricsSnapshot snapshotAndReset() {
        MetricsSnapshot snapshot = new MetricsSnapshot(requests, failures);
        requests = 0;
        failures = 0;
        return snapshot;
    }
}
```

The invariant spans both counters, so one critical section is clearer than two unrelated atomics.
