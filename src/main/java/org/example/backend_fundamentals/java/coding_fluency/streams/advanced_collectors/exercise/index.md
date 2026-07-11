---
order: 10
search: false
---

# Advanced Collectors Practice

## Exercise: partitioningby-simple - partitioningBy

### Goal
Split a stream into exactly two buckets.

### Task
Partition orders into paid and not-paid, producing `Map<Boolean, List<Order>>`.

Then use downstream `counting()` to get `Map<Boolean, Long>`.

## Exercise: collectingandthen - collectingAndThen

### Goal
Wrap a downstream collector with a finishing function.

### Task
Collect all paid order IDs into an unmodifiable list with `collectingAndThen(toList(), Collections::unmodifiableList)`.

Then group orders by status and convert `counting()` from `Long` to `Integer` using `Long::intValue`.

## Exercise: groupingby-maxby - groupingBy with maxBy downstream

### Goal
Find the maximum-value element within each group.

### Task
For each status, find the order with the highest total using `groupingBy` and `maxBy`.

### Checks
- The result value type is `Optional<Order>`.
- Use `orElseThrow()` instead of blind `.get()` when reading.

## Exercise: teeing-collectors - teeing

### Goal
Collect into two collectors simultaneously.

### Task
Use `Collectors.teeing()` to compute total order count and total revenue in one pass.

Merge into: `Orders: 6, Revenue: 1989.0`.
