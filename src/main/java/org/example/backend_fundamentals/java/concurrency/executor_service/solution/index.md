---
order: 20
search: false
---

# ExecutorService Solutions

## Solution: fixed-thread-pool - Fixed Thread Pool

```java
ExecutorService executor = Executors.newFixedThreadPool(3);
try {
    for (int i = 0; i < 10; i++) {
        int taskId = i;
        executor.submit(() ->
            System.out.println(taskId + " " + Thread.currentThread().getName()));
    }
} finally {
    executor.shutdown();
}
```

A fixed pool keeps exactly 3 worker threads available for this executor. The 10 submitted tasks are placed into the executor's work queue, and at most 3 of them run at the same time.

That is the point of using an executor: the application controls concurrency through the pool size instead of creating an unbounded number of native threads. Creating one thread per task can exhaust memory, increase context switching, and make the system fail under load. With a pool, extra work waits in the queue until a worker becomes free.

`shutdown()` is important because executor threads are normally non-daemon threads. If the pool is forgotten, the JVM may keep running even after the main work is done. `shutdown()` means "do not accept new tasks, but finish already submitted tasks."

Common trap: `shutdown()` does not wait by itself. If the caller must know that all tasks finished, combine it with `awaitTermination()`.

## Solution: callable-future - Callable And Future

```java
ExecutorService executor = Executors.newSingleThreadExecutor();
try {
    Future<Integer> future = executor.submit(() -> {
        int sum = 0;
        for (int i = 1; i <= 100; i++) {
            sum += i;
        }
        return sum;
    });

    System.out.println(future.get());
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
} catch (ExecutionException e) {
    throw new IllegalStateException(e.getCause());
} finally {
    executor.shutdown();
}
```

`Callable<Integer>` is used instead of `Runnable` because the task returns a value. `submit()` returns a `Future<Integer>`, which represents a result that may not be ready yet.

The blocking point is `future.get()`. The background task may already be complete, in which case `get()` returns immediately. If not, the calling thread waits until the task completes, fails, or the waiting thread is interrupted.

If the task throws an exception, `get()` throws `ExecutionException`. The original task exception is available through `e.getCause()`. Do not catch `ExecutionException` and ignore it; that hides a real task failure.

If `get()` throws `InterruptedException`, restore the interrupt status with `Thread.currentThread().interrupt()`. The interrupt belongs to the calling thread, and higher-level code may rely on that flag to stop work.

## Solution: submit-before-waiting - Submit Before Waiting

```java
ExecutorService executor = Executors.newFixedThreadPool(2);

long serializedStart = System.nanoTime();
User user1 = executor.submit(this::fetchUser).get();
List<Order> orders1 = executor.submit(this::fetchOrders).get();
long serializedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - serializedStart);

long parallelStart = System.nanoTime();
Future<User> userFuture = executor.submit(this::fetchUser);
Future<List<Order>> orderFuture = executor.submit(this::fetchOrders);

User user = userFuture.get();
List<Order> orders = orderFuture.get();
long parallelMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - parallelStart);
```

In the first version, the code submits `fetchUser()` and immediately calls `get()`. That blocks the caller before `fetchOrders()` is even submitted. If each operation takes roughly one second, the total time is roughly two seconds because the operations are accidentally serialized.

In the second version, both tasks are submitted first. Then the caller waits for the results. With a pool of at least two workers, `fetchUser()` and `fetchOrders()` can run at the same time, so the total time is closer to the slower single operation.

The rule is: submit independent work before waiting for any one result. Calling `get()` too early often removes the concurrency you thought you had.

Common trap: this only helps when the tasks are actually independent and the executor has enough capacity. A single-thread executor would still run them one at a time.

## Solution: bounded-pool-backpressure - Bounded Pool Backpressure

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
        2,
        4,
        30,
        TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(5),
        new ThreadPoolExecutor.CallerRunsPolicy()
);

for (int i = 0; i < 20; i++) {
    int taskId = i;
    executor.execute(() -> {
        System.out.println("task " + taskId + " on " + Thread.currentThread().getName());
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    });
}

executor.shutdown();
```

With this configuration:

- `corePoolSize = 2`: the executor starts by using up to 2 worker threads.
- `maximumPoolSize = 4`: the executor can grow to 4 workers, but only under pressure.
- queue capacity `5`: after the core workers are busy, up to 5 tasks wait in the queue.
- `CallerRunsPolicy`: if the pool is at max size and the queue is full, the submitting thread runs the task.

The growth order matters. A common misunderstanding is that the pool grows from 2 to 4 as soon as more than 2 tasks arrive. It does not. The usual `ThreadPoolExecutor` flow is:

1. Use core threads first.
2. Queue extra work.
3. If the queue is full, grow up to max threads.
4. If max threads are also busy and the queue is full, apply the rejection policy.

`CallerRunsPolicy` is a simple backpressure mechanism. Instead of dropping work or throwing `RejectedExecutionException`, the producer thread must run the task itself. While the producer is busy running that task, it cannot keep submitting more tasks, so submission naturally slows down.

Common trap: an unbounded queue, such as the default queue behind some factory methods, may prevent the pool from ever growing beyond the core size.

## Solution: hidden-submit-failure - Hidden submit Failure

```java
ExecutorService executor = Executors.newSingleThreadExecutor();

