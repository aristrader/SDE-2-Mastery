---
order: 30
---

# Java Memory Model (JMM)

---

## What the JMM is

The JMM is a **specification** — part of the JLS (§17) — defining precisely when a write by one thread becomes **visible** to a read by another, and in what **order** actions across threads may be observed.

Each CPU architecture (x86, ARM, SPARC) has its own reordering rules. The JMM abstracts over all of them: if your program satisfies its rules, it is portable across all JVMs and hardware.

The JMM does **not** describe physical memory or CPU caches directly. It defines a happens-before partial order over program actions. If a program has a data race, the JMM gives very weak guarantees: reads may observe stale or surprising values, and reasoning from source-code order becomes invalid.

---

## happens-before

**Definition:** action A happens-before action B if the JMM guarantees that A's effects (writes) are visible to B, and that A is not reordered after B.

happens-before is about **visibility and ordering**, not physical time. Two actions can happen at the same wall-clock millisecond yet still be ordered by happens-before. Conversely, A physically executing before B does not imply A happens-before B unless a JMM rule establishes it.

Without a happens-before edge between a write and a read of the same variable, the read is allowed to see a stale value — even if the write physically happened earlier on the hardware.

---

## The 5 key happens-before rules

### 1. Monitor unlock → subsequent lock (same monitor)

An unlock of a monitor M happens-before every subsequent lock of M.

This is the `synchronized` guarantee: everything a thread writes before releasing a lock is visible to the next thread that acquires the same lock.

### 2. Volatile write → subsequent volatile read (same variable)

A write to a `volatile` variable V happens-before every subsequent read of V.

"Subsequent" means in real time across threads: if Thread B reads V and observes A's write, the HB edge is established.

### 3. `Thread.start()` → any action in the started thread

All actions performed by the thread that calls `start()` before calling it are visible to the started thread from its very first action.

```java
int x = 10;          // (1)
new Thread(() -> {
    System.out.println(x);   // guaranteed to see 10
}).start();          // (2) — (1) hb (2) hb everything in new thread
```

### 4. All actions in thread A → `Thread.join()` on A returns in thread B

Every action performed by thread A happens-before `A.join()` returns in thread B. After joining, thread B is guaranteed to see all writes that A ever made.

### 5. Transitivity

If A happens-before B and B happens-before C, then A happens-before C.

This is what makes happens-before useful in chains — no direct edge needed between every pair of actions; reason through the chain.

---

## `volatile` semantics in detail

### Visibility

A write to a `volatile` field is immediately visible to any subsequent read of that field by any thread. The write is flushed from the writing thread's cache to main memory; reads go directly to main memory, bypassing the local cache.

### Ordering

No reordering of memory accesses is allowed across a volatile read or write:
- Writes before a volatile write cannot be moved after it.
- Reads after a volatile read cannot be moved before it.

This is why `volatile` fixes the stale-read and ordering problems from the `running` flag example — it creates a happens-before edge, preventing both the visibility and the reordering issue.

### What `volatile` does NOT give you

`volatile` guarantees visibility and ordering. It does **not** give you atomicity for compound operations.

```java
volatile int counter = 0;
counter++;   // still broken — read, increment, write are 3 ops
```

The three operations are individually visible but not atomically bundled. Another thread can interleave between the read and the write. Use `AtomicInteger` or `synchronized` for read-modify-write.

**Exception:** reads and writes of `volatile long` and `volatile double` are atomic (non-volatile 64-bit types can suffer "word tearing" on 32-bit JVMs where the two 32-bit halves are written separately). Atomicity applies to the single read or write, not to `++`.

---

## `synchronized` semantics

`synchronized` provides two things:

1. **Mutual exclusion (intrinsic lock)** — at most one thread holds the monitor at a time.
2. **Happens-before guarantee** — unlock happens-before the next lock of the same monitor. Everything written inside a `synchronized` block is visible to the next thread that enters a `synchronized` block on the same object.

```java
synchronized (this) {
    state = newValue;   // visible to any thread that subsequently synchronizes on this
}
```

`synchronized` is stronger than `volatile`: it provides visibility/ordering **and** atomicity (the entire critical section is one unit). Trade-off: it serializes concurrent access, which can become a bottleneck.

---

## Double-checked locking (DCL)

DCL is a lazy-initialization pattern that tries to avoid synchronization on every access. Famously broken before Java 5; fixed by the JMM revision.

### Pre-Java 5: broken

```java
private static Singleton instance;

public static Singleton getInstance() {
    if (instance == null) {
        synchronized (Singleton.class) {
            if (instance == null) {
                instance = new Singleton();   // BROKEN
            }
        }
    }
    return instance;
}
```

`new Singleton()` is not a single operation. The JVM may:
1. Allocate memory for the object
2. Write the reference to `instance` (field is now non-null)
3. Execute the constructor (fields of the object are initialized)

