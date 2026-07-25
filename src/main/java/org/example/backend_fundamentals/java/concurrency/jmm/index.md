---
order: 30
---

# Java Memory Model (JMM)

The Java Memory Model answers one practical question:

> When one thread writes something, what makes another thread guaranteed to see it?

If you remember only one rule, remember this:

> Threads do not share "source-code order." They share only what the JMM says is safely visible.

Without a JMM guarantee, code can pass 1,000 times and still be broken.

---

## The problem it solves

This looks simple:

```java
class Worker {
    private boolean running = true;

    void stop() {
        running = false;
    }

    void run() {
        while (running) {
            // work
        }
    }
}
```

One thread calls `run()`. Another thread calls `stop()`.

The bug: the running thread is not guaranteed to notice `running = false`.

Why? Because there is no rule connecting the write in one thread to the read in the other thread. The JVM and CPU can cache, reorder, or optimize as long as single-thread behavior still looks correct.

Fix:

```java
class Worker {
    private volatile boolean running = true;

    void stop() {
        running = false;
    }

    void run() {
        while (running) {
            // work
        }
    }
}
```

`volatile` creates the missing visibility rule.

---

## The mental model

Do not think:

> "Thread A wrote first, so Thread B must see it."

Think:

> "What creates the happens-before edge from Thread A's write to Thread B's read?"

If there is no happens-before edge, the read is a data race. A data race means the result is not something you can reason about from source-code order.

---

## Happens-before

`happens-before` means:

- writes before A are visible to B;
- A cannot be reordered after B in a way that breaks the guarantee.

It does not mean wall-clock time.

This is safe:

```java
class MessageBox {
    private String message;
    private volatile boolean ready;

    void publish() {
        message = "done";
        ready = true;
    }

    String read() {
        return ready ? message : "not ready";
    }
}
```

The write to `message` happens before the volatile write to `ready`.
The volatile write to `ready` happens before another thread's volatile read of `ready`.
So if `read()` sees `ready == true`, it must also see `message = "done"`.

The important part: `volatile` does not publish only the `ready` field.
It also publishes normal writes that happened before the volatile write.

Think of `ready = true` as the publish signal:

```text
Thread A
message = "done"   // prepare data
ready = true       // publish signal

Thread B
if (ready) {       // receives signal
    use message;   // guaranteed to see prepared data
}
```

The reader must actually read the same volatile field for this to work.
This has no guarantee:

```java
String readWithoutSignal() {
    return message; // did not read ready first
}
```

The happens-before chain is:

```text
message write
    happens-before, because of source order inside Thread A
volatile write to ready
    happens-before
volatile read of ready that sees true
    happens-before, because of source order inside Thread B
message read
```

By transitivity, the `message` write is visible to the `message` read.

---

## Rules you actually use

| Rule | What it means |
|---|---|
| `synchronized` unlock → later lock on same object | Writes before exiting the lock are visible to the next thread entering the same lock. |
| `volatile` write → later read of same field | Writes before the volatile write are visible after the volatile read sees it. |
| `Thread.start()` | Things done before `start()` are visible inside the new thread. |
| `Thread.join()` | After `join()` returns, the joining thread sees what the finished thread wrote. |
| Transitivity | If A happens-before B and B happens-before C, then A happens-before C. |

Most interview answers reduce to picking the right row from this table.

---

## `volatile`: visibility, not locking

Use `volatile` when one thread writes a simple state signal and other threads read it.

Good:

```java
private volatile boolean shutdown;

void stop() {
    shutdown = true;
}

void run() {
    while (!shutdown) {
        // work
    }
}
```

Not enough:

```java
private volatile int count;

void increment() {
    count++;
}
```

`count++` is still:

1. read current value;
2. add one;
3. write new value.

`volatile` makes each read/write visible. It does not combine the three steps into one atomic action.

Use this instead:

```java
private final AtomicInteger count = new AtomicInteger();

void increment() {
    count.incrementAndGet();
}
```

Or use `synchronized` if multiple fields must change together.

---

## `synchronized`: visibility plus atomicity

`synchronized` gives two guarantees:

- only one thread enters the protected block at a time;
- unlock happens-before the next lock on the same monitor.

```java
class Counter {
    private int count;

    synchronized void increment() {
        count++;
    }

    synchronized int get() {
        return count;
    }
}
```

This works because both read and write use the same monitor.

This is incomplete:

```java
synchronized void increment() {
    count++;
}

int get() {
    return count; // unsafe plain read
}
```