executor.execute(() -> {
    throw new IllegalStateException("execute failed");
});

Future<?> future = executor.submit(() -> {
    throw new IllegalStateException("failed");
});

future.get(); // throws ExecutionException
```

`execute()` and `submit()` treat task failures differently.

With `execute()`, an unchecked exception escapes the task's `run()` method. The worker thread's uncaught exception handling can report it, commonly to logs or stderr depending on the thread and environment.

With `submit()`, the executor captures the exception and stores it inside the returned `Future`. The exception is rethrown to the caller only when someone calls `future.get()`, wrapped in `ExecutionException`.

That means this is dangerous:

```java
executor.submit(() -> {
    throw new IllegalStateException("lost failure");
});
```

The task failed, but if the returned `Future` is ignored, the caller may never notice. For fire-and-forget work, either log inside the task, use `execute()` with an uncaught exception strategy, collect and inspect futures, or use a higher-level mechanism that reports failures.

Common trap: `submit()` is not safer just because it does not visibly crash a worker. It can make failure less visible.

## Solution: pool-starvation-deadlock - Pool Starvation Deadlock

Both pool workers run parent tasks. Each parent submits a child task to the same pool and blocks on `child.get()`. The child tasks are queued, but no worker is free to run them.

The deadlock happens because the pool has only 2 threads:

1. `first` starts running `parent` on worker 1.
2. `second` starts running `parent` on worker 2.
3. Each parent submits a child task back to the same pool.
4. Each parent blocks on `child.get()`.
5. Both workers are blocked, so no worker is available to run either child.

One fix is to avoid nested blocking and compose the work instead:

```java
CompletableFuture<String> first =
        CompletableFuture.supplyAsync(this::fetchRemoteValue, pool)
                .thenApply(value -> "parent + " + value);
```

Another practical fix is to use separate executors when the parent and child work have different capacity needs:

```java
ExecutorService parentPool = Executors.newFixedThreadPool(2);
ExecutorService remotePool = Executors.newFixedThreadPool(10);

Callable<String> parent = () -> {
    Future<String> child = remotePool.submit(this::fetchRemoteValue);
    return "parent + " + child.get();
};
```

The deeper rule is: be suspicious when a task running inside a bounded executor submits more work to the same executor and waits for it. That pattern can starve the pool.

## Solution: cancellation-aware-task - Cancellation Aware Task

```java
Future<?> future = executor.submit(() -> {
    try {
        while (!Thread.currentThread().isInterrupted()) {
            doOneUnitOfWork();
            Thread.sleep(200);
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    } finally {
        cleanup();
    }
});

future.cancel(true);
```

`future.cancel(true)` does not forcibly kill the thread. It marks the future as cancelled and asks the executor to interrupt the worker thread if the task is already running.

The task must cooperate. In this example it cooperates in two ways:

- the loop checks `Thread.currentThread().isInterrupted()`
- `Thread.sleep()` is interruptible and throws `InterruptedException`

When `InterruptedException` is caught, the code restores the interrupt status. This is important because throwing `InterruptedException` clears the flag. Restoring it lets outer code and cleanup logic still observe that cancellation was requested.

Common trap: swallowing `InterruptedException` and continuing the loop turns cancellation into a no-op.

## Solution: graceful-shutdown - Graceful Shutdown Utility

```java
static void shutdownGracefully(ExecutorService executor) {
    executor.shutdown();
    try {
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
    }
}
```

This is the standard graceful shutdown sequence:

1. `shutdown()` stops accepting new tasks.
2. `awaitTermination(...)` gives already-submitted tasks time to finish.
3. `shutdownNow()` requests interruption for running tasks and returns tasks that never started.
4. If the shutdown thread is interrupted, call `shutdownNow()` and restore the interrupt status.

Executor ownership matters. The code that creates the pool should usually also define when it is shut down. Creating pools inside request handling or utility methods and forgetting them can leak threads, keep the JVM alive, and slowly degrade the process.

Common trap: `shutdownNow()` is not guaranteed to stop everything immediately. It interrupts workers, so tasks must still be written to respond to interruption.

## Solution: custom-thread-factory - Custom Thread Factory

```java
AtomicInteger sequence = new AtomicInteger();

ThreadFactory factory = task -> {
    Thread thread = new Thread(task);
    thread.setName("report-worker-" + sequence.incrementAndGet());
    return thread;
};

ExecutorService executor = Executors.newFixedThreadPool(2, factory);
```

`AtomicInteger` is used because the thread factory may be called by multiple threads. It gives each worker a unique sequence number without adding a separate lock.

Thread names matter because production debugging often starts from logs, metrics, or a thread dump. A name like `report-worker-2` tells you which subsystem owns the thread. A default name like `pool-7-thread-2` tells you much less, especially when the process has many executors.

Common trap: do not create a new `AtomicInteger` inside the lambda body. It must be shared by the factory so names keep increasing across worker creation.
