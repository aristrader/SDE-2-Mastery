---
order: 60
---

# Exception Handling

---

## Exception hierarchy

```
Throwable
├── Error               — JVM-level failures (OutOfMemoryError, StackOverflowError). Never catch.
└── Exception
    ├── RuntimeException        — UNCHECKED (compiler doesn't enforce handling)
    │   ├── NullPointerException
    │   ├── IllegalArgumentException
    │   └── IllegalStateException
    └── (everything else extending Exception directly) — CHECKED
        ├── IOException
        └── your custom checked exceptions
```

---

## Checked vs unchecked

**Checked** — compiler forces you to handle or declare (`throws` on the method). Use when the caller can reasonably recover — file not found, network timeout, database down.

**Unchecked** (`RuntimeException` subclasses) — compiler doesn't require handling. Use for programming errors the caller can't meaningfully recover from — null where non-null required, invalid argument, illegal state.

**Standard exception shapes:**

```java
// checked — caller must handle or propagate
public class InsufficientFundsException extends Exception {
    private final double shortFall;
    InsufficientFundsException(String message, double shortFall) {
        super(message);          // pass message to Throwable — getMessage() works
        this.shortFall = shortFall;
    }
    public double getShortFall() { return shortFall; }
}

// unchecked — no declaration needed
public class InvalidAccountStateException extends RuntimeException {
    InvalidAccountStateException(String message) {
        super(message);
    }
}
```

**Don't declare a `message` field** — `Throwable` already has one. Call `super(message)` and let `getMessage()` come from `Throwable`. Only declare custom fields for extra data (like `shortFall`).

**Don't add `@Setter` to exceptions** — exceptions are immutable by contract.

---

## Exception translation

Translate low-level exceptions into domain exceptions at layer boundaries. The caller shouldn't need to know you're using JDBC, files, or any specific library.

```java
public void loadUserConfig(String filename) {
    // no throws IOException on this signature — the caller deals with domain exceptions only
    try {
        readFile(filename);
    } catch (IOException e) {
        throw new ConfigLoadException(e.getMessage(), e);  // preserve cause
    }
}
```

**Preserve the cause** — pass the original exception as the second argument to `super(message, cause)`. The full stack trace chain stays intact for debugging.

**Catch the narrowest type you can** — `catch (IOException e)` not `catch (Exception e)`. Catching `Exception` swallows unrelated `RuntimeException`s from bugs, hiding real failures.

---

## try-with-resources

Guarantees `close()` is called on any `AutoCloseable` resource, even if an exception is thrown. The compiler rewrites it into nested `try/finally` blocks.

```java
try (InputStream in = new FileInputStream(path)) {
    // use in
}  // in.close() always called here
```

**Multiple resources — close order is reverse of open order (stack discipline):**

```java
try (Connection conn = ds.getConnection();    // opens first
     Statement stmt = conn.createStatement()) { // opens second
    // use both
}
// stmt.close() first, then conn.close()
// reason: stmt may depend on conn being open to close cleanly
```

**Suppressed exceptions** — if the try body throws *and* `close()` also throws, the body exception is primary (what you catch). The close exception is attached as suppressed — not lost, but secondary.

```java
try (Resource r = new Resource()) {
    throw new RuntimeException("Body failed");
    // r.close() also throws "Close failed"
} catch (RuntimeException e) {
    e.getMessage();               // "Body failed"   — primary
    e.getSuppressed()[0].getMessage(); // "Close failed"  — suppressed, not swallowed
}
```

Body exception wins because it's more informative about what went wrong; the close exception is secondary context.

---

## Quick recall

**Q. When to use checked vs unchecked?**
A. Checked when the caller can do something useful (retry, fallback, show a message). Unchecked when it's a programming error — null arg, out-of-bounds, invalid state.

**Q. Why call `super(message)` instead of storing your own `message` field?**
A. `Throwable` already has `getMessage()`. A custom field generates a getter that shadows it, which is confusing and non-standard. Only declare fields for extra data beyond the message.

**Q. What is exception translation?**
A. Catching a low-level exception at a layer boundary and rethrowing as a domain exception, preserving the original as the cause.

**Q. Why does `try-with-resources` close in reverse order?**
A. The compiler nests each resource in its own `finally` block — inner closes first. Reverse order ensures a dependent resource (e.g., `Statement`) closes before the resource it depends on (`Connection`).

**Q. Body exception vs close exception — which wins?**
A. Body exception is primary. Close exception is attached as suppressed via `getSuppressed()`. Both are preserved.



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

---

## Topic 2: Exception translation

**Exercise 2a**

Write a method `loadUserConfig(String filename)` that:
- internally calls a helper `readFile(String filename)` which throws a simulated checked `java.io.IOException`
- `loadUserConfig` must NOT declare `throws IOException` on its signature
- translates the low-level `IOException` into a domain `ConfigLoadException` (unchecked), **preserving the original cause**

In `main`, call `loadUserConfig("missing.cfg")` and print `ex.getCause().getClass().getSimpleName()` to confirm the cause is preserved.

---

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
