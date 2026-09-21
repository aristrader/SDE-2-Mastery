---
order: 20
---

# Checked Handling and Propagation

Throwing skips the rest of the current block. Java looks for a matching `catch` in the current method and then unwinds
caller frames until one handles it. A normally constructed throwable records the active call frames at construction.
If the selected catch completes normally, Java next runs any associated `finally` block. Only if that also completes
normally does execution continue after the `try` statement; it never resumes after the throw.

Checked exceptions force a choice at each method boundary:

1. handle with `try-catch`
2. declare with `throws`
3. translate to a more useful exception at a boundary

If no handler matches, the thread terminates and reports the uncaught throwable. Declaring `throws` does not handle or wrap anything at runtime; it advertises that a checked failure may escape to the caller. The compiler enforces this contract for checked exceptions as specified by the [JLS](https://docs.oracle.com/javase/specs/jls/se21/html/jls-11.html#jls-11.2.3).

## Own the failure at the right layer

Catch close to the operation only when that layer can retry, choose a fallback, or restore an invariant. Otherwise, let it propagate to the layer that can decide. At a technical boundary, translate a low-level type into one that makes sense to the caller and keep the original exception as its cause:

```java
try {
    return client.read(key);
} catch (IOException cause) {
    throw new ConfigurationLoadException("Cannot read " + key, cause);
}
```

Translation gives the caller a stable vocabulary; `cause` retains diagnostics. Logging and immediately rethrowing at every layer duplicates noise rather than adding context.

## Quick recall

- **Catch where?** Where you can recover, select a fallback, or add useful boundary context.
- **Declare when?** When the caller is the right owner of the failure.
- **Translate when?** At layer boundaries, while preserving the cause.
