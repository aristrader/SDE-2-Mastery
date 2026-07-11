---
order: 20
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

## Quick recall

- **Default grouping value type?** `List<T>`.
- **Count per group type?** `Long`.
- **Extract one field per element before collecting?** `mapping(...)`.
