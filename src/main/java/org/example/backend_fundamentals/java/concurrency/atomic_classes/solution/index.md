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

Runnable check:

```java
AtomicCounter counter = new AtomicCounter();
ExecutorService executor = Executors.newFixedThreadPool(4);

for (int t = 0; t < 4; t++) {
    executor.submit(() -> {
        for (int i = 0; i < 100_000; i++) {
            counter.increment();
        }
    });
}

executor.shutdown();
executor.awaitTermination(1, TimeUnit.MINUTES);

System.out.println(counter.get()); // 400000
```

`volatile int` would not be enough. `volatile` gives visibility: when one thread writes, other threads are forced to read the latest value. But `count++` is not one operation. It is read, add one, write. Two threads can both read `10`, both calculate `11`, and both write `11`.

`AtomicInteger.incrementAndGet()` performs the increment atomically, so no increment is lost.

Return value difference:

```java
AtomicInteger value = new AtomicInteger(10);

int after = value.incrementAndGet(); // value becomes 11, returns 11
int before = value.getAndIncrement(); // returns 11, then value becomes 12
```

Use `incrementAndGet()` when you need the updated value. Use `getAndIncrement()` when you need the previous value, such as assigning a zero-based sequence number.

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

The loop is needed because the decision depends on the current stock value.

For each attempt:

1. Read the current stock.
2. If it is already zero, return `false`.
3. Try to change `current` to `current - 1`.
4. If the compare-and-set succeeds, this thread bought one item.
5. If it fails, reread and try again.

Two concurrent buyers cannot both buy the final item because only one CAS can change `1` to `0`. The losing thread's CAS fails because the value is no longer the value it observed. On retry, it sees `0` and returns `false`.

A failed CAS does not mean the program is broken. It means another thread made progress first. The correct response is usually to reread the latest state and retry, or stop if the new state means the operation is no longer valid.

Common trap: do not decrement first and then check for negative stock. The check and update must be part of the CAS decision.

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

`getAndSet(0)` returns the old value and resets the counter to zero as one atomic operation. That is exactly what a metrics drain usually needs: "give me everything counted so far, then start a new window."

This version is broken:

```java
int value = requests.get();
requests.set(0);
return value;
```

Suppose the counter is `10`. The reader calls `get()` and sees `10`. Before it calls `set(0)`, another thread increments the counter to `11`. Then the reader calls `set(0)`, erasing that new increment. The returned value is `10`, and the increment that arrived during the reset is lost.

With `getAndSet(0)`, an increment happens either before the reset and is included in the returned value, or after the reset and remains in the counter for the next read.

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

Two independent atomics are not enough when the operation must read and reset both counters consistently. Each atomic protects only itself. It does not create one combined atomic operation across `requests` and `failures`.

This approximate version can be acceptable for loose metrics:

```java
int requestsSnapshot = requests.getAndSet(0);
int failuresSnapshot = failures.getAndSet(0);
```

But it is not a consistent pair. An update can happen between the two resets. For example, a request and its failure could be split across different reporting windows.

The synchronized version uses one monitor for all operations that touch the invariant. `snapshotAndReset()` reads both values and resets both values while holding the same lock, so no other thread can record a request or failure halfway through the snapshot.

Use atomics for single independent values. Use a lock, immutable state with an atomic reference, or another combined coordination mechanism when multiple values must move together.
