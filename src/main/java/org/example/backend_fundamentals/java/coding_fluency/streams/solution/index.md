---
order: 20
search: false
---

# Stream Collectors Solutions

## Solution: groupingby-simple - groupingBy simple
```java
Map<OrderStatus, List<Order>> byStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::status));
```

## Solution: groupingby-counting - groupingBy with downstream counting
```java
Map<OrderStatus, Long> counts = orders.stream()
    .collect(Collectors.groupingBy(Order::status, Collectors.counting()));
```

## Solution: tomap-unique - toMap unique keys
```java
Map<String, Order> orderById = orders.stream()
    .collect(Collectors.toMap(Order::id, Function.identity()));
```

## Solution: tomap-duplicate - toMap duplicate key handler
```java
Map<String, BigDecimal> totalByCustomer = orders.stream()
    .collect(Collectors.toMap(
        Order::customerId,
        Order::total,
        BigDecimal::add
    ));
```

## Solution: joining-strings - joining
```java
String allIds = orders.stream().map(Order::id).collect(Collectors.joining(","));
String paidIds = orders.stream()
    .filter(o -> o.status() == OrderStatus.PAID)
    .map(Order::id)
    .collect(Collectors.joining(" | ", "[", "]"));
```

## Solution: partitioningby-simple - partitioningBy
```java
Map<Boolean, List<Order>> paidPartition = orders.stream()
    .collect(Collectors.partitioningBy(o -> o.status() == OrderStatus.PAID));

Map<Boolean, Long> paidCounts = orders.stream()
    .collect(Collectors.partitioningBy(o -> o.status() == OrderStatus.PAID, Collectors.counting()));
```

## Solution: collectingandthen - collectingAndThen
```java
List<String> unmodifiablePaidIds = orders.stream()
    .filter(o -> o.status() == OrderStatus.PAID)
    .map(Order::id)
    .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));

Map<OrderStatus, Integer> countsAsInt = orders.stream()
    .collect(Collectors.groupingBy(
        Order::status,
        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
    ));
```

## Solution: groupingby-maxby - groupingBy with maxBy downstream
```java
Map<OrderStatus, Optional<Order>> highestTotalByStatus = orders.stream()
    .collect(Collectors.groupingBy(
        Order::status,
        Collectors.maxBy(Comparator.comparing(Order::total))
    ));
```

## Solution: teeing-collectors - teeing (Java 12+)
```java
String summary = orders.stream()
    .collect(Collectors.teeing(
        Collectors.counting(),
        Collectors.summingDouble(o -> o.total().doubleValue()),
        (count, sum) -> "Orders: " + count + ", Revenue: " + sum
    ));
```
