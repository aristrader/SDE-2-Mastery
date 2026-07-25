---
order: 50
---

# synchronized

`synchronized` is Java's built-in mutual exclusion mechanism. It protects a critical section with an object's intrinsic monitor lock.

It gives two guarantees:

- Mutual exclusion: only one thread can hold the monitor at a time.
- Visibility: writes made before unlocking are visible to a later thread that locks the same monitor.

Use it when shared state needs a multi-step operation to behave as one unit.

## What lock is acquired?

Every Java object can be used as a monitor lock.

Method form:

```java
class Counter {
    private int count;

    public synchronized void increment() {
        count++;
    }
}
```

An instance synchronized method locks `this`.

Static method form:

```java
class CounterRegistry {
    public static synchronized void reload() {
        // locks CounterRegistry.class
    }
}
```

A static synchronized method locks the `Class` object.

Block form:

```java
class Counter {
    private final Object lock = new Object();
    private int count;

    public void increment() {
        synchronized (lock) {
            count++;
        }
    }
}
```

Prefer block form for most production code. It lets you choose a private lock object and keep the locked section as small as possible.

## Why pass an object?

`synchronized` needs an object because the object is the coordination point.

```java
synchronized (lock) {
    // protected code
}
```

This does not mean `lock` is the data being protected. It means `lock` is the monitor that threads agree to use before touching the protected data.

Think of the lock object as a key:

```text
same key    -> threads coordinate
different key -> threads do not coordinate
```

This works:

```java
class Counter {
    private final Object lock = new Object();
    private int count;

    void increment() {
        synchronized (lock) {
            count++;
        }
    }

    int current() {
        synchronized (lock) {
            return count;
        }
    }
}
```

Both methods use the same `lock`, so the read and write participate in the same protocol.

This is broken:

```java
class Counter {
    private final Object writeLock = new Object();
    private final Object readLock = new Object();
    private int count;

    void increment() {
        synchronized (writeLock) {
            count++;
        }
    }

    int current() {
        synchronized (readLock) {
            return count;
        }
    }
}
```

The code is synchronized, but not synchronized together. The read and write use different monitors, so there is no mutual exclusion or visibility guarantee between them.

Use this rule:

| Protected state | Good lock choice |
|---|---|
| Instance fields | `private final Object lock = new Object()` |
| Static fields | `private static final Object LOCK = new Object()` or `ClassName.class` |
| One shared collection/resource | Dedicated private final lock for that resource |
| Multiple fields that form one invariant | Same lock for all fields |

Avoid public or shared lock objects unless that is the deliberate API contract.

## Monitor ownership

At runtime:

```text
Thread A enters synchronized(lock)
  -> A owns lock's monitor
Thread B tries synchronized(lock)
  -> B becomes BLOCKED
Thread A exits synchronized(lock)
  -> monitor is released
Thread B can compete for the monitor again
```

Only code synchronizing on the same lock object coordinates with each other. Two different lock objects mean two different monitors and no mutual exclusion between them.

## Why `synchronized` fixes `count++`

Broken:

```java
private int count;

void increment() {
    count++;
}
```

`count++` is read, increment, write. Two threads can interleave and lose an update.

Fixed:

```java
private final Object lock = new Object();
private int count;

void increment() {
    synchronized (lock) {
        count++;
    }
}

int current() {
    synchronized (lock) {
        return count;
    }
}
```

Both read and write use the same lock. That matters. A synchronized writer and an unsynchronized reader are not a complete thread-safety protocol.

## Visibility guarantee

`synchronized` is not only about blocking other threads. It is also a JMM visibility boundary.

```text
Thread A writes shared state inside synchronized(lock)
Thread A exits synchronized(lock)
Thread B later enters synchronized(lock)
Thread B sees Thread A's writes
```

The JMM rule is: unlock of a monitor happens-before a later lock of the same monitor.

This is why all access to protected state should use the same lock. If one method reads without the lock, it may see stale state.

## Reentrancy

Java monitor locks are reentrant. If a thread already owns a monitor, it can acquire the same monitor again.

