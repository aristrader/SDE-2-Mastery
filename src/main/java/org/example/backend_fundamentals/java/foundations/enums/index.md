---
order: 60
---

# Enums

Use an enum when a domain has a **closed, named set of values**: a transaction type, lifecycle state, role, or internal error category. It replaces a convention such as `"CREDIT"` with a type the compiler can check.

## Closed domain, state, and behavior

This accepts any string and leaves invalid states for runtime:

```java
void process(String transactionType) { }

process("credit"); // compiles; typo or unsupported value is possible
```

An enum makes the valid values explicit:

```java
enum TransactionType {
    CREDIT,
    DEBIT
}

void process(TransactionType transactionType) { }

process(TransactionType.CREDIT); // only a declared constant fits
```

An enum constant is an object created by the JVM for that enum class. It can carry immutable state and behavior that belongs to the value, avoiding parallel constants and a separate lookup map.

```java
public enum ErrorCode {
    USER_NOT_FOUND("E001", "User not found"),
    INVALID_TOKEN("E002", "Invalid token");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() { return code; }
    public String message() { return message; }
}
```

Enum constructors are private implicitly; callers cannot use `new ErrorCode(...)`. Enums cannot extend another class, but they can implement interfaces.

## Constant-specific behavior

Use a shared method when all constants follow one rule:

```java
public enum Status {
    ACTIVE,
    INACTIVE;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
```

Use an abstract method only when each constant owns a genuinely different rule. Every constant must implement it; otherwise the enum does not compile.

```java
public enum TransactionStatus implements Labeled {
    PENDING { public boolean isFinal() { return false; } },
    SETTLED { public boolean isFinal() { return true; } },
    REJECTED { public boolean isFinal() { return true; } };

    public abstract boolean isFinal();

    @Override
    public String label() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}

interface Labeled {
    String label();
}
```

Do not put service dependencies or orchestration inside an enum. Behavior derived only from the enum value belongs here; database calls and injected collaborators belong in a service.

## Switching and collection choices

For a switch expression over a known enum, omit `default` when all constants are listed. Adding a new status then produces a compiler error at this decision point instead of silently taking a fallback.

```java
String nextAction(TransactionStatus status) {
    return switch (status) {
        case PENDING -> "wait";
        case SETTLED -> "notify";
        case REJECTED -> "investigate";
    };
}
```

Prefer `EnumMap<TransactionStatus, Handler>` for a map keyed only by this enum, and `EnumSet<TransactionStatus>` for a set of its values. They express the closed key space and use specialized JDK implementations.

## Boundaries and gotchas

- Never use `ordinal()` as a database value, wire value, or business code. It changes when constants are inserted or reordered. Store an explicit stable code, or use the enum name only when that name is the deliberate contract.
- `Enum.valueOf(TransactionStatus.class, input)` is case-sensitive and throws for unknown input. Validate external input and map it deliberately; do not let raw user strings become internal states accidentally.
- With JPA, prefer `@Enumerated(EnumType.STRING)` over ordinal storage unless a migration strategy guarantees ordinal stability.
- `==` is correct for comparing two enum constants. There is one instance per constant per enum class loader.

## Quick recall

- **Why choose an enum over `String` constants?** It constrains a closed domain at compile time and groups the value's state and behavior.
- **Can an enum have fields, constructors, and methods?** Yes; its constructor is private implicitly, and constants supply its arguments.
- **When is an abstract enum method appropriate?** When each constant has distinct value-local behavior; every constant must implement it.
- **Why omit `default` from an enum switch expression?** An exhaustive switch lets the compiler flag decision points when a new constant is added.
- **Why avoid `ordinal()` outside the enum?** Its number is declaration position, so reordering or inserting constants corrupts a persisted or external contract.
- **When use `EnumMap` or `EnumSet`?** When all keys or elements are from one enum type; they make that constraint explicit and use specialized JDK collections.

---
