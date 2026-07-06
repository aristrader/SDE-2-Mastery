---
order: 20
---

# Race Conditions, Atomicity, Visibility, Ordering

---

## Race condition — definition

A race condition occurs when the **correctness of a program depends on the relative timing or interleaving of threads**. Two or more threads "race" to read or write shared state, and the winner determines the outcome.

Race conditions are not always crashes. The most insidious form is silent corruption — wrong result, program keeps running.

A race condition is a program-level concept. It can arise from an atomicity violation, a visibility problem, an ordering problem, or any combination.

---

## Atomicity

An operation is **atomic** if it completes as a single, indivisible unit from the perspective of other threads — no intermediate state is visible.

### Why `i++` is not atomic

`i++` compiles to three bytecode operations:

1. Read `i` from memory into a register
2. Increment the register value
3. Write the result back to memory

With two threads both executing `i++` on a shared field starting at 0:

```
Thread A: read i → 0
Thread B: read i → 0        (A hasn't written yet)
Thread A: increment → 1
Thread A: write i = 1
Thread B: increment → 1     (B incremented its stale copy)
Thread B: write i = 1       (lost update — expected 2, got 1)
```

Expected final value: 2. Actual: 1. One increment is silently lost.

### Check-then-act

A common compound operation that is non-atomic even though each individual step is:

```java
if (!map.containsKey(key)) {       // check
    map.put(key, computeValue());  // act
}
```

Between the `containsKey` check and the `put`, another thread can insert the same key. The condition was true when checked but false when acted upon.

### Read-modify-write

The `i++` pattern generalized: read a value, compute a new value from it, write it back. Any gap between read and write is a window for another thread to invalidate the computation.

### Put-if-absent as a compound operation

`map.containsKey(key)` + `map.put(key, value)` is a put-if-absent. Even on a `ConcurrentHashMap`, calling these two methods separately is not atomic. Use `map.putIfAbsent(key, value)` — a single atomic operation.

---

## Visibility

**Visibility** is about whether a write made by one thread is ever seen by another thread.

Modern hardware does not write directly to main memory on every store. CPUs have multi-level caches and write buffers. A value written by Thread A on core 1 may sit in core 1's L1/L2 cache; Thread B on core 2 may read a stale value from its own cache indefinitely.

The JLS does not require writes to be immediately visible to other threads unless the program uses synchronization. Without it, the JVM may legally allow threads to see stale values forever.

### Stale-read example

```java
private boolean running = true;

// Thread A
public void stop() {
    running = false;
}

// Thread B
public void run() {
    while (running) {  // may never see running = false
        doWork();
    }
}
```

Thread B may spin forever after Thread A sets `running = false`. The write sits in Thread A's write buffer and never reaches Thread B's cache. Marking `running` as `volatile` fixes this — a volatile write flushes to main memory; a volatile read bypasses the cache.

---

## Ordering

**Ordering** is about whether instructions execute in the order they appear in source code.

Compilers, the JIT, and CPUs all reorder instructions for performance. The JVM spec permits this as long as the program behaves correctly **from the perspective of a single thread** (as-if-serial semantics). Reordering visible to other threads can cause bugs.

### Reordering example

```java
int value = 0;
boolean ready = false;

// Thread A
value = 42;      // (1)
ready = true;    // (2)

// Thread B
if (ready) {            // (3)
    System.out.println(value);  // (4) — can print 0
}
```

The CPU or compiler may reorder (1) and (2) in Thread A — `ready = true` can be written before `value = 42`. Thread B sees `ready == true` but reads `value == 0`.

Without `volatile` or `synchronized`, there is no guarantee Thread B sees the writes in any particular order.

---

## How the three interact

The three problems are distinct but frequently co-occur. A single shared field can suffer from all three at once.

| Problem | Root cause | Broken guarantee |
|---|---|---|
| Atomicity | Multiple operations on shared state are not bundled | A compound operation is split by another thread mid-way |
| Visibility | CPU caches hide writes from other cores | Another thread reads a stale value |
| Ordering | Compiler/CPU reorders stores and loads | Another thread sees writes in the wrong order |

**A single non-atomic compound operation** on a properly-visible, properly-ordered field can still corrupt data (atomicity problem).

**A single atomic write** to a field that lacks visibility guarantees can still be invisible to another thread (visibility problem).

**Two sequentially-correct writes** with visibility guarantees can still be seen out of order by another thread (ordering problem).

---

## Data race vs race condition

Often conflated; related but not identical.

**Data race** — low-level: two threads access the same memory location, at least one access is a write, and there is no happens-before relationship between them. A JMM violation; behavior is undefined.

**Race condition** — higher-level, semantic: the program produces a wrong result because of thread scheduling. A race condition can exist without a data race (e.g., two threads each reading and updating a counter in separate `synchronized` blocks — no data race, but if the business logic requires read-increment-write to be atomic, the program is wrong). Conversely, a data race doesn't always produce an observable bug.

In practice: eliminate data races first (use synchronization), then reason about higher-level race conditions at the compound-operation level.

---

## Quick recall

**Q. What is a race condition?**
A. A bug where the outcome depends on the relative scheduling of threads — the result changes based on which thread "wins the race."

**Q. Why is `i++` not atomic?**
A. It compiles to three operations: read, increment, write. Another thread can interleave between any two, causing a lost update.

**Q. What causes visibility problems?**
A. CPU caches and write buffers — a write stays in one core's cache and is never seen by threads on other cores unless synchronization forces a flush.

**Q. What is a check-then-act race?**
A. A condition is evaluated as true, but by the time the action executes, another thread has changed the state. The classic fix is to make the check-and-act a single atomic operation.

**Q. Why can instructions appear out of order to another thread?**
A. Compilers, JIT, and CPUs reorder instructions for performance. Single-thread correctness is preserved but cross-thread visibility of the reordering is not.

**Q. What is the difference between a data race and a race condition?**
A. A data race is a JMM-level violation (unsynchronized concurrent access with at least one write). A race condition is a semantic bug from wrong timing. Data races produce undefined behavior; race conditions can exist even with properly synchronized code if the atomicity scope is wrong.

**Q. Is `ConcurrentHashMap.containsKey(k) + put(k, v)` thread-safe?**
A. No — two separate method calls are not atomic. Use `putIfAbsent(k, v)` for a single atomic operation.