The writer synchronized, but the reader did not participate in the same visibility protocol.

---

## Safe publication

Safe publication means another thread sees both:

- the object reference;
- the state written during construction.

Unsafe shape:

```java
class Holder {
    static Holder instance;
    int value;

    Holder() {
        value = 42;
        instance = this; // reference escapes during construction
    }
}
```

Another thread can see `instance` before construction is safely complete.

The confusing part is that the constructor may really have run in the creating thread, while another thread still does not have a guarantee that it sees the constructor's writes.

Example:

```java
class Service {
    static Service instance;
    int port;

    Service() {
        port = 8080;
    }

    static void publish() {
        instance = new Service();
    }
}
```

Another thread can do this:

```java
Service service = Service.instance;

if (service != null) {
    System.out.println(service.port);
}
```

Without safe publication, the second thread may see:

```text
service != null
service.port == 0
```

That does not mean the constructor skipped `port = 8080` in the creating thread.
It means the reference write and the field write were not safely published together to the reading thread.

Object construction has separate effects:

```text
1. allocate memory with default values
2. run constructor writes, such as port = 8080
3. publish the reference
```

Safe publication gives other threads a rule that if they see the reference, they also see the constructor-written state.

The core mistake is publishing the reference without a happens-before edge:

```java
static Service instance; // plain shared reference

static void publish() {
    instance = new Service(); // unsafe publication
}
```

The fix is not "constructor finished." The fix is "reader receives the reference through a safe publication mechanism."

Common safe publication options:

| Option | Why it works |
|---|---|
| Static initializer | Class initialization is safely serialized by the JVM. |
| `volatile` reference | Volatile write/read creates visibility for the published reference and prior writes. |
| `AtomicReference` | Uses volatile-style visibility and supports CAS. |
| Lock-guarded field | Writer and reader use the same lock. |
| Immutable object with `final` fields | Final fields get a special constructor-completion guarantee, if `this` does not escape. |

Examples:

```java
class Config {
    final int timeoutMillis;
    final String region;

    Config(int timeoutMillis, String region) {
        this.timeoutMillis = timeoutMillis;
        this.region = region;
    }
}
```

This object is immutable, but the reference still needs to be published safely unless it is created and handed off through a safe mechanism.

Safe through class initialization:

```java
class ConfigHolder {
    static final Config CONFIG = new Config(500, "ap-south-1");
}
```

Safe through volatile reference:

```java
class ConfigRegistry {
    private static volatile Config config;

    static void publish(Config newConfig) {
        config = newConfig;
    }

    static Config read() {
        return config;
    }
}
```

Safe through same lock:

```java
class LockedRegistry {
    private Config config;

    synchronized void publish(Config newConfig) {
        config = newConfig;
    }

    synchronized Config read() {
        return config;
    }
}
```

Important distinction:

| Case | Safe publication? | Object thread-safe after publication? |
|---|---:|---:|
| Immutable object with `final` fields, safely published | Yes | Yes, if no mutable internals leak |
| Mutable object, safely published | Yes | Not automatically |
| Object stored in plain static field without synchronization | No | No |
| Object put into a concurrent collection | Usually yes for handoff | Only collection operations are protected |

Safe publication is about the initial handoff. It does not make later mutations safe.

---

## Double-checked locking

Double-checked locking is a lazy singleton pattern:

> Create the singleton only on the first call, but avoid synchronization after it has already been created.

Do not start here if lazy initialization is not required.
Use eager initialization instead:

```java
class Singleton {
    private static final Singleton INSTANCE = new Singleton();

    private Singleton() {
    }

    static Singleton getInstance() {
        return INSTANCE;
    }
}
```

This works because JVM class initialization is thread-safe. The class is initialized once, and all threads safely see the initialized static fields.

The private constructor matters because it blocks outside code from creating more instances:

```java
// Not allowed if constructor is private
new Singleton();
```

Naive lazy singleton is broken:

```java
class Singleton {
    private static Singleton instance;

    private Singleton() {
    }

    static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}
```

Two threads can both see `instance == null` and both create an object.

The simple correct lazy version is synchronized:

```java
class Singleton {
    private static Singleton instance;

    private Singleton() {
    }

    static synchronized Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}
```

This is correct because only one thread can enter `getInstance()` at a time.
The cost is that every call synchronizes, even after the singleton already exists.

Double-checked locking tries to avoid that repeated synchronization:

Broken without `volatile`:

