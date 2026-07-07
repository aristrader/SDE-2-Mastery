---
order: 20
search: false
---

# Solutions

## Solution: file-structure - Top-Level Modifiers

```java
// In App.java
// private class HiddenApp {} // ERROR: modifier private not allowed here

public class App {
    private class HiddenNestedApp {} // Valid
}
```
Java prevents top-level classes from being `private` because `private` means "visible only to the enclosing scope." Since a top-level class has no enclosing class, it would be invisible to the entire application and therefore impossible to use. `private` classes are only valid when nested inside another class.
