---
order: 20
search: false
---

# JMM Solutions

## Solution: visibility-flag - Visibility Flag

The unsafe version usually looks like this:

```java
final class Worker implements Runnable {
    private boolean running = true;

    void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            doWork();
        }
    }

    private void doWork() {
        // some repeated work
    }
}
```

The bug is not that `stop()` fails to assign `false`. The bug is that the Java Memory Model does not give the worker thread a visibility guarantee for that write. One thread writes `running = false`; another thread reads `running`. Without a happens-before relationship between those actions, the reader is allowed to keep seeing an old value.

A correct flag version uses `volatile`:

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

`volatile` gives two guarantees that matter here:

- Visibility: when one thread writes `running = false`, another thread that later reads `running` must see that write or a newer write.
- Ordering around the volatile access: normal writes before a volatile write cannot be freely moved after it, and normal reads after a volatile read cannot be freely moved before it in a way that breaks the volatile visibility protocol.

So this is a good use of `volatile`: the shared state is a single flag, the writer only assigns a new value, and the reader only checks the latest value.

It is still not a general replacement for locking. This is still broken:

```java
final class Counter {
    private volatile int count;

    void increment() {
        count++;
    }

    int value() {
        return count;
    }
}
```

`count++` is not one operation. It is:

```text
read current count
add one
write new count
```

`volatile` makes each read/write visible, but it does not make the read-add-write sequence atomic. Two threads can read `10`, both compute `11`, and both write `11`. One increment is lost.

Use this rule in interviews:

- `volatile` is good for a latest-value signal such as `running`, `shutdownRequested`, or an immutable config reference.
- `volatile` is not enough when correctness depends on a compound action such as check-then-act, increment, or updating multiple fields together.
- For compound actions, use `synchronized`, `Lock`, or an atomic class whose operation matches the whole invariant.
