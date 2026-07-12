---
order: 20
search: false
---

# synchronized Solutions

## Solution: synchronized-counter - Synchronized Counter

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

For instance synchronized methods, the monitor is `this`. Only one thread can execute a synchronized instance method on the same object at a time.

## Solution: wrong-lock-object - Wrong Lock Object

```java
private final Object lock = new Object();

void increment() {
    synchronized (lock) {
        count++;
    }
}
```

`new Object()` inside the method creates a fresh monitor per call, so threads do not exclude each other.

## Solution: partial-synchronization - Partial Synchronization

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

The balance check and debit are one invariant. Splitting them lets two threads both pass the check.

## Solution: lock-scope - Lock Scope

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

## Solution: synchronized-block-counter - Synchronized Block Counter

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

The same monitor must protect both mutation and observation.

## Solution: read-write-same-lock - Reader And Writer Same Lock

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
