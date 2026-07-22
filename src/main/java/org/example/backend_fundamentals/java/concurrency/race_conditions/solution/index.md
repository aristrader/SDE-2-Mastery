---
order: 20
search: false
---

# Race Conditions Solutions

## Solution: lost-update-counter - Lost Update Counter

The unsafe counter usually looks harmless:

```java
final class UnsafeCounter {
    private int count;

    void increment() {
        count++;
    }

    int value() {
        return count;
    }
}
```

The problem is that `count++` is not atomic. It is a read-add-write sequence:

```text
read count
add 1
write count
```

Two threads can both read the same old value and both write the same new value. That is a lost update.

For one independent counter, use `AtomicInteger`:

```java
AtomicInteger count = new AtomicInteger();

Runnable task = () -> {
    for (int i = 0; i < 100_000; i++) {
        count.incrementAndGet();
    }
};
```

`AtomicInteger.incrementAndGet()` makes the whole increment one atomic operation. Another correct solution is to guard the counter with one monitor:

```java
final class SafeCounter {
    private int count;

    synchronized void increment() {
        count++;
    }

    synchronized int value() {
        return count;
    }
}
```

Pick the tool based on the invariant. `AtomicInteger` is good for one standalone number. `synchronized` is better when the counter must stay consistent with other fields.

## Solution: spot-check-then-act - Spot Check-Then-Act

Both threads can read `stock == 1`, both enter the `if`, and both decrement. The business invariant is "only one buyer gets the final item," so the check and decrement must be protected together.

Broken shape:

```java
boolean purchase() {
    if (stock <= 0) {
        return false;
    }

    stock--;
    return true;
}
```

The bug is not only the decrement. The bug is splitting this decision:

```text
if stock is available, reserve exactly one item
```

Correct monitor-based version:

```java
synchronized boolean purchase() {
    if (stock <= 0) {
        return false;
    }
    stock--;
    return true;
}
```

Now only one thread at a time can check and decrement. If the first buyer gets the last item, the second buyer enters later and sees `stock == 0`.

A CAS loop with `AtomicInteger` also works:

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

Interview wording:

> Check-then-act must be one atomic operation. Either guard the whole check and update with the same lock, or use a CAS operation that validates the value has not changed before writing the new value.

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

Why a few correct-looking runs prove nothing:

```text
Thread A reads 41
Thread B reads 41
Thread A writes 42
Thread B writes 42
```

The final value is `42`, but two increments happened. That bad interleaving may happen rarely, often, or only under load. The fact that one local run printed the expected number only means the scheduler did not expose the bug that time.

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

A synchronized version is also safe:

```java
class SynchronizedCounter {
    private int count;

    synchronized void increment() {
        count++;
    }

    synchronized int get() {
        return count;
    }
}
```

Good interview conclusion:

> Thread-safety is not proved by testing until it passes once. It is proved by showing the shared state has a happens-before, atomicity, or locking rule that prevents the bad interleaving.
