---
order: 40
---

# volatile

`volatile` is a visibility and ordering tool for one shared variable. It is not a lock.

Use it when one thread writes a simple state signal and other threads need to see that latest signal without entering a synchronized block.

Good mental model:

```text
volatile = "make this single variable visible across threads"
synchronized = "make this critical section exclusive and visible"
AtomicInteger = "make this one value support atomic updates"
```

## The problem `volatile` solves

Without synchronization, one thread's write does not have to become visible to another thread promptly, or ever in a way your source code suggests.

```java
class Worker {
    private boolean running = true;

    void stop() {
        running = false;
    }

    void runLoop() {
        while (running) {
            doWork();
        }
    }
}
```

One thread calls `stop()`. Another thread runs `runLoop()`. Without `volatile`, the loop is allowed to keep reading a cached or optimized copy of `running == true`.

Fix:

```java
private volatile boolean running = true;
```

Now a write to `running` by one thread happens-before a later read of `running` by another thread.

## What `volatile` guarantees

### Visibility

A write to a `volatile` variable is visible to later reads of that same variable by other threads.

```java
private volatile boolean running = true;

public void stop() {
    running = false; // visible to the loop
}

public void run() {
    while (running) {
        doWork();
    }
}
```

The reader does not need to acquire a lock to see the updated flag.

### Ordering

`volatile` also creates a memory-ordering boundary. Writes before a volatile write cannot be reordered after it in a way that breaks the volatile happens-before rule.

```java
private int value;
private volatile boolean ready;

// Thread A
value = 42;
ready = true;

// Thread B
if (ready) {
    System.out.println(value); // guaranteed to see 42
}
```

The volatile write to `ready` publishes the earlier write to `value`. The volatile read of `ready` receives that publication.

## What `volatile` does not guarantee

### No atomicity for compound operations

`counter++` is not one operation. It is read, increment, write.

```java
private volatile int counter;

void increment() {
    counter++; // still broken
}
```

Two threads can both read `10`, both compute `11`, and both write `11`. The latest value is visible, but one increment was lost.

Fix:

```java
private final AtomicInteger counter = new AtomicInteger();

void increment() {
    counter.incrementAndGet();
}
```

Or use `synchronized` if the update belongs to a larger invariant.

### No mutual exclusion

`volatile` does not stop two threads from entering the same method at the same time.

```java
private volatile boolean initialized;

void initIfNeeded() {
    if (!initialized) {
        initialize();
        initialized = true;
    }
}
```

Two threads can both see `initialized == false` and both call `initialize()`. The flag is visible, but the check-then-act sequence is not atomic.

## Good uses

### Stop flag

```java
class Poller {
    private volatile boolean running = true;

    void run() {
        while (running) {
            pollOnce();
        }
    }

    void stop() {
        running = false;
    }
}
```

This fits because there is one variable, one plain write, and many plain reads.

### Safe publication of immutable config

```java
private volatile Config config = loadInitialConfig();

Config currentConfig() {
    return config;
}

void reload() {
    config = loadNewConfig();
}
```

This works best when `Config` is immutable. The volatile reference replacement is visible to readers. It does not make a mutable `Config` object internally safe.

### Double-checked locking

```java
private volatile Singleton instance;

Singleton getInstance() {
    Singleton local = instance;
    if (local == null) {
        synchronized (this) {
            local = instance;
            if (local == null) {
                local = new Singleton();
                instance = local;
            }
        }
    }
    return local;
}
```

`volatile` is required because object construction is not just "assign reference". The JVM may allocate memory, assign the reference, and initialize fields as separate steps. Volatile prevents a thread from seeing a non-null reference to a partially initialized object.

In real code, prefer simpler initialization patterns when possible: enum singleton, static holder, dependency injection, or eager initialization.

## Not enough cases

| Code shape | Why `volatile` is not enough | Better tool |
|---|---|---|
| `counter++` | Read-modify-write is not atomic | `AtomicInteger`, `LongAdder`, or `synchronized` |
| `if (!started) start()` | Check-then-act race | `synchronized` or `AtomicBoolean.compareAndSet` |
| Update two fields together | Invariant spans multiple values | `synchronized` or lock |
| Mutate object stored in volatile reference | Reference is visible, object internals still race | Immutability or synchronization |
| `map.get()` then `map.put()` | Two separate collection calls | Concurrent map atomic method |

## `volatile long` and `double`

The Java Language Specification guarantees atomic reads/writes for most primitive variables, but non-volatile `long` and `double` have special historical rules on 32-bit JVMs: a 64-bit value could be read or written as two 32-bit halves.

Declaring a `long` or `double` volatile guarantees atomic single reads/writes and visibility. It still does not make `volatileLong++` atomic.

## `volatile` vs `synchronized`

| Question | `volatile` | `synchronized` |
|---|---|---|
| Makes writes visible? | Yes | Yes |
| Prevents reordering around the signal? | Yes | Yes, through lock/unlock |
| Allows only one thread inside a block? | No | Yes |
| Makes compound operations atomic? | No | Yes, if the whole operation is inside the block |
| Can block other threads? | No | Yes |

Interview answer shape: `volatile` is enough for a single variable plain read/write signal. It is not enough for read-modify-write, check-then-act, or invariants across multiple values.

## Quick recall

**Q. What two things does `volatile` guarantee?**
A. Visibility and ordering for accesses to that volatile variable.

**Q. Why is `volatile int counter; counter++` still broken?**
A. `counter++` is read, increment, write. Volatile makes each access visible but does not bundle the three steps atomically.

**Q. What is the best simple use case for `volatile`?**
A. A stop flag: one thread writes `false`, worker threads read it in a loop.

**Q. Does `volatile` provide mutual exclusion?**
A. No. Multiple threads can still execute the same code at the same time.

**Q. Why does double-checked locking require `volatile`?**
A. To prevent publishing a reference before the object is fully constructed and to make the constructed state visible to readers.

**Q. Does a volatile reference make the referenced object thread-safe?**
A. No. It makes reference replacement visible; internal mutable state still needs its own safety.

