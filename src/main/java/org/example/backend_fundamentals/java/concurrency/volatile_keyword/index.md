---
order: 40
---

# volatile

---

## What volatile guarantees

### 1. Visibility

A write to a `volatile` variable **happens-before** every subsequent read of that same variable (JMM guarantee). Any value written by one thread is immediately visible to threads that read it afterward — no caching in CPU registers or thread-local store.

Without `volatile`, the JVM and CPU are free to keep the variable in a register or cache line, and other threads may never see the updated value.

### 2. Ordering (memory barrier)

`volatile` acts as a **memory barrier**: the compiler and CPU cannot reorder accesses across a volatile read or write. All writes that happened before a volatile write are visible to any thread that performs a volatile read of the same variable.

This is the property that makes double-checked locking correct when the instance field is `volatile`.

---

## What volatile does NOT guarantee

### No atomicity for compound operations

`volatile int counter; counter++` is three operations — read, increment, write. Another thread can interleave between any two. The race is real even though `counter` is `volatile`.

```java
// NOT thread-safe even with volatile
private volatile int counter = 0;

public void increment() {
    counter++; // read → increment → write: not atomic
}
```

Use `AtomicInteger` or `synchronized` for read-modify-write.

### No mutual exclusion

Two threads can execute inside the same method simultaneously. `volatile` only guarantees each thread sees the latest written value; it does not prevent concurrent execution.

---

## When volatile is the right choice

**Status flags (single writer, many readers):**

```java
private volatile boolean running = true;

public void run() {
    while (running) {
        // worker loop
    }
}

public void stop() {
    running = false; // write immediately visible to the loop
}
```

Without `volatile`, the JVM may hoist `running` into a register and the loop never terminates even after `stop()` is called.

**Safe publication of immutable objects:**

```java
private volatile Config config;

public void reload(Config newConfig) {
    config = newConfig; // volatile write — full object visible to readers
}
```

**Double-checked locking (Java 5+):**

```java
private volatile Singleton instance;

public Singleton getInstance() {
    if (instance == null) {
        synchronized (this) {
            if (instance == null) {
                instance = new Singleton(); // volatile write prevents partial init escape
            }
        }
    }
    return instance;
}
```

Without `volatile` on `instance`, the partially-constructed object can be published before the constructor finishes (instruction reordering).

---

## When volatile is NOT enough

Any **check-then-act** or **read-modify-write** operation is unsafe with `volatile` alone:

| Operation | Problem | Fix |
|---|---|---|
| `counter++` | Three separate ops — race | `AtomicInteger` or `synchronized` |
| `if (map == null) map = new HashMap<>()` | Two threads both see null | `synchronized` or `AtomicReference` |
| Swapping two fields | Non-atomic pair | `synchronized` |

---

## volatile long and double

On 32-bit JVMs, writes to `long` and `double` (64-bit types) are **not guaranteed atomic** — the JVM may write the two 32-bit halves in separate operations. A reading thread could see a half-written value (**word tearing**).

Declaring the field `volatile` makes the write atomic on all JVMs, including 32-bit. On 64-bit JVMs `long`/`double` writes are already atomic in practice, but `volatile` is still needed for visibility and ordering.

---

## volatile vs synchronized — comparison

| | volatile | synchronized |
|---|---|---|
| Mutual exclusion | No | Yes |
| Visibility | Yes | Yes |
| Ordering | Yes (memory barrier) | Yes (happens-before on lock/unlock) |
| Blocking | No | Yes |
| Compound-op safety | No | Yes |
| Cost | Very low | Higher (contention possible) |

Pick `volatile` when you have a **single shared variable** with one writer and the operation is a plain read or write. Pick `synchronized` (or `Atomic*`) for anything more complex.

---

## Quick recall

**Q. What two things does volatile guarantee?**
A. Visibility (write is immediately visible to subsequent reads) and ordering (no reordering across the volatile access — acts as a memory barrier).

**Q. Why is `volatile int counter; counter++` still a race?**
A. `counter++` is three operations (read, increment, write). Volatile only prevents stale reads; it does not make compound operations atomic.

**Q. Name a case where volatile is the correct and sufficient tool.**
A. A boolean stop-flag: one thread writes `running = false`, many threads read it in a loop. Single write, plain read — volatile is enough.

**Q. Why does double-checked locking require volatile?**
A. Without volatile, the JVM can reorder the constructor call and the reference assignment, publishing a partially-initialized object to other threads.

**Q. What is word tearing and when does volatile prevent it?**
A. On 32-bit JVMs, a `long`/`double` write can split into two 32-bit ops; a reader may see a half-written value. `volatile` makes 64-bit writes atomic.

**Q. Can two threads execute simultaneously with only a volatile field between them?**
A. Yes — volatile provides no mutual exclusion. Both run concurrently; they just see each other's latest writes.

