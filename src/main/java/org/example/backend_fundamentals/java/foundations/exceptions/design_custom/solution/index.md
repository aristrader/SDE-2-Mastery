---
order: 20
search: false
---

# Custom Exceptions and Design Solutions

## Solution: custom-exception - Custom Exception

```java
class InvalidAgeException extends RuntimeException {
    InvalidAgeException(String message) {
        super(message);
    }
}

static void validateAge(int age) {
    if (age < 0) {
        throw new InvalidAgeException("Age is invalid: " + age);
    }
}

public static void main(String[] args) {
    try {
        validateAge(-1);
    } catch (InvalidAgeException e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
}
```

## Solution: exception-translation - Exception translation

```java
class ConfigLoadException extends RuntimeException {
    ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}

static String readFile(String filename) throws IOException {
    throw new IOException("Missing file: " + filename);
}

static String loadUserConfig(String filename) {
    try {
        return readFile(filename);
    } catch (IOException e) {
        throw new ConfigLoadException("Could not load config: " + filename, e);
    }
}

public static void main(String[] args) {
    try {
        loadUserConfig("missing.cfg");
    } catch (ConfigLoadException ex) {
        System.out.println(ex.getCause().getClass().getSimpleName());
    }
}
```

The low-level checked exception is hidden from the public method signature, but the cause chain is kept for debugging.

## Solution: exception-design - Exception Design

| Scenario | Choice | Reason |
| --- | --- | --- |
| Invalid login credentials | return value/result | expected business outcome |
| Database unavailable | unchecked exception | infrastructure failure, usually not recoverable locally |
| File missing | checked exception | caller may choose another file or create it |
| Insufficient balance | result or unchecked domain exception | depends whether it is normal flow or request failure |
| Network timeout | checked or retry result | caller may retry or fallback |
| Invalid API request | unchecked exception | invalid caller input, commonly mapped to HTTP 400 |
| Coupon already redeemed | result or unchecked domain exception | expected business rule failure |

## Solution: reflection-questions - Reflection Questions

1. To separate normal flow from failure flow without ignored error codes.
2. `Error` is serious JVM failure; `Exception` is application-level failure.
3. Checked is compiler-enforced; unchecked is not.
4. `throw` throws now; `throws` declares possible checked escape.
5. It can replace pending returns or suppress pending exceptions.
6. It closes reliably with less boilerplate and preserves suppressed close failures.
7. To avoid noisy method signatures and rely on centralized handlers.
8. When the failure is normal expected flow, like login rejected.
9. When the caller is expected to recover from that specific failure.
10. When it is a programming bug or domain validation failure that should abort the request.
