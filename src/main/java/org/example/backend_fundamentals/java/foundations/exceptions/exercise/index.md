---
order: 10
search: false
---

# Exception Handling Practice

## Exercise: checked-vs-unchecked - Extend the hierarchy

### Goal
Understand checked vs unchecked exception hierarchy.

### Task
1. Create a checked exception `InsufficientFundsException(String message, double shortfall)` that stores `shortfall` as a field with a getter.
2. Create an unchecked exception `InvalidAccountStateException(String message)`.
3. Write a method `withdraw(double balance, double amount)` that:
   - throws `InvalidAccountStateException` if `amount <= 0`
   - throws `InsufficientFundsException` if `amount > balance`
   - prints `"Withdrew X"` otherwise
4. In `main`, call `withdraw` three times — one success, one invalid amount, one insufficient funds — and handle accordingly.

Answer in a comment before writing: **Why does `InsufficientFundsException` need to be checked? Why does `InvalidAccountStateException` not?**

## Exercise: exception-translation - Exception translation

### Goal
Translate exceptions properly without losing the original cause.

### Task
Write a method `loadUserConfig(String filename)` that:
- internally calls a helper `readFile(String filename)` which throws a simulated checked `java.io.IOException`
- `loadUserConfig` must NOT declare `throws IOException` on its signature
- translates the low-level `IOException` into a domain `ConfigLoadException` (unchecked), **preserving the original cause**

In `main`, call `loadUserConfig("missing.cfg")` and print `ex.getCause().getClass().getSimpleName()` to confirm the cause is preserved.

## Exercise: try-with-resources-warmup - Warm-up (close ordering)

### Goal
Understand the closing order of resources in `try-with-resources`.

### Task
Create a class `Resource` that implements `AutoCloseable`:
- constructor prints `"Opening Resource [name]"`
- `use()` prints `"Using Resource [name]"`
- `close()` prints `"Closing Resource [name]"`

Open two resources in a single `try-with-resources` block (`r1` and `r2`). Call `use()` on both.

Note in a comment: **which closes first, and why?**

## Exercise: try-with-resources-exception - Close with exception

### Goal
Observe how suppressed exceptions work in try-with-resources.

### Task
Extend `Resource.close()` to throw `RuntimeException("Close failed: [name]")` for `r1` only. In `main`:
- call `r1.use()`, then throw `RuntimeException("Body failed")` from the try body
- catch the exception, print `ex.getMessage()`, then print `ex.getSuppressed()[0].getMessage()`

Predict what you'll see before running. Note in a comment: **why is the body exception primary and the close exception suppressed?**
