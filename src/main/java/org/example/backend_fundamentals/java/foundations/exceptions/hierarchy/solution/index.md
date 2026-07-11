---
order: 20
search: false
---

# Exception Hierarchy Solutions

## Solution: throw-vs-throws - throw vs throws

```java
static void validateAge(int age) {
    if (age < 0) {
        throw new IllegalArgumentException("Age cannot be negative");
    }
    System.out.println("Age is valid");
}
```

`throw` is the action. No `throws` is required because `IllegalArgumentException` extends `RuntimeException`.

## Solution: checked-vs-unchecked - Checked vs Unchecked

```java
static void checked() throws IOException {
    throw new IOException("file failed");
}

static void unchecked() {
    throw new IllegalArgumentException("bad input");
}
```

`IOException` is checked because it extends `Exception` directly. `IllegalArgumentException` is unchecked because it extends `RuntimeException`.

## Solution: exception-hierarchy - Exception Hierarchy

```java
// throw new ArithmeticException();     // unchecked
// throw new NullPointerException();    // unchecked
// throw new IOException();             // checked: catch or declare
```

The compiler complains only for the checked exception.
