---
order: 20
search: false
---

# synchronized Solutions

## Solution: synchronized-counter - Synchronized Counter

The whole counter state is protected by the object's monitor:

```java
final class Counter {
    private int value;

    synchronized void increment() {
        value++;
    }

    synchronized int value() {
        return value;
    }
}
```

For instance synchronized methods, the monitor is `this`. Only one thread can execute a synchronized instance method on the same `Counter` object at a time.

This fixes two separate problems:

- Atomicity: `value++` is read-add-write, but the monitor prevents another thread from entering the increment while it is in progress.
- Visibility: a synchronized read that uses the same monitor sees writes made by previous synchronized blocks/methods on that monitor.

The reader must also be synchronized. If `increment()` is synchronized but `value()` is not, the reader does not participate in the same visibility protocol.

## Solution: wrong-lock-object - Wrong Lock Object

This is wrong:

```java
void increment() {
    synchronized (new Object()) {
        count++;
    }
}
```

Every call creates a different lock. Different threads are not competing for the same monitor, so there is no mutual exclusion.

Use one shared lock object:

```java
private final Object lock = new Object();

void increment() {
    synchronized (lock) {
        count++;
    }
}
```

The rule is simple: all code that protects the same state must synchronize on the same monitor. The lock should be a field, not a local object created inside the method.

Using a private lock object is often better than synchronizing on a public object because outside code cannot accidentally or deliberately hold your lock.

## Solution: partial-synchronization - Partial Synchronization

The check and the update must be one critical section:

```java
boolean withdraw(int amount) {
    synchronized (this) {
        if (balance < amount) {
            return false;
        }
        balance -= amount;
        return true;
    }
}
```

The protected invariant is:

```text
balance must never go below zero
```

This version is unsafe:

```java
boolean withdraw(int amount) {
    if (balance < amount) {
        return false;
    }

    synchronized (this) {
        balance -= amount;
        return true;
    }
}
```

Two threads can both observe enough balance before either debit happens. Synchronizing only the write is too late; the decision was already made using unprotected state.

In interviews, phrase it as: "The critical section must cover the full invariant, not just the assignment."

## Solution: lock-scope - Lock Scope

Keep slow unrelated work outside the lock. Keep shared-state mutation inside the lock.

```java
void processOrder(Order order) {
    validate(order);
    Payment payment = callRemotePaymentService(order);

    synchronized (this) {
        updateBalance(order, payment);
    }

    sendEmail(order);
}
```

Only the shared-state update is locked. Slow remote calls do not block unrelated threads from entering the monitor.

This is not just a performance preference. Holding a lock during remote calls can create avoidable contention, deadlock risk, and request pileups. But do not shrink the lock so far that the protected invariant is split. The right lock scope is the smallest block that still protects the whole shared-state decision.

Good answer:

```text
Validate local input outside the lock.
Call external systems outside the lock.
Lock around the shared mutable state that must remain consistent.
Release the lock before notifications or slow side effects.
```

## Solution: synchronized-block-counter - Synchronized Block Counter

Using a block is equivalent to a synchronized method only if every access uses the same lock:

```java
final class Counter {
    private final Object lock = new Object();
    private int value;

    void increment() {
        synchronized (lock) {
            value++;
        }
    }

    int value() {
        synchronized (lock) {
            return value;
        }
    }
}
```

The same monitor must protect both mutation and observation. The private `lock` object is the monitor, not `this`.

This gives you slightly tighter control than synchronized methods:

- callers cannot synchronize on your private lock
- you can lock only the lines that touch shared state
- you can use different private locks for independent state, if there is a real need

Do not use different locks for `increment()` and `value()`. Different locks do not create happens-before visibility for the same state.

## Solution: read-write-same-lock - Reader And Writer Same Lock

Both methods must use the same monitor:

```java
final class Counter {
    private int value;

    synchronized void increment() {
        value++;
    }

    synchronized int value() {
        return value;
    }
}
```

Synchronizing only the writer does not give the reader the same visibility guarantee. Different locks also do not coordinate.

Broken version:

```java
final class Counter {
    private final Object writeLock = new Object();
    private int value;

    void increment() {
        synchronized (writeLock) {
            value++;
        }
    }

    int value() {
        return value;
    }
}
```

The write is protected, but the read is just a plain unsynchronized read. The reader may observe stale data, and it is not part of the same monitor protocol.

Interview answer:

> A lock protects a variable only when every read and every write that matters uses the same lock.
