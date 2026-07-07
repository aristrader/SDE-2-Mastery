---
order: 30
---

# Source File Structure

Java enforces strict rules on how `.java` files are structured and how classes are organized within them.

## Top-Level Classes
A top-level class is a class declared at the root of a file (not inside another class).
1. **One public class per file:** You can only have one `public` top-level class per `.java` file.
2. **Filename must match:** The name of the `.java` file must exactly match the name of the `public` class inside it.
3. **Other classes:** You can have multiple other top-level classes in the same file, but they **must be package-private** (no access modifier).
4. **No private or protected:** Top-level classes **cannot** be `private` or `protected`. A private top-level class would be invisible to everyone, making it useless.

## Nested Classes
Classes declared *inside* another class (nested classes) have different rules.
- Nested classes **can** be `private`, `protected`, `public`, or package-private.
- A `private` nested class is perfectly valid and can be instantiated by the outer class. It is completely hidden from the outside world.

```java
public class Card { // Top-level (Public)
    private class CardDetails { // Nested (Private)
        // Only Card can see and use this class
    }
}
class Helper { } // Top-level (Package-Private)
```
