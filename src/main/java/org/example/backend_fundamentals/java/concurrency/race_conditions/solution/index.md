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

## Solution: spot-check-then-act - Spot Check-Then-Act

Both threads can read `stock == 1`, both enter the `if`, and both decrement. The business invariant is "only one buyer gets the final item," so the check and decrement must be protected together.

```java
synchronized boolean purchase() {
    if (stock <= 0) {
        return false;
    }
    stock--;
    return true;
}
```

A CAS loop with `AtomicInteger` also works, but `synchronized` is the clearest first answer.

```java
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
```

The loop retries only when another thread changed `stock` between the check and the decrement. The successful `compareAndSet` is the atomic check-and-act step.

## Solution: successful-run-is-not-proof - Successful Run Is Not Proof

Not safe:

```java
class UnsafeCounter {
    private int count;

    void increment() {
        count++;
    }

    int get() {
        return count;
    }
}
```

Safe:

```java
class SafeCounter {
    private final AtomicInteger count = new AtomicInteger();

    void increment() {
        count.incrementAndGet();
    }

    int get() {
        return count.get();
    }
}
```

Correct output only means the harmful interleaving did not happen during those runs. A thread-safe program needs a correctness guarantee such as a monitor, volatile visibility protocol, atomic operation, or safe publication rule.

`count++` is three steps: read, add, write. Two threads can read the same old value, then both write the same new value. `AtomicInteger.incrementAndGet()` makes that update one atomic operation, so the lost-update interleaving is not possible.
