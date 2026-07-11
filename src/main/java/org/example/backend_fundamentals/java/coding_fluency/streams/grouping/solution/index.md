---
order: 20
search: false
---

# groupingBy Solutions

## Solution: groupingby-simple - groupingBy simple

```java
Map<OrderStatus, List<Order>> byStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::status));
```

Use `groupingBy` because each status can have multiple orders.

## Solution: groupingby-counting - groupingBy with downstream counting

```java
Map<OrderStatus, Long> counts = orders.stream()
    .collect(Collectors.groupingBy(Order::status, Collectors.counting()));
```

`counting()` returns `Long`.
