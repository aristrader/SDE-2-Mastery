---
order: 20
search: false
---

# wait/notify Solutions

## Solution: single-slot-buffer - Single Slot Buffer

This buffer has exactly one slot. The protected condition is:

```text
available == false means producers may put
available == true means consumers may take
```

```java
final class SingleSlotBuffer<T> {
    private T value;
    private boolean available;

    synchronized void put(T newValue) throws InterruptedException {
        while (available) {
            wait();
        }
        value = newValue;
        available = true;
        notifyAll();
    }

    synchronized T take() throws InterruptedException {
        while (!available) {
            wait();
        }
        T result = value;
        value = null;
        available = false;
        notifyAll();
        return result;
    }
}
```

Why this works:

- `put()` waits while the slot is already full.
- `take()` waits while the slot is empty.
- `wait()` releases the monitor, so another thread can enter and change the condition.
- `notifyAll()` wakes threads after the condition changes.
- The `while` loop rechecks the condition after wake-up.

The `while` is not optional. A wake-up only means "something may have changed." Another thread may have consumed the value before this thread reacquired the monitor, or the wake-up may be spurious.

Use `notifyAll()` for this exercise because producers and consumers wait on the same monitor for different conditions. `notify()` can wake the wrong side.

## Solution: bounded-buffer - Bounded Buffer

The bounded buffer is the same idea as the single-slot buffer, but the condition is based on queue size:

```text
queue full  -> producers wait
queue empty -> consumers wait
```

```java
final class BoundedBuffer<T> {
    private final Queue<T> queue = new ArrayDeque<>();
    private final int capacity;

    BoundedBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity");
        }
        this.capacity = capacity;
    }

    synchronized void put(T value) throws InterruptedException {
        while (queue.size() == capacity) {
            wait();
        }
        queue.add(value);
        notifyAll();
    }

    synchronized T take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        T value = queue.remove();
        notifyAll();
        return value;
    }

    synchronized int size() {
        return queue.size();
    }
}
```

`ArrayDeque` is safe here because all access is guarded by the same monitor. No thread touches `queue` outside a synchronized method.

Why `notifyAll()` appears in both methods:

- after `put`, a waiting consumer may now be able to take
- after `take`, a waiting producer may now be able to put

This is still a teaching implementation. In production Java, prefer `ArrayBlockingQueue`, `LinkedBlockingQueue`, or another `BlockingQueue` implementation unless the interview specifically asks you to write wait/notify code.

## Solution: producer-consumer-shutdown - Producer Consumer Shutdown

The shutdown rule is: consumers must not stop until all real work has been produced and consumed.

After all producers finish, insert one poison pill per consumer:

```java
producer1.join();
producer2.join();

buffer.put(POISON);
buffer.put(POISON);
```

One poison pill can stop only one consumer. Inserting pills before producers finish can make consumers exit while real work remains.

A fuller shape:

```java
for (Thread producer : producers) {
    producer.join();
}

for (int i = 0; i < consumerCount; i++) {
    buffer.put(POISON);
}
```

Each consumer exits when it takes a poison pill:

```java
while (true) {
    Task task = buffer.take();
    if (task == POISON) {
        return;
    }
    process(task);
}
```

Common traps:

- One poison pill is not enough for multiple consumers.
- Poison pills must be inserted after producers finish.
- The poison pill must be distinguishable from real work.
- If consumers can be interrupted, define whether interruption means shutdown or cancellation.

## Solution: interruption-aware-buffer - Interruption Aware Buffer

Let `wait()` propagate interruption. Do not catch interruption and continue waiting silently.

```java
synchronized T take() throws InterruptedException {
    while (queue.isEmpty()) {
        wait();
    }
    T value = queue.remove();
    notifyAll();
    return value;
}
```

The caller is the right layer to decide whether interruption means retry, shutdown, or user cancellation.

Bad version:

```java
synchronized T take() {
    while (queue.isEmpty()) {
        try {
            wait();
        } catch (InterruptedException ignored) {
            // broken: caller cancellation was lost
        }
    }
    return queue.remove();
}
```

That code can make shutdown hang because a thread was asked to stop but kept waiting anyway.

Two acceptable patterns:

```java
// Best when the method can expose interruption
synchronized T take() throws InterruptedException {
    while (queue.isEmpty()) {
        wait();
    }
    return queue.remove();
}
```

```java
// Use only when the API cannot throw InterruptedException
catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    throw new IllegalStateException("interrupted while waiting", e);
}
```

Interview answer:

> Interruption is a cancellation signal. Either propagate `InterruptedException`, or restore the interrupt flag before translating it.

## Solution: alternate-odd-even - Alternate Odd And Even

This exercise is a turn-taking problem. The shared state is `number`; the condition is whose turn it is.

```java
final class OddEvenPrinter {
    private final Object lock = new Object();
    private int number = 1;
    private final int limit;

    OddEvenPrinter(int limit) {
        this.limit = limit;
    }

    void printOdd() {
        printWhen(true);
    }

    void printEven() {
        printWhen(false);
    }

    private void printWhen(boolean odd) {
        while (true) {
            synchronized (lock) {
                while (number <= limit && (number % 2 == 1) != odd) {
                    waitForTurn();
                }
                if (number > limit) {
                    lock.notifyAll();
                    return;
                }
                System.out.println(number++);
                lock.notifyAll();
            }
        }
    }

    private void waitForTurn() {
        try {
            lock.wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
```

The condition loop is the important part:

```java
while (number <= limit && (number % 2 == 1) != odd) {
    lock.wait();
}
```

It says:

- if this thread is the odd printer, wait while the current number is even
- if this thread is the even printer, wait while the current number is odd
- after waking, check again before printing

The `number > limit` branch also calls `notifyAll()` so the other thread can wake up and exit instead of waiting forever at the end.

Common mistakes:

- using `if` instead of `while`
- forgetting to notify after incrementing
- not notifying when the limit is reached
- holding separate locks for odd and even threads

In modern application code, this is mainly an interview exercise. For real pipelines, use queues, executors, semaphores, latches, or other higher-level coordination utilities.
