---
order: 20
search: false
---

# toMap Solutions

## Solution: tomap-unique - toMap unique keys

```java
Map<String, Order> orderById = orders.stream()
    .collect(Collectors.toMap(Order::id, Function.identity()));
```

The two-arg form is correct only because order IDs are unique.

## Solution: tomap-duplicate - toMap duplicate key handler

```java
Map<String, BigDecimal> totalByCustomer = orders.stream()
    .collect(Collectors.toMap(
        Order::customerId,
        Order::total,
        BigDecimal::add));
```

`BigDecimal::add` receives the existing and incoming totals for the same customer.

## Solution: employee-tomap - Employee Maps

```java
Map<Integer, Employee> byId = employees.stream()
    .collect(Collectors.toMap(Employee::id, Function.identity()));

Map<Integer, String> nameById = employees.stream()
    .collect(Collectors.toMap(Employee::id, Employee::name));

Map<Integer, Employee> latestById = employees.stream()
    .collect(Collectors.toMap(
        Employee::id,
        Function.identity(),
        (oldEmployee, newEmployee) -> newEmployee));
```

The two-argument `toMap` throws `IllegalStateException` when duplicate IDs appear.
