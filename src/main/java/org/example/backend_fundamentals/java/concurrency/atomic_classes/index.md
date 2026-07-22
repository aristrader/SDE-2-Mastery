---
order: 60
---

# Atomic Classes

Atomic classes are the smallest correct tool when one shared value needs one atomic operation. They are not a replacement for protecting a larger business invariant.

Use them when the shared state is naturally one value: request count, sequence number, feature flag reference, last-seen timestamp, or current config reference.

Do not use them just because code is "concurrent". If the rule spans multiple fields or multiple objects, use `synchronized`, a lock, a concurrent collection operation, or a higher-level design.

## What atomic means here

An atomic operation appears indivisible to other threads. No other thread can observe the operation half-done.

`AtomicInteger.incrementAndGet()` is atomic. It does not expose a gap between "read current value" and "write incremented value".

```java
AtomicInteger requests = new AtomicInteger();

int after = requests.incrementAndGet(); // increment, then return new value
int before = requests.getAndIncrement(); // return old value, then increment
requests.addAndGet(5);
```

This is why atomics fix the lost-update problem from `count++`:

```java
// Broken: read, increment, write are separate operations.
count++;

// Correct for one counter.
counter.incrementAndGet();
```

## Core classes

| Class | Use for |
|---|---|
| `AtomicInteger`, `AtomicLong` | Counters, sequence numbers, small numeric state |
| `AtomicBoolean` | One atomic true/false flag, such as "started" or "closed" |
| `AtomicReference<T>` | Atomically replacing one object reference |
| `AtomicIntegerArray`, `AtomicLongArray`, `AtomicReferenceArray` | Atomic updates to individual array slots |

Atomic wrappers use volatile-style visibility internally. A successful atomic update is visible to later reads of that atomic variable.

## Read-and-reset must be one operation

Metrics often need "read current count and reset to zero".

Correct:

```java
int current = requests.getAndSet(0);
```

Broken:

```java
int current = requests.get();
requests.set(0);
```

Why broken:

```text
Thread A: get() -> 10
Thread B: incrementAndGet() -> 11
Thread A: set(0)

Thread B's increment is erased.
```

`getAndSet(0)` makes the read and reset one atomic operation, so no increment can slip between them.

## CAS

CAS means compare-and-set:

> Change the value only if it still equals the expected value.

```java
boolean changed = stock.compareAndSet(expected, next);
```

If the current value equals `expected`, the atomic updates it to `next` and returns `true`. If another thread changed it first, the atomic leaves the value alone and returns `false`.

CAS is the basis for many lock-free algorithms. The important beginner point: failed CAS usually means "some other thread won; re-read and retry", not "throw an error".

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

Why the loop is required:

```text
Thread A reads stock = 1
Thread B reads stock = 1
Thread A CAS 1 -> 0 succeeds
Thread B CAS 1 -> 0 fails because current value is now 0
Thread B loops, reads 0, returns false
```

Without CAS, both buyers could pass `stock > 0` and both decrement.

## Atomic update helpers

Prefer the built-in update methods over handwritten CAS loops when the logic is simple.

```java
counter.updateAndGet(current -> Math.max(0, current - 1));
counter.accumulateAndGet(delta, Integer::sum);
reference.updateAndGet(old -> reload(old));
```

These methods still use retry internally, so the function may run more than once under contention. Keep the function side-effect-free. Do not send emails, write audit logs, or mutate external state inside an atomic update function.

## Atomic is not transactional

Two atomic fields do not make one atomic business operation.

```java
class Metrics {
    private final AtomicInteger requests = new AtomicInteger();
    private final AtomicInteger failures = new AtomicInteger();
}
```

Each counter is safe by itself. But "read both and reset both together" is a combined invariant. Another thread can observe requests reset but failures not reset yet.

Bank transfer example:

```java
AtomicInteger sourceBalance = new AtomicInteger(100);
AtomicInteger destinationBalance = new AtomicInteger(0);
```

This does not make a transfer atomic. Decrementing one account and incrementing another account are two separate operations. If a thread crashes or another thread observes the middle, the system can see money disappear or appear.

Use a lock or transaction boundary when the invariant spans multiple values.

## `AtomicReference`

`AtomicReference<T>` is useful when the entire reference is the state.

```java
AtomicReference<Config> config = new AtomicReference<>(loadConfig());

Config current = config.get();
config.set(reloadConfig());
```

This safely publishes the new `Config` reference. It does not make a mutable `Config` object internally thread-safe. Prefer immutable referenced objects.

Conditional replacement:

```java
config.compareAndSet(oldConfig, newConfig);
```

Use this when the update should happen only if nobody changed the reference since you read it.

## `LongAdder`

`LongAdder` is optimized for high-contention counters. Instead of forcing every increment through one hot atomic variable, it spreads updates across internal cells and sums them when read.

| Type | Use when |
|---|---|
| `AtomicInteger` / `AtomicLong` | You need one exact atomic value and frequent immediate reads. |
| `LongAdder` | Many threads increment heavily and reads are less frequent, such as metrics counters. |
| `synchronized` or locks | The invariant spans multiple fields or a multi-step business rule. |

Trade-off: `LongAdder.sum()` is not a single linearizable read against concurrent increments. For metrics, that is usually fine. For stock counts, account balances, or IDs, use `AtomicLong` or a stronger invariant mechanism.

## Pitfalls

- `volatile int count; count++;` is still not atomic.
- `if (stock.get() > 0) stock.decrementAndGet();` is still check-then-act and can oversell.
- One atomic field does not make the whole class thread-safe.
- Atomic update functions can be retried, so they must not have external side effects.
- CAS loops can spin under heavy contention; if the invariant is complex, a simple lock is often clearer and faster enough.

## Quick recall

**Q. When is `AtomicInteger` the smallest correct tool?**
A. One shared integer needs one supported atomic operation, such as increment, add, compare-and-set, or read-and-reset.

**Q. Why is `get()` followed by `set(0)` not an atomic reset?**
A. Another thread can increment between the two calls, then the `set(0)` erases that increment.

**Q. What does CAS do?**
A. It updates only if the current value still equals the expected value.

**Q. Why does a CAS loop retry?**
A. Another thread may change the value between your read and CAS. Retry with the latest observed value.

**Q. Why are two `AtomicInteger`s insufficient for a transfer?**
A. Each field update is atomic, but the business invariant spans both balances.

**Q. Why should atomic update functions avoid side effects?**
A. The function may run more than once under contention due to internal CAS retries.

**Q. When is `LongAdder` preferable?**
A. Highly contended metrics counters where increment throughput matters more than an exact immediate read.

