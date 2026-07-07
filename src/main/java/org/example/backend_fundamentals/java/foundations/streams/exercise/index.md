---
order: 10
search: false
---

# Stream Collectors Practice

## Why this matters
`Collectors.groupingBy` is the most common stream operation in production service code — grouping transactions by status, user by country, events by type.
`toMap` is an interview trap: it looks simple but throws at runtime on duplicate keys unless you supply a merge function.
Knowing `partitioningBy`, downstream collectors like `counting()`, and `maxBy` separates candidates who have read the docs from those who have used them.

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

---

## Exercise 1: groupingBy — simple (~8 min)
**Goal:** Bucket a flat list into groups using `Collectors.groupingBy`.

**Task:** Group all orders by `OrderStatus`, producing a `Map<OrderStatus, List<Order>>`. Print how many orders are in each bucket.

**Gotcha:** `groupingBy` produces a `List` value by default. Using `toMap(Order::status, ...)` instead collides on the second PAID order and throws `IllegalStateException`. This exercise is about `groupingBy`, not `toMap`.

---

## Exercise 2: groupingBy with downstream counting (~8 min)
**Goal:** Aggregate within each group using a downstream collector.

**Task:** Produce a `Map<OrderStatus, Long>` that maps each status to the number of orders with that status. Use `groupingBy` with `Collectors.counting()` as the downstream collector.

**Gotcha:** `Collectors.counting()` returns `Long`, not `Integer`. A map declared with `Integer` value type won't compile. Signature is `groupingBy(classifier, downstream)` — don't swap parameter order.

---

## Exercise 3: toMap — unique keys (~8 min)
**Goal:** Build a lookup map from a stream where keys are guaranteed unique.

**Task:** Produce a `Map<String, Order>` keyed by order ID, mapping each ID to its `Order`. This is the happy path — the seed data has no duplicate IDs.

**Gotcha:** The two-arg `toMap(keyMapper, valueMapper)` throws `IllegalStateException` if two elements map to the same key. Write the two-arg form here; Exercise 4 covers the collision case.

---

## Exercise 4: toMap — duplicate key handler (~8 min)
**Goal:** Survive duplicate keys using the merge function overload.

**Task:** Build a `Map<String, BigDecimal>` keyed by `customerId`, where the value is the **sum** of all order totals for that customer. Customer C1 has three orders — the merge function must add totals together, not overwrite.

Try it first with the two-arg `toMap` to see the exception, then switch to the three-arg form.

**Gotcha:** The three-arg `toMap(keyMapper, valueMapper, mergeFunction)` resolves collisions by calling `mergeFunction.apply(existing, incoming)`. Wrong argument order (subtracting instead of adding) produces a wrong but non-crashing result that's easy to miss.

---

## Exercise 5: joining (~5 min)
**Goal:** Produce a delimited string from a stream of strings.

**Task:**
1. Join all order IDs into a single comma-separated string: `O1,O2,O3,O4,O5,O6`.
2. Join PAID order IDs with delimiter ` | `, prefix `[`, suffix `]`: `[O1 | O4 | O6]`.

**Gotcha:** `Collectors.joining()` works on `Stream<String>` only — forget to `.map(Order::id)` before `.collect(joining(...))` and you get a compile error. The three-arg overload is `joining(delimiter, prefix, suffix)` — argument order matters.

---

## Exercise 6: partitioningBy (~6 min)
**Goal:** Split a stream into exactly two buckets (true/false) using `partitioningBy`.

**Task:** Partition orders into paid (`true`) and not-paid (`false`), producing a `Map<Boolean, List<Order>>`.

Then, as a follow-up, use `partitioningBy` with a downstream `counting()` to get the count of orders in each partition rather than the list.

**Gotcha:** `partitioningBy` always produces both keys (`true` and `false`), even if one partition is empty. `groupingBy` on a boolean classifier may omit keys for empty groups — reach for `partitioningBy` when both keys must be present.

---

## Exercise 7: collectingAndThen — post-process a collection (~8 min)
**Goal:** Wrap a downstream collector with a finishing function using `collectingAndThen`.

**Task:**
1. Collect all PAID order IDs into an **unmodifiable** list by wrapping `toList()` with `collectingAndThen(toList(), Collections::unmodifiableList)`.
2. Group orders by status, then use `collectingAndThen` with `counting()` to produce a `Map<OrderStatus, Integer>` (not `Long`) by adding a `Long::intValue` finisher inside the downstream.

**Gotcha:** `Collectors.counting()` returns `Long`. `collectingAndThen(counting(), Long::intValue)` converts the per-group count to `int` — the idiomatic way to change a downstream result's type without a separate `.entrySet().stream()` pass.

---

## Exercise 8: groupingBy with maxBy downstream (~10 min)
**Goal:** Find the maximum-value element within each group using a chained downstream collector.

**Task:** For each `OrderStatus`, find the order with the highest `total`. Produce a `Map<OrderStatus, Optional<Order>>`. Use `groupingBy` with `Collectors.maxBy(Comparator.comparing(Order::total))` as the downstream.

**Gotcha:** Result value type is `Optional<Order>`, not `Order` — `maxBy` always returns `Optional` because the downstream collector can't prove the group is non-empty. `.get()` works on this seed data but is a code smell — use `.orElseThrow()` to be explicit.

---

## Exercise 9: teeing — two collectors in one pass (Java 12+) (~10 min)
**Goal:** Collect into two collectors simultaneously and merge the results, without iterating the stream twice.

**Task:** Using `Collectors.teeing()`, compute in a single stream pass over all orders:
- The total number of orders (`counting()`).
- The sum of all order totals (`Collectors.summingDouble(o -> o.total().doubleValue())`).

Merge the two results into a summary string: `"Orders: 6, Revenue: 1989.0"`.

**Gotcha:** `teeing(collector1, collector2, mergeFunction)` is Java 12+. The merge function receives each downstream collector's result in the order you passed them — pass `(count, sum)` and the merge arguments are `(Long count, Double sum)`.

---

## Quick recall

**Q.** Why does `toMap` with non-unique keys throw `IllegalStateException` at runtime rather than a compile error?
**A.** The two-arg form has no merge function, so the collector fails when it encounters a second value for the same key. The collision is data-dependent — the compiler cannot predict it.

**Q.** What does `groupingBy` return when no elements match a group?
**A.** Nothing — that key is simply absent from the map. `partitioningBy` differs: it always emits both `true` and `false` keys.

**Q.** What is the type returned by `Collectors.counting()`?
**A.** `Long` (not `int` or `Integer`). Declare your map value type as `Long` or use `var`.

**Q.** What is the downstream collector in `groupingBy(classifier, downstream)`?
**A.** A second `Collector` applied to the elements within each group — e.g., `counting()`, `toList()`, `maxBy(...)`, `summingInt(...)`. It transforms the per-group list into whatever aggregate you need.

**Q.** How do you produce a `Map<String, Order>` where a later order overwrites an earlier one on duplicate key?
**A.** Use the three-arg `toMap(keyMapper, valueMapper, (existing, incoming) -> incoming)`. The merge function picks the incoming value, effectively discarding the earlier one.

**Q.** What does `collectingAndThen` do?
**A.** Wraps a collector with a finishing function that transforms the final result — e.g., converting a mutable list to unmodifiable, or converting `Long` count to `Integer`.

**Q.** What is `Collectors.teeing` and when would you use it?
**A.** (Java 12+) Feeds each stream element into two downstream collectors simultaneously and merges their results with a function. Use it when you need two aggregates (e.g., count + sum) without streaming the data twice.
