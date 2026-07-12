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

The pool reuses a bounded number of threads instead of creating one thread per task.

## Solution: callable-future - Callable And Future

```java
Future<Integer> future = executor.submit(() -> {
    int sum = 0;
    for (int i = 1; i <= 100; i++) {
        sum += i;
    }
    return sum;
});

try {
    System.out.println(future.get());
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
} catch (ExecutionException e) {
    throw new IllegalStateException(e.getCause());
}
```

`get()` blocks the caller until completion. Task failure is observed as `ExecutionException`.

## Solution: submit-before-waiting - Submit Before Waiting

```java
Future<User> userFuture = executor.submit(this::fetchUser);
Future<List<Order>> orderFuture = executor.submit(this::fetchOrders);

User user = userFuture.get();
List<Order> orders = orderFuture.get();
```

Both tasks are submitted before either result is needed, so they can overlap if the pool has enough workers.

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
```

The pool grows beyond core only after the queue fills. `CallerRunsPolicy` makes the submitting thread run rejected work, slowing submission.

## Solution: hidden-submit-failure - Hidden submit Failure

```java
Future<?> future = executor.submit(() -> {
    throw new IllegalStateException("failed");
});

future.get(); // throws ExecutionException
```

`submit()` stores the failure in the returned future. If nobody observes it, the failure can be operationally invisible.

## Solution: pool-starvation-deadlock - Pool Starvation Deadlock

Both pool workers run parent tasks. Each parent submits a child task to the same pool and blocks on `child.get()`. The child tasks are queued, but no worker is free to run them.

Fix by avoiding blocking inside pool tasks, composing async work, or using a separate executor for child work.

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

`cancel(true)` requests interruption. The task must cooperate by checking interruption or reacting to interruptible blocking calls.

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

Executor threads keep resources alive. Application code should have clear ownership and shutdown.

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

Named threads make logs, metrics, and thread dumps diagnosable.
