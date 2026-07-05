---
order: 60
---

# synchronized

---

## Intrinsic lock (monitor lock)

Every Java object has exactly one **intrinsic lock** (also called a **monitor lock**). `synchronized` acquires that lock on entry and releases it on exit — automatically, even if an exception is thrown.

- **Instance method** — locks `this`
- **Static method** — locks the `Class` object (e.g., `Counter.class`)
- **Block** — locks whatever object you pass: `synchronized (lock) { ... }`

```java
// Method form — locks 'this'
public synchronized void increment() {
    count++;
}

// Block form — locks an explicit object
private final Object lock = new Object();

public void increment() {
    synchronized (lock) {
        count++;
    }
}
```

Prefer the **block form** in production code: it lets you choose the lock object, narrow the critical section, and avoid exposing `this` as a lock (external code can synchronize on `this` too, causing interference).

---

## Monitor — ownership and blocking

The monitor model:

- At most **one thread** owns the monitor at a time.
- All other threads trying to enter block on the **entry set** (BLOCKED state).
- When the owner releases, one waiting thread is promoted to owner.

`wait()`, `notify()`, and `notifyAll()` operate on the same monitor and let threads coordinate (Producer-Consumer pattern). You must own the monitor to call them — otherwise `IllegalMonitorStateException`.

---

## Reentrancy

Java's intrinsic lock is **reentrant**: if a thread already owns a lock, it can re-acquire it without blocking. A counter tracks how many times the same thread has acquired it; the lock releases only when the count drops to zero.

**Why it matters:** without reentrancy, this would deadlock:

```java
public synchronized void outer() {
    inner(); // tries to acquire the same lock — deadlock without reentrancy
}

public synchronized void inner() {
    // ...
}
```

With reentrancy, `inner()` increments the hold count and proceeds.

---

## Memory visibility guarantee (JMM)

`synchronized` provides two guarantees, not just one:

1. **Mutual exclusion** — only one thread in the critical section at a time.
2. **Visibility** — an unlock **happens-before** the next lock on the same monitor. All writes made inside a synchronized block are flushed to main memory on unlock and visible to any thread that subsequently acquires the same lock.

Skip synchronization and a thread can observe stale data even without a data race. Mutual exclusion without visibility is still broken.

---

## synchronized vs volatile

| | synchronized | volatile |
|---|---|---|
| Mutual exclusion | Yes | No |
| Visibility guarantee | Yes (unlock → lock HB) | Yes (write → read HB) |
| Atomicity of compound ops | Yes (within the block) | No |
| Blocking | Yes — other threads block | No |
| Use when | Read-modify-write, check-then-act | Single-write flag, safe publication |

`volatile` is lighter: visibility only, no exclusion. Use `synchronized` (or `AtomicXxx`) whenever the operation isn't a single read or write.

---

## Common pitfalls

**Locking on the wrong object (per-call local)**

```java
// BUG: two threads calling process() with equal but distinct String objects
// get different monitors — no mutual exclusion at all
public void process(String id) {
    synchronized (id) { ... }
}
```

**Locking on an interned String literal**

```java
// BUG: string literals are interned — two unrelated classes using the same literal
// share the same String object, so their synchronized blocks interfere with each other
synchronized ("MY_LOCK") { ... }
```

**Locking on a cached boxed Integer**

```java
private Integer counter = 0;
synchronized (counter) { /* BUG: Integer.valueOf(-128..127) returns JVM-cached instances */ }
```

Any code that holds a reference to the same `Integer` box can interfere. Use a dedicated `private final Object lock`.

**Widening lock scope unnecessarily** — holding a lock across IO or long computation increases contention. Keep critical sections small: only the shared-state access, not the surrounding work.

**Exposing `this` as the lock** — external code can `synchronized(yourObject)` and interfere. Prefer a private internal lock object.

---

## Quick recall

**Q. What does `synchronized` acquire?**
A. The intrinsic (monitor) lock on the specified object — `this` for instance methods, `Class` for static methods, or whatever object is passed to a block.

**Q. What does `synchronized` guarantee beyond mutual exclusion?**
A. Memory visibility: an unlock happens-before the next lock on the same monitor, so all writes made inside the block are visible to any thread that subsequently acquires the same lock.

**Q. Why is Java's intrinsic lock reentrant?**
A. So a synchronized method can call another synchronized method on the same object without deadlocking — the JVM tracks a hold count per thread.

**Q. What was biased locking and why was it removed?**
A. An optimization that made repeat single-thread acquires nearly free, but revocation on contention required a safepoint pause — costly in modern multi-threaded workloads. Removed in Java 21.

**Q. synchronized block vs synchronized method — which is preferred and why?**
A. Block form: lets you choose the lock object, narrow the critical section, and avoid exposing `this` as a publicly visible lock.

**Q. Name two lock-object pitfalls and why each breaks synchronization.**
A. (1) Locking on a String literal (`synchronized ("LOCK")`) — interned literals are shared JVM-wide, so unrelated classes can deadlock each other. (2) Locking on a boxed `Integer` in the -128..127 range — `Integer.valueOf()` returns a cached instance shared across code, causing unintended cross-class interference. Both are fixed by using a dedicated `private final Object lock`.
