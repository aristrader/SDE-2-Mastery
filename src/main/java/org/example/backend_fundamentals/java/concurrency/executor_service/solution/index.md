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
