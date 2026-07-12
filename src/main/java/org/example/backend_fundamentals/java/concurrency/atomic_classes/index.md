---
order: 60
---

# Atomic Classes

Atomic classes are the smallest correct tool when one shared value needs one supported atomic operation. They are not a replacement for protecting a larger business invariant.

## Core Operations

`AtomicInteger`, `AtomicLong`, `AtomicBoolean`, and `AtomicReference` wrap one value and expose atomic reads, writes, and updates.

```java
AtomicInteger requests = new AtomicInteger();

requests.incrementAndGet(); // increment, then return new value
requests.getAndIncrement(); // return old value, then increment
requests.addAndGet(5);
requests.getAndSet(0);      // atomic read-and-reset
```

`getAndSet(0)` matters for metrics resets. This is broken:

```java
int current = requests.get();
requests.set(0);
```

An increment can arrive between the `get()` and `set(0)` and then be erased.

## CAS

Compare-and-set means:

> Change the value only if it still equals the expected value.

```java
boolean purchase(AtomicInteger stock) {
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

The loop is required because another thread may change the value after the read. A failed CAS means "retry with the newer value," not "the program failed."

## Atomic Is Not Transactional

Two atomic fields do not make one atomic business operation.

```java
AtomicInteger requests = new AtomicInteger();
AtomicInteger failures = new AtomicInteger();
```

Each counter is safe by itself. But "read both and reset both together" is a combined invariant. Another thread can observe one reset and the other not reset unless the whole operation is coordinated.

Use `synchronized` or another higher-level coordination mechanism when several fields must change as one unit.

## LongAdder

`LongAdder` is often better for high-contention metrics where many threads increment and reads are less frequent.

| Type | Use when |
|---|---|
| `AtomicInteger` / `AtomicLong` | You need one exact atomic value and immediate reads. |
| `LongAdder` | You need high-throughput counters under heavy contention. |
| `synchronized` | You need a multi-step or multi-field invariant. |

## Pitfalls

- `volatile int count; count++;` is still not atomic.
- `AtomicInteger stock; if (stock.get() > 0) stock.decrementAndGet();` is still check-then-act.
- One atomic field does not make the whole class thread-safe.
- CAS loops are correct but less readable; prefer `synchronized` when the invariant spans several values or the CAS logic becomes clever.

## Quick recall

**Q. When is `AtomicInteger` the smallest correct tool?**
A. One shared integer needs one supported atomic operation, such as increment, add, compare-and-set, or read-and-reset.

**Q. Why is `get()` followed by `set(0)` not an atomic reset?**
A. Another thread can increment between the two calls and that increment can be lost.

**Q. What does CAS do?**
A. It updates only if the current value still matches the expected value.

**Q. Why does a CAS loop retry?**
A. Contention means another thread changed the value first; retry using the latest observed value.

**Q. Why are two `AtomicInteger`s insufficient for a bank transfer?**
A. Each field update is atomic, but the transfer invariant spans both balances.

**Q. When is `LongAdder` preferable?**
A. Highly contended metrics counters where write throughput matters more than a single immediately consistent value.
