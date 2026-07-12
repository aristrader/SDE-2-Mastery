---
order: 80
---

# Advanced Collectors

These collectors are useful after the common cases are comfortable.

| Collector | Use it for |
| --- | --- |
| `partitioningBy(predicate)` | exactly two buckets: `true` and `false` |
| `collectingAndThen(downstream, finisher)` | transform the collector result |
| `maxBy(comparator)` | maximum element, usually downstream of `groupingBy` |
| `teeing(a, b, merger)` | run two collectors in one pass, Java 12+ |

Prefer the simpler collector when it fits. Advanced collectors are for reducing extra passes or making the result type exact.

## partitioningBy

Use `partitioningBy` when the classifier is a boolean.

```java
Map<Boolean, List<Employee>> byActive = employees.stream()
    .collect(Collectors.partitioningBy(Employee::active));
```

With a downstream collector:

```java
Map<Boolean, Long> activeCounts = employees.stream()
    .collect(Collectors.partitioningBy(Employee::active, Collectors.counting()));
```

Unlike `groupingBy`, both `true` and `false` keys are present.

## collectingAndThen

`collectingAndThen` runs a normal collector and then transforms its result.

```java
List<String> names = employees.stream()
    .map(Employee::name)
    .collect(Collectors.collectingAndThen(
        Collectors.toList(),
        Collections::unmodifiableList));
```

## maxBy downstream

`maxBy` is common under `groupingBy`.

```java
Map<String, Optional<Employee>> highestPaidByDepartment = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.maxBy(Comparator.comparingDouble(Employee::salary))));
```

## Quick recall

- **Always both boolean keys?** `partitioningBy`.
- **Convert `Long` count to `Integer` inside collector?** `collectingAndThen(counting(), Long::intValue)`.
- **Why does `maxBy` return `Optional`?** The collector cannot prove the group is non-empty.
- **Need two independent aggregate results in one pass?** `teeing`.
