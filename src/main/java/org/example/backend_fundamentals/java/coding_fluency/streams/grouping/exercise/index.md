---
order: 10
search: false
---

# groupingBy Practice

## Exercise: groupingby-simple - groupingBy simple

### Goal
Bucket a flat list into groups.

### Task
Group all orders by `OrderStatus`, producing `Map<OrderStatus, List<Order>>`.

### Checks
- Print how many orders are in each bucket.
- Explain why this is `groupingBy`, not `toMap`.

## Exercise: groupingby-counting - groupingBy with downstream counting

### Goal
Aggregate within each group.

### Task
Produce `Map<OrderStatus, Long>` using `groupingBy` with `Collectors.counting()`.

### Checks
- The value type is `Long`, not `Integer`.
- The downstream collector is the second argument.
