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
Enums in Java are full classes. They can encapsulate both state (fields) and behavior (methods) directly related to the constants, making them much more powerful than simple integer constants.
