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

## Solution: static-holder-lazy-initialization - Static Holder Lazy Initialization

Runnable version:

```java
public class StaticHolderLazyInitializationSolution {
    public static void main(String[] args) {
        // Run one case at a time.
        case3();
    }

    static void case1() {
        System.out.println("main");
    }

    static void case2() {
        System.out.println("main start");
        Singleton.touchOuter();
        System.out.println("main end");
    }

    static void case3() {
        System.out.println("main start");
        Singleton.getInstance();
        System.out.println("main end");
    }

    static void case4() {
        System.out.println("main start");
        Singleton.getInstance();
        Singleton.getInstance();
        System.out.println("main end");
    }

    static void case5() {
        System.out.println("main start");
        Singleton.touchOuter();
        Singleton.getInstance();
        System.out.println("main end");
    }

    static final class Singleton {
        static {
            System.out.println("Singleton initialized");
        }

        private Singleton() {
            System.out.println("Singleton constructor");
        }

        private static final class Holder {
            static {
                System.out.println("Holder initialized");
            }

            static final Singleton INSTANCE = new Singleton();
        }

        static void touchOuter() {
            System.out.println("touchOuter called");
        }

        static Singleton getInstance() {
            return Holder.INSTANCE;
        }
    }
}
```

Expected outputs:

| Case | Output |
|---|---|
| Case 1 | `main` |
| Case 2 | `main start`, then `Singleton initialized`, then `touchOuter called`, then `main end` |
| Case 3 | `main start`, then `Singleton initialized`, then `Holder initialized`, then `Singleton constructor`, then `main end` |
| Case 4 | `main start`, then `Singleton initialized`, then `Holder initialized`, then `Singleton constructor`, then `main end` |
| Case 5 | `main start`, then `Singleton initialized`, then `touchOuter called`, then `Holder initialized`, then `Singleton constructor`, then `main end` |

Explanation:

- `Singleton` and `Holder` are different classes.
- Calling `Singleton.touchOuter()` initializes only the outer `Singleton` class.
- `Holder` initializes only when code first touches `Holder.INSTANCE`.
- During `Holder` initialization, `INSTANCE = new Singleton()` runs.
- Class initialization is done once by the JVM, with synchronization.
- After class initialization completes, all threads safely see `Holder.INSTANCE`.
- No `volatile` is needed because the JVM's class-initialization rule creates the safe-publication guarantee.

The pattern is lazy because the singleton object is not created until `getInstance()` accesses `Holder.INSTANCE`.
