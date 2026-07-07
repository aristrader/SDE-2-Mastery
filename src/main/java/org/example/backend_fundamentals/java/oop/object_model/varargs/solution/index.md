---
order: 20
search: false
---

# Solutions

## Solution: varargs-rules - The Last Parameter

```java
// public void log(String... messages, int level) {} // ERROR

public void log(int level, String... messages) {
    System.out.println("Level: " + level);
    for (String msg : messages) {
        System.out.println(msg);
    }
}

// Caller
log(1, "Error", "Disk full");
```
Varargs must be the last parameter so the compiler knows exactly when the "variable" arguments end. If `String...` was allowed first, the compiler wouldn't know if the last argument belonged to the varargs list or to the next parameter.
