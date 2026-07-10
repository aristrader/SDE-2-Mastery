---
order: 10
search: false
---

# Stream Collectors Practice

## Domain model

Use this model:

```java
public enum OrderStatus { PENDING, PAID, CANCELLED }

public record LineItem(String productId, int quantity, BigDecimal unitPrice) {}

public record Order(
    String id,
    OrderStatus status,
    String customerId,
    List<LineItem> lineItems,
    BigDecimal total
) {}
```

Same seed data:

```java
List<Order> orders = List.of(
    new Order("O1", PAID,      "C1", List.of(new LineItem("P1", 2, new BigDecimal("50.00"))), new BigDecimal("100.00")),
    new Order("O2", PENDING,   "C2", List.of(new LineItem("P2", 1, new BigDecimal("200.00"))), new BigDecimal("200.00")),
    new Order("O3", CANCELLED, "C1", List.of(new LineItem("P3", 3, new BigDecimal("30.00"))), new BigDecimal("90.00")),
    new Order("O4", PAID,      "C3", List.of(new LineItem("P1", 1, new BigDecimal("50.00")), new LineItem("P4", 2, new BigDecimal("75.00"))), new BigDecimal("200.00")),
    new Order("O5", PENDING,   "C2", List.of(new LineItem("P2", 2, new BigDecimal("200.00"))), new BigDecimal("400.00")),
    new Order("O6", PAID,      "C1", List.of(new LineItem("P5", 1, new BigDecimal("999.00"))), new BigDecimal("999.00"))
);
```

## Exercise: groupingby-simple - groupingBy simple

### Goal
Bucket a flat list into groups using `Collectors.groupingBy`.

### Task
Group all orders by `OrderStatus`, producing a `Map<OrderStatus, List<Order>>`. Print how many orders are in each bucket.

### Gotcha
`groupingBy` produces a `List` value by default. Using `toMap(Order::status, ...)` instead collides on the second PAID order and throws `IllegalStateException`. This exercise is about `groupingBy`, not `toMap`.

## Exercise: groupingby-counting - groupingBy with downstream counting

### Goal
Aggregate within each group using a downstream collector.

### Task
Produce a `Map<OrderStatus, Long>` that maps each status to the number of orders with that status. Use `groupingBy` with `Collectors.counting()` as the downstream collector.

### Gotcha
`Collectors.counting()` returns `Long`, not `Integer`. A map declared with `Integer` value type won't compile. Signature is `groupingBy(classifier, downstream)` — don't swap parameter order.

## Exercise: tomap-unique - toMap unique keys

### Goal
Build a lookup map from a stream where keys are guaranteed unique.

### Task
Produce a `Map<String, Order>` keyed by order ID, mapping each ID to its `Order`. This is the happy path — the seed data has no duplicate IDs.

### Gotcha
The two-arg `toMap(keyMapper, valueMapper)` throws `IllegalStateException` if two elements map to the same key. Write the two-arg form here; Exercise 4 covers the collision case.

## Exercise: tomap-duplicate - toMap duplicate key handler

### Goal
Survive duplicate keys using the merge function overload.

### Task
Build a `Map<String, BigDecimal>` keyed by `customerId`, where the value is the **sum** of all order totals for that customer. Customer C1 has three orders — the merge function must add totals together, not overwrite.

Try it first with the two-arg `toMap` to see the exception, then switch to the three-arg form.

### Gotcha
The three-arg `toMap(keyMapper, valueMapper, mergeFunction)` resolves collisions by calling `mergeFunction.apply(existing, incoming)`. Wrong argument order (subtracting instead of adding) produces a wrong but non-crashing result that's easy to miss.

## Exercise: joining-strings - joining

### Goal
Produce a delimited string from a stream of strings.

### Task
1. Join all order IDs into a single comma-separated string: `O1,O2,O3,O4,O5,O6`.
2. Join PAID order IDs with delimiter ` | `, prefix `[`, suffix `]`: `[O1 | O4 | O6]`.

### Gotcha
`Collectors.joining()` works on `Stream<String>` only — forget to `.map(Order::id)` before `.collect(joining(...))` and you get a compile error. The three-arg overload is `joining(delimiter, prefix, suffix)` — argument order matters.

## Exercise: partitioningby-simple - partitioningBy

### Goal
Split a stream into exactly two buckets (true/false) using `partitioningBy`.

### Task
Partition orders into paid (`true`) and not-paid (`false`), producing a `Map<Boolean, List<Order>>`.

Then, as a follow-up, use `partitioningBy` with a downstream `counting()` to get the count of orders in each partition rather than the list.

### Gotcha
`partitioningBy` always produces both keys (`true` and `false`), even if one partition is empty. `groupingBy` on a boolean classifier may omit keys for empty groups — reach for `partitioningBy` when both keys must be present.

## Exercise: collectingandthen - collectingAndThen

### Goal
Wrap a downstream collector with a finishing function using `collectingAndThen`.

### Task
1. Collect all PAID order IDs into an **unmodifiable** list by wrapping `toList()` with `collectingAndThen(toList(), Collections::unmodifiableList)`.
2. Group orders by status, then use `collectingAndThen` with `counting()` to produce a `Map<OrderStatus, Integer>` (not `Long`) by adding a `Long::intValue` finisher inside the downstream.

### Gotcha
`Collectors.counting()` returns `Long`. `collectingAndThen(counting(), Long::intValue)` converts the per-group count to `int` — the idiomatic way to change a downstream result's type without a separate `.entrySet().stream()` pass.

## Exercise: groupingby-maxby - groupingBy with maxBy downstream

### Goal
Find the maximum-value element within each group using a chained downstream collector.

### Task
For each `OrderStatus`, find the order with the highest `total`. Produce a `Map<OrderStatus, Optional<Order>>`. Use `groupingBy` with `Collectors.maxBy(Comparator.comparing(Order::total))` as the downstream.

### Gotcha
Result value type is `Optional<Order>`, not `Order` — `maxBy` always returns `Optional` because the downstream collector can't prove the group is non-empty. `.get()` works on this seed data but is a code smell — use `.orElseThrow()` to be explicit.

## Exercise: teeing-collectors - teeing (Java 12+)

### Goal
Collect into two collectors simultaneously and merge the results, without iterating the stream twice.

### Task
Using `Collectors.teeing()`, compute in a single stream pass over all orders:
- The total number of orders (`counting()`).
- The sum of all order totals (`Collectors.summingDouble(o -> o.total().doubleValue())`).

Merge the two results into a summary string: `"Orders: 6, Revenue: 1989.0"`.

### Gotcha
`teeing(collector1, collector2, mergeFunction)` is Java 12+. The merge function receives each downstream collector's result in the order you passed them.
