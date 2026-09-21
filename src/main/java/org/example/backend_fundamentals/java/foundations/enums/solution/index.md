---
order: 20
search: false
---

# Solutions

## Solution: enum-with-state - Enums with State

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

    public String getCode() { return code; }
    public String getMessage() { return message; }
}

// In main:
for (ErrorCode e : ErrorCode.values()) {
    System.out.println(e.getCode() + ": " + e.getMessage());
}
```

## Solution: enum-methods - Enums with Behavior

```java
    // Add this method inside the ErrorCode enum block:
    public boolean isCritical() {
        return this == INVALID_TOKEN;
    }

// In main:
System.out.println(ErrorCode.INVALID_TOKEN.isCritical()); // true
```

## Solution: transaction-status-behavior - Constant-Specific Behavior

```java
interface Labeled {
    String label();
}

enum TransactionStatus implements Labeled {
    PENDING {
        public boolean isFinal() { return false; }
    },
    SETTLED {
        public boolean isFinal() { return true; }
    },
    REJECTED {
        public boolean isFinal() { return true; }
    };

    public abstract boolean isFinal();

    @Override
    public String label() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
```

## Solution: exhaustive-status-switch - Compiler-Checked Decisions

```java
String nextAction(TransactionStatus status) {
    return switch (status) {
        case PENDING -> "wait";
        case SETTLED -> "notify";
        case REJECTED -> "investigate";
    };
}
```

There is no `default`: adding a status forces this decision point to be reconsidered.
