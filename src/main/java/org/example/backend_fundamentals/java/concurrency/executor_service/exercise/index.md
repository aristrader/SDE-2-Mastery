---
order: 10
search: false
---

# ExecutorService Practice

## Exercise: fixed-thread-pool - Fixed Thread Pool

### Goal
Run tasks through an executor instead of creating threads manually.

### Task
Create a fixed thread pool of size 3.

Submit 10 tasks that print the task id and thread name. Shut down the executor cleanly.

### Checks
- No manual `new Thread(...).start()` per task.
- `shutdown()` is called.
- Explain why pools avoid unbounded thread creation.
