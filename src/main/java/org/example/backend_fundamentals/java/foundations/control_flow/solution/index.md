---
order: 20
search: false
---

# Solutions

## Solution: enhanced-for-mutation - ConcurrentModificationException

```java
List<String> names = new ArrayList<>(List.of("Alice", "Bob", "Charlie", "Diana"));
for (String name : names) {
    if (name.equals("Bob")) {
        names.remove(name); // Throws ConcurrentModificationException on the next iterator access.
    }
}
```
An enhanced `for` uses an `Iterator` for an `Iterable`. Directly changing the collection invalidates the iterator; many JDK iterators detect that structural change and fail fast with `ConcurrentModificationException`. This is best-effort detection, not a concurrency guarantee.

```java
for (Iterator<String> iterator = names.iterator(); iterator.hasNext();) {
    if (iterator.next().equals("Bob")) {
        iterator.remove();
    }
}
```

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
The modern syntax prevents accidental fall-through because arrow rules do not need `break`. When a branch computes a value, use an exhaustive switch expression:

```java
String kind = switch (day) {
    case SATURDAY, SUNDAY -> "Weekend";
    default -> "Weekday";
};
```
