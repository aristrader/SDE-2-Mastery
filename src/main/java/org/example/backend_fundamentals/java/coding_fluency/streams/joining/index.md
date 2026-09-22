---
order: 50
---

# joining

`Collectors.joining` turns a `Stream<String>` into one `String`.

```java
String csv = fruits.stream()
    .collect(Collectors.joining(", "));
```

Overloads:

- `joining()`
- `joining(delimiter)`
- `joining(delimiter, prefix, suffix)`

Map non-string objects to strings before joining.

```java
String names = employees.stream()
    .map(Employee::name)
    .collect(Collectors.joining(", "));
```

Use the three-argument overload when the output needs wrapping:

```java
String departments = employees.stream()
    .map(Employee::department)
    .distinct()
    .collect(Collectors.joining(", ", "[", "]"));
```

## Quick recall

**Q. What input does `joining` consume?**
A. `Stream<String>`; map domain objects to their required text first.

**Q. What makes CSV output?**
A. `joining(",")` (or `", "` when spacing is part of the required format).

**Q. How do you add brackets around the final string?**
A. Use `joining(delimiter, prefix, suffix)`.
