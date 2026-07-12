---
order: 20
search: false
---

# Thread Lifecycle Solutions

## Solution: start-vs-run - start() vs run()

```java
Thread thread = new Thread(() ->
        System.out.println(Thread.currentThread().getName()), "worker");

thread.run();   // prints main/current caller thread
thread.start(); // starts the worker thread; direct run() did not consume the one allowed start
```

Use one version at a time:

```java
thread.start();
```

`run()` is a normal method call on the current thread. `start()` asks the JVM to create a new execution path and later invoke `run()` on it. Calling `start()` twice throws `IllegalThreadStateException`.

## Solution: join-not-sleep - join, Not Sleep

```java
int[] shared = new int[1];

Thread worker = new Thread(() -> shared[0] = 10);
worker.start();
worker.join();

System.out.println(shared[0]);
```

The caller of `join()` waits. After `join()` returns, actions completed by the joined thread are visible to the joining thread.

## Solution: thread-state-observation - Observe Thread States

```java
Object lock = new Object();

Thread sleeper = new Thread(() -> {
    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

System.out.println(sleeper.getState()); // NEW
sleeper.start();
Thread.sleep(50);
System.out.println(sleeper.getState()); // usually TIMED_WAITING
sleeper.join();
System.out.println(sleeper.getState()); // TERMINATED
```

Thread states are snapshots. The scheduler can move a thread between states before you print.
