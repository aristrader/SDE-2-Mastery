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
