# Exception Handling — Exercises

---

## Topic 1: Checked vs unchecked

**Exercise 1a — Extend the hierarchy**

1. Create a checked exception `InsufficientFundsException(String message, double shortfall)` that stores `shortfall` as a field with a getter.
2. Create an unchecked exception `InvalidAccountStateException(String message)`.
3. Write a method `withdraw(double balance, double amount)` that:
   - throws `InvalidAccountStateException` if `amount <= 0`
   - throws `InsufficientFundsException` if `amount > balance`
   - prints `"Withdrew X"` otherwise
4. In `main`, call `withdraw` three times — one success, one invalid amount, one insufficient funds — and handle accordingly.

Answer in a comment before writing: **Why does `InsufficientFundsException` need to be checked? Why does `InvalidAccountStateException` not?**

---

## Topic 2: Exception translation

**Exercise 2a**

Write a method `loadUserConfig(String filename)` that:
- internally calls a helper `readFile(String filename)` which throws a simulated checked `java.io.IOException`
- `loadUserConfig` must NOT declare `throws IOException` on its signature
- translates the low-level `IOException` into a domain `ConfigLoadException` (unchecked), **preserving the original cause**

In `main`, call `loadUserConfig("missing.cfg")` and print `ex.getCause().getClass().getSimpleName()` to confirm the cause is preserved.

---

## Topic 3: try-with-resources

**Exercise 3a — Warm-up (close ordering)**

Create a class `Resource` that implements `AutoCloseable`:
- constructor prints `"Opening Resource [name]"`
- `use()` prints `"Using Resource [name]"`
- `close()` prints `"Closing Resource [name]"`

Open two resources in a single `try-with-resources` block (`r1` and `r2`). Call `use()` on both.

Note in a comment: **which closes first, and why?**

**Exercise 3b — Close with exception**

Extend `Resource.close()` to throw `RuntimeException("Close failed: [name]")` for `r1` only. In `main`:
- call `r1.use()`, then throw `RuntimeException("Body failed")` from the try body
- catch the exception, print `ex.getMessage()`, then print `ex.getSuppressed()[0].getMessage()`

Predict what you'll see before running. Note in a comment: **why is the body exception primary and the close exception suppressed?**
