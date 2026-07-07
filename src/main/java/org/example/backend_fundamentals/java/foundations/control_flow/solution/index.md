---
order: 20
search: false
---

# Solutions

## Solution: enhanced-for-mutation - ConcurrentModificationException

```java
List<String> names = new ArrayList<>(List.of("Alice", "Bob", "Charlie"));
// Throws ConcurrentModificationException
for (String name : names) {
    if (name.equals("Bob")) {
        names.remove(name); 
    }
}
```
An enhanced `for` loop compiles down to using an `Iterator`. If you modify the underlying collection directly (like calling `list.remove()`) instead of using the `Iterator`'s own `.remove()` method, the iterator detects the structural change and throws a `ConcurrentModificationException` to fail fast.

## Solution: modern-switch - Modern Switch Syntax

```java
// Traditional
switch (day) {
    case SATURDAY:
    case SUNDAY:
        System.out.println("Weekend");
        break;
    default:
        System.out.println("Weekday");
        break;
}

// Modern (Java 14+)
switch (day) {
    case SATURDAY, SUNDAY -> System.out.println("Weekend");
    default -> System.out.println("Weekday");
}
```
The modern syntax is more concise and prevents accidental fall-through because it doesn't require `break` statements.
