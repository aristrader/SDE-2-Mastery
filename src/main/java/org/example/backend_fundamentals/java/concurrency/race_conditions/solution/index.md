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

## Solution: successful-run-is-not-proof - Successful Run Is Not Proof

Correct output only means the harmful interleaving did not happen during those runs. A thread-safe program needs a correctness guarantee such as a monitor, volatile visibility protocol, atomic operation, or safe publication rule.
