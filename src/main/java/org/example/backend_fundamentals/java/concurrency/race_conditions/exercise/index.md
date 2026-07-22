---
order: 10
search: false
---

# Race Conditions Practice

## Exercise: lost-update-counter - Lost Update Counter

### Goal
See why `count++` is not atomic.

### Task
Start multiple threads. Each thread increments the same `int` counter many times.

Run the program several times and compare the final count with the expected count.

### Checks
- Explain read-modify-write.
- Explain why the result changes between runs.
- Fix it with `AtomicInteger` or synchronization.

## Exercise: spot-check-then-act - Spot Check-Then-Act

### Goal
Recognize races in code you did not write.

### Task
Explain what can go wrong here when two threads call `purchase()` at the same time:

```java
class Inventory {
    private int stock = 1;

    boolean purchase() {
        if (stock > 0) {
            stock--;
            return true;
        }
        return false;
    }
}
```

### Checks
- Both buyers can observe `stock > 0`.
- The check and decrement must be one critical section or one atomic operation.

## Exercise: successful-run-is-not-proof - Successful Run Is Not Proof

### Goal
Avoid trusting lucky schedules.

### Task
You run an unsafe counter 100 times and it prints the expected value every time. Explain why that does not prove the code is thread-safe.

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

### Checks
- Mention scheduling.
- Mention guarantees vs observations.
- Explain why `count++` can lose updates.
- Explain what guarantee `AtomicInteger` adds.
