---
order: 60
---

# Streams Core — Coding Exercises

## Why this matters
Stream pipelines replace 90% of for-loops in modern Java services; reviewers flag imperative loops as a code smell.
`flatMap` over nested collections (e.g., order → line items) is a daily pattern in KYC and order-processing code.
Interviewers routinely ask candidates to rewrite a for-loop as a stream on the spot — fumbling laziness or single-use is an immediate red flag.

## Domain model

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

Seed data to paste into your runner:

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

## Exercise 1: Filter and map (~5 min)
**Goal:** Practice `filter()` chained with `map()` to project a subset of fields.

**Task:** From `orders`, produce a `List<String>` containing the IDs of every PAID order, in encounter order.

**Gotcha:** `filter()` and `map()` are intermediate — nothing executes until you call a terminal operation. Store `.filter(...).map(...)` in a variable without a terminal op and you get a `Stream<String>`, not a `List<String>`.

---

## Exercise 2: flatMap over nested collections (~8 min)
**Goal:** Collapse a two-level structure (orders → line items) into a single stream.

**Task:** From `orders`, collect every `LineItem` across all orders into a `List<LineItem>`. Then, in a separate pipeline, collect just the distinct `productId` strings (there are duplicates — `P1` and `P2` each appear twice).

**Gotcha:** `.map(Order::lineItems)` gives a `Stream<List<LineItem>>` — a stream of lists, not items. Use `flatMap(order -> order.lineItems().stream())`, then map to product ID and call `distinct()`.

---

## Exercise 3: reduce to sum totals (~5 min)
**Goal:** Aggregate values using `reduce()` instead of a mutable accumulator.

**Task:** Using `reduce()`, compute the sum of `total` across all PAID orders. Do not use `Collectors.summingDouble` or `mapToDouble().sum()` — use `reduce()` explicitly.

**Gotcha:** `reduce(identity, accumulator)` requires identity to be the neutral value for the operation — for `BigDecimal` addition that is `BigDecimal.ZERO`, not `null`. The single-arg `reduce()` returns `Optional`, so you must handle the empty case.

---

## Exercise 4: anyMatch / allMatch / noneMatch (~5 min)
**Goal:** Short-circuit matching instead of filtering and checking size.

**Task:**
1. Is there any PENDING order with a total above `300.00`?
2. Are all CANCELLED orders attributed to customer `C1`?
3. Are there no orders with a `null` customerId?

Write each as a single expression on `orders`. Do not collect into a list first.

**Gotcha:** These are terminal operations — they consume the stream. You cannot reuse the same `Stream<Order>` variable for all three; call each expression on the source `orders` list directly, or re-open the stream each time.

---

## Exercise 5: distinct and sorted with Comparator (~7 min)
**Goal:** De-duplicate and sort without mutating the source list.

**Task:**
1. Collect all `customerId` values (including duplicates) and then produce a sorted `List<String>` of distinct customer IDs, ascending alphabetically.
2. Produce a `List<Order>` of all orders sorted by `total` descending, then by `id` ascending as a tiebreaker.

**Gotcha:** `sorted()` on a `Stream<Order>` requires either `Order` to be `Comparable` (it isn't — it's a record with no natural order) or an explicit `Comparator`. Forget the `Comparator` and you get a `ClassCastException` at runtime, not a compile error.

---

## Exercise 6: peek for debugging (~3 min)
**Goal:** Inspect elements flowing through a pipeline without changing them.

**Task:** Add a `.peek(o -> System.out.println("before filter: " + o.id()))` before the filter in your Exercise 1 pipeline, and another `.peek(o -> System.out.println("after filter: " + o.id()))` between `filter` and `map`. Observe what prints.

**Gotcha:** `peek()` is intermediate and lazy — it only fires for elements that reach that stage. Short-circuit terminals (`findFirst`, `anyMatch`, `limit`) skip `peek` for elements past the cutoff. Never use `peek` in production for logic that must always run — it is a debugging-only tool.

---

