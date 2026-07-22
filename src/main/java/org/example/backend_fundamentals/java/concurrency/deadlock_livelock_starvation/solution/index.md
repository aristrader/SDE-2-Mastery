---
order: 20
search: false
---

# Deadlock, Livelock, Starvation Solutions

## Solution: create-deadlock - Create A Deadlock

A minimal deadlock needs two locks and two threads acquiring them in opposite order:

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

The sleep widens the timing window so the deadlock is easier to observe. The cause is not `sleep()` itself. The cause is the cycle:

```text
Thread A holds lock1 and waits for lock2
Thread B holds lock2 and waits for lock1
```

This is a deadlock because neither thread can make progress without the other releasing a lock, and neither can reach the release point.

Interview answer:

> Deadlock requires a cycle of waiting. Opposite lock acquisition order is a common way to create that cycle.

## Solution: fix-deadlock-ordering - Fix Deadlock With Ordering

Make every code path acquire locks in the same global order:

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

Why this works:

```text
Thread A may hold lock1 and wait for lock2.
Thread B cannot hold lock2 while waiting for lock1, because it must acquire lock1 first.
```

The cycle is removed. There can still be waiting, but not circular waiting.

For dynamic objects, define a stable ordering key such as account id. Do not order by the caller's source/destination direction because another transfer may use the reverse direction.

## Solution: account-transfer-ordering - Account Transfer Lock Ordering

The transfer must protect one invariant:

```text
debit source and credit destination happen together, or not at all
```

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

Why lock ordering matters:

```text
transfer(A, B) locks A then B
transfer(B, A) must also lock A then B
```

If each transfer locks `source` first, the two transfers can deadlock:

```text
Thread 1 holds A, wants B
Thread 2 holds B, wants A
```

Ordering by `id()` prevents that cycle. The actual debit still uses the original `source` and `destination`; only the lock acquisition order changes.

Also keep validation outside or before the critical update when possible. The balance check and both balance writes must stay inside the nested locks.

## Solution: pool-starvation - Thread Pool Starvation

Both workers block waiting for child tasks queued to the same pool. The queued tasks cannot run because no worker is free.

Fixes include composing without blocking, using a separate executor for nested work, or designing the pool and queue so the dependency cannot exhaust all workers.

Broken pattern:

```java
ExecutorService pool = Executors.newFixedThreadPool(2);

pool.submit(() -> {
    Future<String> child = pool.submit(() -> "child-1");
    return child.get();
});

pool.submit(() -> {
    Future<String> child = pool.submit(() -> "child-2");
    return child.get();
});
```

If both parent tasks occupy the two workers and both block on `get()`, the child tasks sit in the queue forever.

Better options:

```java
CompletableFuture<String> result =
        CompletableFuture.supplyAsync(this::loadUser, pool)
                .thenCompose(user ->
                        CompletableFuture.supplyAsync(() -> loadOrders(user), pool));
```

or use a separate executor for child work if the dependency is real:

```java
ExecutorService parentPool = Executors.newFixedThreadPool(2);
ExecutorService childPool = Executors.newFixedThreadPool(4);
```

Interview answer:

> Starvation is not always a lock problem. A fixed thread pool can starve itself when all workers block waiting for queued work that requires those same workers.
