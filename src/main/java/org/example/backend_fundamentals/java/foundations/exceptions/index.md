---
order: 30
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


<ExerciseNav />
