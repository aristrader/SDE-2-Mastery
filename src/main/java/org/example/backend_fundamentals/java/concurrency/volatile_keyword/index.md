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

### Stop flag with `start()` and `join()`

This runnable shape shows four separate ideas:

```java
public class Practice {
    public static void main(String[] args) throws InterruptedException {
        Running running = new Running();
        Thread worker = new Thread(running::run);

        worker.start();
        Thread.sleep(2000);
        running.stop();
        worker.join();

        System.out.println("STOPPED");
    }

    static class Running {
        private volatile boolean shutdown;

        void stop() {
            shutdown = true;
        }

        void run() {
            long count = 0;

            while (!shutdown) {
                count++;
            }

            System.out.println("Stopped at " + count);
        }
    }
}
```

What each line is doing:

| Line | Meaning |
|---|---|
| `worker.start()` | Starts the worker thread. Everything before `start()` is visible inside the new thread. |
| `Thread.sleep(2000)` | Only delays the main thread. It does not create a visibility guarantee for `shutdown`. |
| `running.stop()` | Writes `shutdown = true`. This must be visible to the worker for the loop to end. |
| `worker.join()` | Main thread waits until the worker finishes. After `join()`, main sees what the worker wrote before ending. |

Why `volatile` matters:

```java
private volatile boolean shutdown;
```

The main thread writes `shutdown = true`. The worker thread repeatedly reads `shutdown`.
Because the field is volatile, the worker is required to notice the write.

Without `volatile`, this loop is unsafe:

```java
while (!shutdown) {
    count++;
}
```

The worker may keep reading an old cached/optimized value of `shutdown == false`.
The main thread did call `stop()`, but there is no happens-before edge forcing the worker to observe that plain boolean write.

Why `join()` matters:

```java
worker.join();
System.out.println("STOPPED");
```

`join()` is not what stops the worker. `volatile` lets the worker see the stop signal.
`join()` makes the main thread wait until the worker has actually exited.

Without `join()`, main can print `STOPPED` before the worker prints `Stopped at ...`, or before the worker has finished cleanup.

So the responsibilities are:

| Tool | Responsibility |
|---|---|
| `volatile` | Worker sees the stop signal. |
| `join()` | Main waits for worker completion. |
| `start()` | Starts thread and safely hands initial state to it. |
| `sleep()` | Delay only; not a synchronization tool. |

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

**Q. In a stop-flag example, does `join()` replace `volatile`?**
A. No. `volatile` lets the worker see the stop signal; `join()` makes the caller wait for worker completion.

**Q. Does `Thread.sleep()` make another thread see a write?**
A. No. Sleep delays the current thread; it is not a visibility mechanism.

**Q. Does `volatile` provide mutual exclusion?**
A. No. Multiple threads can still execute the same code at the same time.

**Q. Why does double-checked locking require `volatile`?**
A. To prevent publishing a reference before the object is fully constructed and to make the constructed state visible to readers.

**Q. Does a volatile reference make the referenced object thread-safe?**
A. No. It makes reference replacement visible; internal mutable state still needs its own safety.