## Exercise 7: findFirst and short-circuit operations (~5 min)
**Goal:** Return the first match without processing the whole stream.

**Task:**
1. Find the first PAID order whose total exceeds `500.00`. Return it as an `Optional<Order>`.
2. Sort orders by total descending and use `.findFirst()` to get the highest-value order.

**Gotcha:** `findFirst()`, `anyMatch()`, and `limit()` short-circuit — they stop as soon as their condition is satisfied. `findFirst()` returns `Optional<Order>`; call `.orElseThrow()` if a match must exist, or `.orElse(null)` / `.orElse(defaultOrder)` if the empty case is valid.

---

## Exercise 8: reduce — identity, accumulator, and combiner (~8 min)
**Goal:** Understand both the 2-arg and 3-arg forms of `reduce`, including the combiner used in parallel streams.

**Task:**
1. Use the 2-arg form `reduce(identity, accumulator)` to sum the totals of all PAID orders (same goal as Exercise 3, but now name the arguments explicitly in a comment).
2. Use the 3-arg form `reduce(identity, accumulator, combiner)` on a `parallelStream()` to sum the same totals. The combiner merges partial results from different threads.

**Gotcha:** The 3-arg form is only meaningful on parallel streams — on a sequential stream the combiner is never called. The combiner must be associative and compatible with the accumulator: if the accumulator adds `BigDecimal`s, the combiner must also add. A wrong combiner produces a wrong answer on parallel streams but a correct one on sequential — a subtle, hard-to-reproduce bug.

---

## Exercise 9: limit and skip for pagination (~5 min)
**Goal:** Simulate a page of results from a sorted stream.

**Task:** Sort all orders by total descending (same comparator as Exercise 5). Implement a helper that, given `pageNumber` (0-indexed) and `pageSize`, returns the correct `List<Order>` slice using `skip()` and `limit()`.

**Gotcha:** `skip(n)` and `limit(m)` are intermediate — apply them *after* `sorted()`. Skipping before sorting skips from an unordered stream and gives wrong results. `skip` takes a `long`, not `int`.

---

## Quick recall

**Q.** What happens if you call a terminal operation on a stream that has already been consumed?
**A.** `IllegalStateException: stream has already been operated upon or closed`. Streams are single-use; re-open from the source collection each time.

**Q.** What is the difference between intermediate and terminal operations?
**A.** Intermediate ops (e.g., `filter`, `map`, `flatMap`, `sorted`, `peek`) are lazy and return a new `Stream`. Terminal ops (e.g., `collect`, `reduce`, `anyMatch`, `findFirst`) trigger execution and close the stream.

**Q.** What is the difference between `map` and `flatMap`?
**A.** `map` applies a function that returns a value, wrapping it one-for-one. `flatMap` applies a function that returns a `Stream` and merges all those streams into one flat stream — use it to collapse nested collections.

**Q.** Why does `filter().map()` not execute immediately?
**A.** Both are intermediate (lazy) operations. The pipeline runs only when a terminal operation (`collect`, `reduce`, `anyMatch`, etc.) is called, and only processes as many elements as the terminal op needs.

**Q.** What is the identity element for `reduce`, and what is the combiner in the 3-arg form?
**A.** The identity is the neutral value (`BigDecimal.ZERO` for addition) such that `identity OP x == x`. The combiner (3-arg form only) merges partial results across threads in a parallel stream — it is never called on a sequential stream, so combiner bugs only surface under parallelism.

**Q.** How do you sort a stream of domain objects that don't implement `Comparable`?
**A.** Pass an explicit `Comparator` to `sorted()` — e.g., `Comparator.comparing(Order::total).reversed()`. Calling `sorted()` with no args on a non-`Comparable` type compiles fine but throws `ClassCastException` at runtime.

**Q.** What is `peek()` useful for, and when should you avoid it?
**A.** Debugging intermediate pipeline stages. Never use it for required side effects — it only fires for elements that reach that stage, and short-circuit ops can skip it entirely.
