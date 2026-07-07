---
order: 20
search: false
---

# Method References Solutions

## Solution: static-method - Static method reference
```java
orders.stream().filter(OrderUtils::isHighValue);
orders.stream().map(OrderUtils::summarise);
List<Integer> ints = stringNums.stream().map(Integer::parseInt).toList();
```

## Solution: bound-instance - Bound instance method reference
```java
orders.stream().forEach(System.out::println);
orders.stream().filter(target.status()::equals);
```

## Solution: unbound-instance - Unbound instance method reference
```java
// Two hops required because method references cannot chain calls
orders.stream().map(Order::status).map(OrderStatus::name);
orders.stream().map(Order::customerId);
statuses.stream().map(String::toUpperCase);
```

## Solution: comparator-comparing - Comparator.comparing
```java
orders.stream().sorted(Comparator.comparing(Order::customerId));
orders.stream().sorted(Comparator.comparing(Order::total).reversed());
orders.stream().sorted(Comparator.comparing(Order::status).thenComparing(Order::total).reversed());
```

## Solution: constructor-ref - Constructor reference
```java
Stream<OrderDto> dtos = orders.stream().map(OrderDto::new);
ArrayList<OrderDto> dtoList = dtos.collect(Collectors.toCollection(ArrayList::new));
```
