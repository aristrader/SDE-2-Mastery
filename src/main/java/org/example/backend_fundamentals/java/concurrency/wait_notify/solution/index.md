---
order: 20
search: false
---

# wait/notify Solutions

## Solution: single-slot-buffer - Single Slot Buffer

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

The condition loop protects against spurious wakeups and other threads consuming the condition first.

## Solution: bounded-buffer - Bounded Buffer

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

`ArrayDeque` is safe here because all access is guarded by the same monitor.

## Solution: producer-consumer-shutdown - Producer Consumer Shutdown

After all producers finish, insert one poison pill per consumer:

```java
producer1.join();
producer2.join();

buffer.put(POISON);
buffer.put(POISON);
```

One poison pill can stop only one consumer. Inserting pills before producers finish can make consumers exit while real work remains.

## Solution: interruption-aware-buffer - Interruption Aware Buffer

Let `wait()` propagate interruption:

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

## Solution: alternate-odd-even - Alternate Odd And Even

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

The condition loop is the important part. The exact printing task is just a way to force turn coordination.
