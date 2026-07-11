---
order: 30
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

## Quick recall

- **Duplicate key in two-arg `toMap`?** `IllegalStateException`.
- **Fix duplicate keys?** Add a merge function.
- **Whole element as value?** `Function.identity()`.
