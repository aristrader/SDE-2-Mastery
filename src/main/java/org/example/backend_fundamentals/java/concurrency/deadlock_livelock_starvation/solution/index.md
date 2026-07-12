---
order: 20
search: false
---

# Deadlock, Livelock, Starvation Solutions

## Solution: create-deadlock - Create A Deadlock

```java
Object lock1 = new Object();
Object lock2 = new Object();

new Thread(() -> {
    synchronized (lock1) {
        sleep(100);
        synchronized (lock2) {
            System.out.println("A done");
        }
    }
}).start();

new Thread(() -> {
    synchronized (lock2) {
        sleep(100);
        synchronized (lock1) {
            System.out.println("B done");
        }
    }
}).start();
```

The sleep widens the timing window. The cause is opposite lock acquisition order.

## Solution: fix-deadlock-ordering - Fix Deadlock With Ordering

```java
Runnable task = () -> {
    synchronized (lock1) {
        synchronized (lock2) {
            System.out.println(Thread.currentThread().getName());
        }
    }
};
```

No cycle can form if every path acquires `lock1` before `lock2`.

## Solution: account-transfer-ordering - Account Transfer Lock Ordering

```java
boolean transfer(Account source, Account destination, long amount) {
    if (source == destination) {
        throw new IllegalArgumentException("self-transfer");
    }
    if (amount <= 0) {
        throw new IllegalArgumentException("amount");
    }

    Account first = source.id() < destination.id() ? source : destination;
    Account second = source.id() < destination.id() ? destination : source;

    synchronized (first) {
        synchronized (second) {
            if (source.balance() < amount) {
                return false;
            }
            source.debit(amount);
            destination.credit(amount);
            return true;
        }
    }
}
```

The balance check and both writes are one invariant. Separate atomics cannot make the transfer atomic.

## Solution: pool-starvation - Thread Pool Starvation

Both workers block waiting for child tasks queued to the same pool. The queued tasks cannot run because no worker is free.

Fixes include composing without blocking, using a separate executor for nested work, or designing the pool and queue so the dependency cannot exhaust all workers.