Steps 2 and 3 can be reordered. Another thread sees `instance != null` (step 2 done), skips the `synchronized` block, and uses an object whose constructor has not yet run — a partial-construction bug.

### Post-Java 5: fixed with `volatile`

```java
private static volatile Singleton instance;

public static Singleton getInstance() {
    if (instance == null) {
        synchronized (Singleton.class) {
            if (instance == null) {
                instance = new Singleton();
            }
        }
    }
    return instance;
}
```

`volatile` on `instance` creates a happens-before edge: the volatile write of the fully-constructed reference happens-before any volatile read that sees the non-null value. The constructor completes before the reference write; the reference write happens-before any reader; therefore the constructor's writes are visible to all readers.

Without `volatile`, the HB chain is broken: the write inside `synchronized` is only visible to threads that subsequently enter `synchronized` on the same lock. A thread that sees `instance != null` in the outer `if` never enters `synchronized` and has no HB edge to the construction.

---

## Safe publication

An object is **safely published** if both its reference and its internal state are visible to other threads at the same time — no thread sees a partially-constructed object.

Unsafe publication: storing a reference in a shared field from inside the constructor, or assigning to a plain field without synchronization.

### Safe publication idioms

| Idiom | Mechanism |
|---|---|
| Static initializer | JVM guarantees class initialization is serialized; static fields initialized in a static block are safely published to all threads. |
| `volatile` field | Volatile write happens-before any subsequent volatile read of the same field; ensures the reference and its visible state reach readers atomically. |
| `AtomicReference` | Uses volatile semantics internally; `set()` is a volatile write, `get()` is a volatile read. Preferred over a raw volatile field when you need CAS operations alongside safe publication. |
| `final` fields | JMM special rule: after a constructor completes, any thread that reads the object's reference through a properly published channel is guaranteed to see the final fields' values correctly — **even without synchronization on the reference itself**. Only final fields get this guarantee; non-final fields of the same object do not. |
| Lock-guarded field | Publish by writing the reference inside a `synchronized` block on a lock that every reader also acquires before reading. The unlock→lock HB chain propagates the write. |

### Why `final` fields are special

The JMM has a special "freeze" rule for final fields: at the end of a constructor, all writes to `final` fields are "frozen" and any thread that obtains the object's reference sees the frozen values. This is why immutable objects (all fields `final`) are inherently thread-safe once published.

```java
class Point {
    final int x;
    final int y;
    Point(int x, int y) { this.x = x; this.y = y; }
}
```

Any thread that reads a `Point` reference sees the correct `x` and `y` without synchronization, as long as the reference doesn't escape the constructor before assignment.

---

## Trick questions / gotchas

**"It worked in 1,000 test runs."** That proves only that the race did not manifest under those schedules. Thread safety comes from a happens-before guarantee, not observed output.

**"The write happened first in real time."** Real-time order does not imply visibility. Without a happens-before edge, another thread may legally read an older value.

**"The writer synchronized, so the reader is safe."** Only if the reader also uses the same monitor or another compatible visibility mechanism. A synchronized write and plain read are not a complete protocol.

**"Volatile makes this object thread-safe."** A volatile reference makes reference replacement visible. It does not make mutation inside the referenced object atomic or safe.

**"Final fields solve all publication problems."** Final fields get special visibility after construction, but non-final fields in the same object do not. Also, leaking `this` during construction can break the guarantee.

---

## Quick recall

**Q. What does the JMM actually define?**
A. When a write by one thread becomes visible to a read by another, and what orderings are guaranteed — abstracted over all CPU memory models.

**Q. What does happens-before actually guarantee?**
A. That all writes made before action A are visible to any thread that observes the effects of A or anything after it in the HB chain. It is about visibility and ordering, not wall-clock time.

**Q. Name the 5 key happens-before rules.**
A. Monitor unlock → next lock; volatile write → subsequent volatile read; `start()` → actions in started thread; thread actions → `join()` return; transitivity.

**Q. Why does DCL need `volatile` in Java 5+?**
A. Without `volatile`, the JVM can reorder writing the reference before the constructor completes. Another thread sees a non-null reference to a partially-constructed object. `volatile` creates a HB edge ensuring the constructor finishes before the reference write, which happens-before any reader.

**Q. Does `volatile` make `counter++` thread-safe?**
A. No. `volatile` guarantees visibility of each individual read/write, but `counter++` is a read-modify-write compound operation. Use `AtomicInteger` or `synchronized`.

**Q. What makes `final` fields special for thread safety?**
A. The JMM freeze rule: at constructor end, final field values are frozen and visible to any thread that obtains the object's reference, with no additional synchronization required. Non-final fields in the same object do not get this guarantee.

**Q. What is safe publication?**
A. Publishing an object so that both its reference and its state are visible to other threads simultaneously — no thread sees a partially-constructed state. Achieved via static initializer, volatile field, AtomicReference, final fields (freeze rule), or lock-guarded fields.