```java
public synchronized void outer() {
    inner();
}

public synchronized void inner() {
    // same thread re-enters this monitor
}
```

The JVM tracks a hold count. The monitor is released only when the thread exits the synchronized region as many times as it entered.

Without reentrancy, `outer()` calling `inner()` would deadlock itself.

## `synchronized` and `wait()`

`wait()`, `notify()`, and `notifyAll()` operate on the same monitor concept.

You must own `lock`'s monitor before calling `lock.wait()`, `lock.notify()`, or `lock.notifyAll()`. Otherwise Java throws `IllegalMonitorStateException`.

`wait()` releases the monitor while waiting and reacquires it before returning. The dedicated `wait, notify, notifyAll` page covers the full condition-loop pattern.

## Lock scope

Keep the critical section focused on shared state.

Bad:

```java
synchronized (lock) {
    validate(input);
    String response = httpClient.call(input); // slow IO while holding lock
    state.put(input.id(), response);
}
```

Better:

```java
validate(input);
String response = httpClient.call(input);

synchronized (lock) {
    state.put(input.id(), response);
}
```

Holding locks across IO, sleeps, remote calls, or long CPU work increases contention and can create deadlock risk if callbacks or downstream code call back into locked code.

## Lock object pitfalls

### Locking on a local object

```java
void increment() {
    Object lock = new Object();
    synchronized (lock) {
        count++;
    }
}
```

Every call creates a new lock. Threads do not coordinate at all.

### Locking on method arguments

```java
void process(String id) {
    synchronized (id) {
        update(id);
    }
}
```

Callers control the lock object. Equal IDs are not necessarily the same object, and shared/interned strings can accidentally coordinate unrelated code.

### Locking on string literals

```java
synchronized ("LOCK") {
    update();
}
```

String literals are interned. Another unrelated class using the same literal can share the same monitor.

### Locking on boxed primitives

```java
Integer lock = 1;
synchronized (lock) {
    update();
}
```

Boxed values may be cached and shared. Use a dedicated private final lock object.

### Exposing `this`

```java
public synchronized void update() {
    // locks this
}
```

This is not always wrong, but external code can also do `synchronized (yourObject)`, causing interference. A private lock avoids that.

## `synchronized` vs `volatile`

| Question | `synchronized` | `volatile` |
|---|---|---|
| Mutual exclusion? | Yes | No |
| Visibility? | Yes | Yes |
| Compound operation safety? | Yes, if all steps are inside the block | No |
| Blocking? | Yes | No |
| Best for | Invariants, read-modify-write, check-then-act | Single-variable signals |

If the operation is `counter++`, use `synchronized` or an atomic class. If the operation is "one thread sets stop flag, others read it", `volatile` may be enough.

## Quick recall

**Q. What does `synchronized` acquire?**
A. The intrinsic monitor lock of the object: `this`, `SomeClass.class`, or the explicit block lock.

**Q. Why do we pass an object to `synchronized(lock)`?**
A. That object is the monitor threads coordinate on. Threads block each other only when they use the same monitor object.

**Q. What should you usually pass to `synchronized(...)`?**
A. A private final lock object for instance state, or a private static final lock for static state.

**Q. What two guarantees does `synchronized` provide?**
A. Mutual exclusion and visibility through unlock-to-lock happens-before on the same monitor.

**Q. Why must reads and writes use the same lock?**
A. The visibility guarantee applies only through the same monitor. A synchronized writer plus plain reader is incomplete.

**Q. Why is Java's intrinsic lock reentrant?**
A. So a thread that already owns a monitor can enter another synchronized method/block using the same monitor without self-deadlock.

**Q. Why prefer a private final lock object?**
A. It prevents external or unrelated code from acquiring the same monitor and interfering.

**Q. Why avoid locking around IO?**
A. It holds the monitor while slow external work runs, increasing contention and deadlock risk.

**Q. What happens if you call `wait()` without owning the monitor?**
A. Java throws `IllegalMonitorStateException`.
