---
order: 40
---

# Custom Exceptions and Design

Create a custom exception only when its type gives a caller a meaningful domain or boundary vocabulary. `IOException` already says something useful; `ConfigurationLoadException` can say why an infrastructure failure matters to a configuration API. `SomethingWentWrongException` says neither.

## Preserve the diagnostic chain

At a boundary, translate the implementation exception but preserve it as the cause. `Throwable.getCause()` then keeps the low-level evidence available to logs and diagnostics.

```java
final class ConfigurationLoadException extends RuntimeException {
    ConfigurationLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Keep exception objects simple:

- call `super(message)`
- call `super(message, cause)` when translating
- add fields only for useful structured data, such as a stable error code
- avoid setters and a duplicate `message` field

## Choose the contract, not a fashion

Modern Spring-style backend code often uses unchecked domain exceptions with centralized HTTP handling because every controller caller cannot recover locally. Checked exceptions still fit when callers must make a concrete recovery choice, such as selecting another input source. Expected business outcomes can be a result instead: do not use an exception merely to say that credentials were rejected.

Catch specific types. Do not translate an exception if the original type is already the correct public contract, and do not catch only to log and rethrow unchanged.

## Quick recall

- **Preserve root cause?** Pass it as `cause` to the superclass constructor.
- **Own `message` field?** No, `Throwable` already has one.
- **Custom checked exception?** When caller recovery is part of the contract.
- **Custom unchecked exception?** A violated precondition or a failure the caller cannot resolve locally.
