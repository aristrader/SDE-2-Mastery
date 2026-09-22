---
order: 40
---

# Stream Collection Basics

The first collector decision is the result type and mutability. Most collection pipelines end by turning a stream back into a `List`, `Set`, `Map`, or string.

| Collector | Produces | Notes |
| --- | --- | --- |
| `Collectors.toList()` | list with no mutability/implementation guarantee | Available since Java 8; use an explicit collection factory when callers must mutate. |
| `Stream.toList()` | unmodifiable list | Java 16+. Prefer when the caller should not mutate. |
| `Collectors.toUnmodifiableList()` | unmodifiable list | Java 10+; rejects null elements, unlike `Stream.toList()`. |
| `Collectors.toSet()` | set with no mutability/implementation guarantee | Deduplicates; no order guarantee. |
| `Collectors.toUnmodifiableSet()` | unmodifiable set | Java 10+. |

Choose mutability intentionally. `Collectors.toList()` is a convenient unspecified result; use `Collectors.toCollection(ArrayList::new)` when mutability is part of the contract, or an unmodifiable collector/result when callers must not change it.

## toList

```java
List<String> names = employees.stream()
    .map(Employee::name)
    .collect(Collectors.toList());
```

On Java 16+, prefer `stream.toList()` when the result should not be modified. It is not identical to `Collectors.toUnmodifiableList()`: `Stream.toList()` permits null elements, while the unmodifiable collector rejects them.

```java
List<String> names = employees.stream()
    .map(Employee::name)
    .toList();
```

## toSet

`toSet` removes duplicates but does not promise a specific implementation or order.

```java
Set<String> departments = employees.stream()
    .map(Employee::department)
    .collect(Collectors.toSet());
```

If order matters, collect into the exact set type.

```java
Set<String> departments = employees.stream()
    .map(Employee::department)
    .collect(Collectors.toCollection(LinkedHashSet::new));
```

## Quick recall

- **Explicit mutable list collector?** `Collectors.toCollection(ArrayList::new)`.
- **Unmodifiable list on Java 16+?** `stream.toList()`.
- **Set collector guarantee order?** No, not unless you collect into an ordered set explicitly.
- **Does `toSet()` dedupe custom objects correctly by magic?** No, it still depends on `equals()` and `hashCode()`.
