---
order: 20
search: false
---

# JMM Solutions

## Solution: visibility-flag - Visibility Flag

Copy-paste runnable version:

```java
import java.util.concurrent.atomic.AtomicInteger;

public class JmmVisibilityFlagSolution {
    public static void main(String[] args) throws InterruptedException {
        VolatileWorker worker = new VolatileWorker();

        Thread thread = new Thread(worker, "worker");
        thread.start();

        Thread.sleep(100);
        worker.stop();
        thread.join();

        System.out.println("worker stopped");
        atomicCounterDemo();
    }

    static final class VolatileWorker implements Runnable {
        private volatile boolean running = true;

        void stop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                // repeated work
            }
        }
    }

    static void atomicCounterDemo() throws InterruptedException {
        AtomicInteger count = new AtomicInteger();

        Runnable increment = () -> {
            for (int i = 0; i < 100_000; i++) {
                count.incrementAndGet();
            }
        };

        Thread first = new Thread(increment);
        Thread second = new Thread(increment);

        first.start();
        second.start();
        first.join();
        second.join();

        System.out.println(count.get()); // always 200000
    }

    // Unsafe shape for the first part of the exercise:
    // static final class PlainWorker implements Runnable {
    //     private boolean running = true; // unsafe cross-thread stop flag
    //
    //     void stop() {
    //         running = false;
    //     }
    //
    //     @Override
    //     public void run() {
    //         while (running) {
    //             // may keep seeing stale true
    //         }
    //     }
    // }

    // Unsafe shape for the counter check:
    // static final class VolatileCounter {
    //     private volatile int count;
    //
    //     void increment() {
    //         count++; // unsafe: read, add, write
    //     }
    // }
}
```

Answer:

- Plain `boolean running` is unsafe because the thread calling `stop()` writes one value, but the worker thread has no visibility guarantee that it will see that write.
- `volatile boolean running` fixes this flag because every read of `running` sees the latest volatile write or a newer one.
- `volatile` is enough here because the operation is just "read the latest flag value."
- `volatile` is not enough for `count++` because increment is three steps:

```text
read count
add one
write count
```

Two threads can both read the same old value and overwrite each other. For a counter, use `AtomicInteger.incrementAndGet()` or guard the increment and read with the same `synchronized` lock.

To run locally:

```bash
javac JmmVisibilityFlagSolution.java
java JmmVisibilityFlagSolution
```

Expected output shape:

```text
worker stopped
200000
```
