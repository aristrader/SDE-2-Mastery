---
order: 20
search: false
---

# Advanced Collectors Solutions

## Solution: partitioningby-simple - partitioningBy

```java
Map<Boolean, List<Order>> paidPartition = orders.stream()
    .collect(Collectors.partitioningBy(o -> o.status() == OrderStatus.PAID));

Map<Boolean, Long> paidCounts = orders.stream()
    .collect(Collectors.partitioningBy(
        o -> o.status() == OrderStatus.PAID,
        Collectors.counting()));
```

`partitioningBy` always includes both `true` and `false` keys.

```java
Map<Boolean, List<Employee>> activePartition = employees.stream()
    .collect(Collectors.partitioningBy(Employee::active));

Map<Boolean, List<Employee>> salaryPartition = employees.stream()
    .collect(Collectors.partitioningBy(e -> e.salary() >= 100_000));

Map<Boolean, Long> activeCounts = employees.stream()
    .collect(Collectors.partitioningBy(Employee::active, Collectors.counting()));
```

## Solution: collectingandthen - collectingAndThen

```java
List<String> unmodifiablePaidIds = orders.stream()
    .filter(o -> o.status() == OrderStatus.PAID)
    .map(Order::id)
    .collect(Collectors.collectingAndThen(
        Collectors.toList(),
        Collections::unmodifiableList));

Map<OrderStatus, Integer> countsAsInt = orders.stream()
    .collect(Collectors.groupingBy(
        Order::status,
        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
```

## Solution: groupingby-maxby - groupingBy with maxBy downstream

```java
Map<OrderStatus, Optional<Order>> highestTotalByStatus = orders.stream()
    .collect(Collectors.groupingBy(
        Order::status,
        Collectors.maxBy(Comparator.comparing(Order::total))));
```

Read each value with `orElseThrow()` if absence should be impossible.

## Solution: teeing-collectors - teeing

```java
String summary = orders.stream()
    .collect(Collectors.teeing(
        Collectors.counting(),
        Collectors.summingDouble(o -> o.total().doubleValue()),
        (count, sum) -> "Orders: " + count + ", Revenue: " + sum));
```
