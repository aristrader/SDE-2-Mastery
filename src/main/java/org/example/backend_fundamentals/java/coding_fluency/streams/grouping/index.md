---
order: 70
---

# groupingBy

Use `groupingBy` when one key maps to many input elements.

```java
Map<OrderStatus, List<Order>> byStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::status));
```

The two-argument overload applies a downstream collector to each group:

```java
Map<OrderStatus, Long> counts = orders.stream()
    .collect(Collectors.groupingBy(Order::status, Collectors.counting()));
```

Common downstream collectors:

- `counting()`
- `summingInt(...)`
- `averagingInt(...)`
- `mapping(..., toList())`
- `maxBy(...)`

## Downstream examples

```java
Map<String, List<String>> namesByDepartment = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.mapping(Employee::name, Collectors.toList())));

Map<String, Double> averageSalaryByDepartment = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.averagingDouble(Employee::salary)));

Map<String, Optional<Employee>> highestPaidByDepartment = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.maxBy(Comparator.comparingDouble(Employee::salary))));
```

The mental model is:

```text
classifier chooses bucket -> downstream collector reduces each bucket
```

Use `partitioningBy` instead when there are exactly two boolean buckets.

## Quick recall

- **Default grouping value type?** `List<T>`.
- **Count per group type?** `Long`.
- **Extract one field per element before collecting?** `mapping(...)`.
- **Highest item per group?** `maxBy(...)`, usually returning `Optional<T>`.
- **Exactly active/inactive buckets?** `partitioningBy`.
