---
order: 20
search: false
---

# Thread Lifecycle Solutions

## Solution: start-vs-run - start() vs run()

`run()` is just a normal method call. It executes on the caller's current thread.

```java
Thread thread = new Thread(() ->
        System.out.println(Thread.currentThread().getName()), "worker");

thread.run();   // prints main/current caller thread
thread.start(); // starts the worker thread; direct run() did not consume the one allowed start
```

Use one version at a time:

```java
Thread thread = new Thread(() ->
        System.out.println(Thread.currentThread().getName()), "worker");

thread.start(); // JVM creates a new thread and invokes run() there
```

`start()` asks the JVM to create a new execution path and later invoke `run()` on that new thread.

Important details:

- `thread.run()` does not start a thread.
- `thread.start()` can be called only once for a `Thread` object.
- Calling `start()` twice throws `IllegalThreadStateException`.
- If you want to run the same task again, create a new `Thread` object or use an `ExecutorService`.

Interview answer:

> `run()` is an ordinary method call. `start()` transitions the thread from `NEW` to runnable execution and causes the JVM to call `run()` on a separate thread.

## Solution: runnable-task - Runnable Task

Copy-paste runnable version:

```java
public class RunnableTaskSolution {
    public static void main(String[] args) throws InterruptedException {
        Runnable task = new EmailTask();

        Thread worker = new Thread(task, "email-worker");
        worker.start();
        worker.join();

        System.out.println("main continues on " + Thread.currentThread().getName());
    }

    static final class EmailTask implements Runnable {
        @Override
        public void run() {
            System.out.println("send email on " + Thread.currentThread().getName());
        }
    }
}
```

Output shape:

```text
send email on email-worker
main continues on main
```

`Runnable` is the unit of work. `Thread` is the execution mechanism. Passing a `Runnable` to `new Thread(...)` keeps those two responsibilities separate.

Directly calling `task.run()` would execute on the current thread. Calling `worker.start()` creates the new execution path and then the JVM invokes `run()` on that new thread.

Use `implements Runnable` when your class represents a task. Avoid `extends Thread` unless you are intentionally customizing thread behavior, which is rare in normal backend code.

## Solution: join-not-sleep - join, Not Sleep

Use `join()` when one thread must wait for another thread to finish:

```java
int[] shared = new int[1];

Thread worker = new Thread(() -> shared[0] = 10);
worker.start();
worker.join();

System.out.println(shared[0]);
```

The caller of `join()` waits. After `join()` returns, actions completed by the joined thread are visible to the joining thread.

`sleep()` is not a correctness tool:

```java
worker.start();
Thread.sleep(100);
System.out.println(shared[0]); // maybe 10, maybe still 0
```

The worker may take longer than the sleep duration because of scheduling, CPU load, GC, blocking work, or slow hardware. Increasing the sleep only makes the bug less likely; it does not make the program correct.

Use this rule:

- `sleep()` means "pause this thread for at least roughly this long."
- `join()` means "wait until that specific thread has terminated."
- For real application code, prefer `ExecutorService`, `Future`, `CompletableFuture`, `CountDownLatch`, or structured concurrency-style APIs where appropriate.

## Solution: thread-state-observation - Observe Thread States

Thread states are easiest to observe with a small demo, but they are still snapshots:

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

Expected states:

- before `start()`: `NEW`
- during `sleep(500)`: usually `TIMED_WAITING`
- after `join()` returns: `TERMINATED`

The word "usually" matters. `getState()` is diagnostic, not a synchronization mechanism. The scheduler can move a thread between states before you print.

Examples:

- A thread blocked entering `synchronized` is `BLOCKED`.
- A thread in `Object.wait()` without timeout is `WAITING`.
- A thread in `Thread.sleep(...)`, timed `wait(...)`, or timed `join(...)` is `TIMED_WAITING`.
- A thread that is eligible to run may appear as `RUNNABLE`, even if the OS is not running it at that exact instant.

Interview answer:

> Thread states help explain lifecycle and blocking behavior, but application correctness should use coordination APIs such as `join`, locks, latches, futures, or queues, not repeated `getState()` polling.
