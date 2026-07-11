---
order: 40
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

## Quick recall

- **Input stream type?** `Stream<String>`.
- **CSV delimiter?** `joining(",")`.
- **Wrap with brackets?** `joining(", ", "[", "]")`.