```java
class Singleton {
    private static Singleton instance;

    private Singleton() {
    }

    static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

The two checks do different jobs:

```java
if (instance == null) {         // first check: fast path, avoid lock later
    synchronized (...) {
        if (instance == null) { // second check: prevent duplicate creation
            instance = new Singleton();
        }
    }
}
```

Why the second check is required:

```text
Thread A passes first check
Thread B passes first check
Thread A enters lock and creates instance
Thread B enters lock later
Thread B must check again, otherwise it creates a second instance
```

So the inner check prevents duplicate creation.

`volatile` solves a different problem: safe publication.

Without `volatile`, the outer `if` reads `instance` without synchronization.
A thread can observe `instance != null`, skip the lock, and return the reference without a happens-before edge to the constructor writes.

Fixed:

```java
class Singleton {
    private static volatile Singleton instance;

    private Singleton() {
    }

    static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

With `volatile`, this write:

```java
instance = new Singleton();
```

is a volatile write to `instance`, and this read:

```java
if (instance == null)
```

is a volatile read of `instance`.

If the reader sees the non-null value written by the creator thread, it must also see the writes that happened before that volatile write, including constructor-written state.

Best lazy singleton answer for interviews is often the static holder:

```java
class Singleton {
    private Singleton() {
    }

    private static class Holder {
        private static final Singleton INSTANCE = new Singleton();
    }

    static Singleton getInstance() {
        return Holder.INSTANCE;
    }
}
```

This is lazy because `Holder` is not initialized until `getInstance()` touches `Holder.INSTANCE`.
It is thread-safe because class initialization is safely serialized by the JVM.
No manual `volatile` or `synchronized` is needed.

Use this decision tree:

| Need | Use |
|---|---|
| Singleton, lazy not needed | `private static final Singleton INSTANCE` |
| Singleton, lazy needed | Static holder |
| Asked specifically about JMM/DCL | `volatile` double-checked locking |

Double-checked locking is mostly useful as a JMM interview question. In real code, prefer static final, static holder, enum singleton, or dependency injection.

---

## How to answer JMM questions

Use this order:

1. Name the shared state.
2. Name the read and write.
3. Ask what creates the happens-before edge.
4. If there is no edge, call it a data race.
5. Pick the smallest fix: `volatile`, `AtomicInteger`, or `synchronized`.

Examples:

| Problem | Smallest fix |
|---|---|
| Stop flag | `volatile boolean` |
| Counter increment | `AtomicInteger` or `synchronized` |
| Multiple fields updated together | `synchronized` or a lock |
| Publish immutable config | final fields plus safe publication |
| Lazy singleton | static holder, enum, or `volatile` DCL |

---

## Common traps

**"It worked in my test."**
That proves only that the bad schedule did not happen.

**"The write happened first."**
Wall-clock order is not enough. You need happens-before.

**"I used `volatile`, so the object is thread-safe."**
`volatile` makes the reference visible. It does not make mutations inside the object atomic.

**"The writer synchronized."**
The reader must use the same lock, or another valid visibility mechanism.

**"`final` solves everything."**
`final` helps constructor visibility for final fields. It does not protect later mutation.

---

## Quick recall

**Q. What does the JMM define?**
A. The visibility and ordering guarantees between threads.

**Q. What is happens-before?**
A. A rule that makes earlier writes visible to a later action.

**Q. Does real-time order imply happens-before?**
A. No. A write can happen earlier in time and still not be visible.

**Q. What does `volatile` give?**
A. Visibility and ordering for reads/writes of that field, not atomic compound updates.

**Q. Why is `count++` unsafe with `volatile int`?**
A. It is read, add, write; another thread can interleave between those steps.

**Q. What does `synchronized` give?**
A. Mutual exclusion plus unlock-to-lock visibility on the same monitor.

**Q. What is safe publication?**
A. Publishing an object so other threads see both the reference and the constructed state.

**Q. Does safe publication make a mutable object thread-safe forever?**
A. No. It only makes the initial handoff visible; later mutation still needs synchronization.

**Q. In double-checked locking, what prevents duplicate creation?**
A. The second `instance == null` check inside the synchronized block.

**Q. In double-checked locking, why is `volatile` required?**
A. To safely publish the constructed object to threads that read `instance` outside the lock.

**Q. What singleton should you use when lazy initialization is not needed?**
A. `private static final Singleton INSTANCE = new Singleton();`.

**Q. What singleton is usually better than manual double-checked locking for lazy initialization?**
A. The static holder idiom.
