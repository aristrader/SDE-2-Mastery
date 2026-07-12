---
order: 60
---

# toMap

Use `toMap` when each key should map to one final value.

```java
Map<String, Order> byId = orders.stream()
    .collect(Collectors.toMap(Order::id, Function.identity()));
```

The two-argument form fails fast on duplicate keys. If duplicates are expected, use the three-argument form with a merge function:

```java
Map<String, BigDecimal> totalByCustomer = orders.stream()
    .collect(Collectors.toMap(
        Order::customerId,
        Order::total,
        BigDecimal::add));
```

Common value choices:

```java
Map<Integer, Employee> byId = employees.stream()
    .collect(Collectors.toMap(Employee::id, Function.identity()));

Map<Integer, String> nameById = employees.stream()
    .collect(Collectors.toMap(Employee::id, Employee::name));
```

For duplicate keys, make the overwrite rule explicit.

```java
Map<Integer, Employee> latestById = employees.stream()
    .collect(Collectors.toMap(
        Employee::id,
        Function.identity(),
        (oldValue, newValue) -> newValue));
```

If one key should map to many values, do not force `toMap`; use `groupingBy`.

## Quick recall

- **Duplicate key in two-arg `toMap`?** `IllegalStateException`.
- **Fix duplicate keys?** Add a merge function.
- **Whole element as value?** `Function.identity()`.
- **One key to many employees?** `groupingBy`, not `toMap`.
