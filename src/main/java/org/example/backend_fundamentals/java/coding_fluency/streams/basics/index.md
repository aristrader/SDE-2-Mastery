---
order: 40
---

# Stream Collection Basics

The first collector decision is the result type and mutability. Most collection pipelines end by turning a stream back into a `List`, `Set`, `Map`, or string.

| Collector | Produces | Notes |
| --- | --- | --- |
| `Collectors.toList()` | mutable list | Usually an `ArrayList`; available since Java 8. |
| `Stream.toList()` | unmodifiable list | Java 16+. Prefer when the caller should not mutate. |
| `Collectors.toUnmodifiableList()` | unmodifiable list | Java 10-15 equivalent style. |
| `Collectors.toSet()` | mutable set | Deduplicates; no order guarantee. |
| `Collectors.toUnmodifiableSet()` | unmodifiable set | Java 10+. |

Choose mutability intentionally. Returning a mutable list invites callers to change it; returning an unmodifiable list documents that the stream result is final.

## toList

```java
List<String> names = employees.stream()
    .map(Employee::name)
    .collect(Collectors.toList());
```

On Java 16+, prefer `stream.toList()` when the result should not be modified.

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

- **Mutable list collector?** `Collectors.toList()`.
- **Unmodifiable list on Java 16+?** `stream.toList()`.
- **Set collector guarantee order?** No, not unless you collect into an ordered set explicitly.
- **Does `toSet()` dedupe custom objects correctly by magic?** No, it still depends on `equals()` and `hashCode()`.
