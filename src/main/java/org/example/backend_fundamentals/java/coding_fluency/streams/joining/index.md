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

- **Input stream type?** `Stream<String>`.
- **CSV delimiter?** `joining(",")`.
- **Wrap with brackets?** `joining(", ", "[", "]")`.
- **Joining employees directly?** Map them to strings first.
