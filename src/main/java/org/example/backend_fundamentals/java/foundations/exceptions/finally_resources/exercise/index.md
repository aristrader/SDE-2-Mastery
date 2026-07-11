---
order: 10
search: false
---

# finally and Resources Practice

## Exercise: finally - finally

### Goal
Observe when `finally` runs.

### Task
Write a small method with `try` and `finally`.

Test normal execution, an exception, and a return statement.

## Exercise: return-in-finally - return in finally

### Goal
See why returning from `finally` is dangerous.

### Task
Experiment with:

```java
try {
    return 1;
} finally {
    return 2;
}
```

### Checks
- Explain why the answer is `2`.
- Explain what happens if the `try` block throws instead.

## Exercise: try-with-resources - try-with-resources

### Goal
Replace manual cleanup with structured cleanup.

### Task
Read a text file using `BufferedReader`.

Implement both:

- traditional `try-finally`
- `try-with-resources`

### Checks
- Compare the boilerplate.
- Explain reverse close order.
- Explain suppressed exceptions.

## Exercise: try-with-resources-warmup - Warm-up (close ordering)

### Goal
Understand close ordering in `try-with-resources`.

### Task
Create `Resource implements AutoCloseable`.

- constructor prints `Opening Resource [name]`
- `use()` prints `Using Resource [name]`
- `close()` prints `Closing Resource [name]`

Open `r1` and `r2` in one `try-with-resources` block and call `use()` on both.

### Checks
- Note which resource closes first.
- Explain why close order is reverse declaration order.

## Exercise: try-with-resources-exception - Close with exception

### Goal
Observe suppressed exceptions.

### Task
Change `Resource.close()` so `r1` throws `RuntimeException("Close failed: r1")`.

Inside the try body, call `r1.use()` and then throw `RuntimeException("Body failed")`.

Catch the exception and print:

- `ex.getMessage()`
- `ex.getSuppressed()[0].getMessage()`

### Checks
- Predict the output before running.
- Explain why the body exception is primary and close failure is suppressed.
