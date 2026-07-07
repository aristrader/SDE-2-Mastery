---
order: 70
---

# Stream Terminal Operations — Collectors

Streams turn a sequence of elements into a result via a *terminal operation*. The most expressive terminal is `Stream.collect(Collector)` — a `Collector` defines how to accumulate the stream into a container (List, Set, Map, String, count, sum, …). Knowing which collector to reach for replaces a lot of manual loops with a one-liner.

This doc focuses on the collector family — the part of the Streams API that dovetails with the Collections work in the previous step. Companion demos sit alongside in `basics/`, `grouping/`, `to_map/`, and `joining/`.

> **Read alongside:** the Collections trio (Lists / Maps / Sets) — collectors produce those types with the same trade-offs (mutable vs unmodifiable, dedup, duplicate-key handling).

---

## The collectors you'll use most

| Collector | Produces | Notes |
| --- | --- | --- |
| `Collectors.toList()` | Mutable `ArrayList` | Default. Available since Java 8. |
| `Collectors.toUnmodifiableList()` | Unmodifiable `List` | Java 10+. Like `List.of()` but from a stream. |
| `Stream.toList()` | Unmodifiable `List` | Java 16+. Shorter than `collect(toUnmodifiableList())`. Prefer this on Java 16+. |
| `Collectors.toSet()` | Mutable `HashSet` | Dedupes during collection; no order guarantee. |
| `Collectors.toUnmodifiableSet()` | Unmodifiable `Set` | Java 10+. |
| `Collectors.toMap(keyFn, valueFn)` | Mutable `HashMap` | Throws `IllegalStateException` on duplicate keys. Use the three-arg overload with a merge function to combine. |
| `Collectors.groupingBy(classifier)` | `Map<K, List<V>>` | Groups elements by a function. Replaces manual `computeIfAbsent` loops. |
| `Collectors.groupingBy(classifier, downstream)` | `Map<K, R>` where `R` is the downstream result | Use with `counting()`, `summingInt`, `averagingInt`, `mapping`, `toSet`, etc. |
| `Collectors.joining(delimiter)` | `String` | Concatenates a stream of strings. Also has `(delimiter, prefix, suffix)` overload. |
| `Collectors.counting()` | `Long` | Counts elements. Usually used as a downstream collector inside `groupingBy`. |

---

## `toList()` vs `Stream.toList()` — pick by mutability

- **`Collectors.toList()`** returns a *mutable* `ArrayList`. Available since Java 8.
- **`Stream.toList()`** returns an *unmodifiable* `List`. Available since Java 16.

Choose based on whether the caller will mutate the result. For "give me the values, I'll never change them" — `Stream.toList()` (or `Collectors.toUnmodifiableList()` on Java 10–15). For "I need to keep adding to this later" — `Collectors.toList()`.

See `basics/ToListVsStreamToListRun` for the live mutability check.

---

## `groupingBy` — the most powerful collector

Replaces the most common manual map-accumulation loop:

```java
// Manual — verbose, easy to getValue wrong
Map<String, List<Employee>> byDept = new HashMap<>();
for (Employee e : employees) {
    byDept.computeIfAbsent(e.department(), k -> new ArrayList<>()).add(e);
}

// groupingBy — one line
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::department));
```

Both produce `Map<String, List<Employee>>`. The `groupingBy` form is shorter, intent-revealing, and harder to get wrong — no chance of forgetting `computeIfAbsent` and overwriting earlier entries with `put`.

### Downstream collectors — counting, summing, averaging, mapping

The two-argument `groupingBy(classifier, downstream)` runs a second collector against each group instead of collecting the group's elements into a list. This is how you produce per-group counts, sums, averages, sets, transformed lists — all in one stream pass:

```java
// Count per department
Map<String, Long> count = employees.stream()
    .collect(Collectors.groupingBy(Employee::department, Collectors.counting()));

// Total salary per department
Map<String, Integer> total = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.summingInt(Employee::salary)));

// Average salary per department
Map<String, Double> avg = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.averagingInt(Employee::salary)));

// Names per department (extract a field per element with mapping(...))
Map<String, List<String>> names = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.mapping(Employee::name, Collectors.toList())));
```

The downstream collector composes — anywhere a collector goes, you can plug in another one (`mapping(..., toSet())`, `mapping(..., joining(", "))`, etc.).

See `grouping/GroupingByBasicsRun` and `grouping/GroupingByDownstreamRun` for live examples.

---

## `toMap` — and the duplicate-key trap

`Collectors.toMap(keyFn, valueFn)` builds a map by extracting a key and a value from each element. It requires keys to be unique:

```java
// One employee per name — works
Map<String, Integer> salaryByName = employees.stream()
    .collect(Collectors.toMap(Employee::name, Employee::salary));
```

If two elements produce the same key, the two-arg form throws `IllegalStateException` — deliberate fail-fast, since silent overwrite usually hides a bug.

To handle duplicates, pass a **merge function** as the third argument that decides how to combine values when keys collide:

```java
// Sum salaries when departments collide
Map<String, Integer> totalByDept = employees.stream()
    .collect(Collectors.toMap(
        Employee::department,
        Employee::salary,
        Integer::sum));

// Keep the maximum on collision
Map<String, Integer> maxByDept = employees.stream()
    .collect(Collectors.toMap(
        Employee::department,
        Employee::salary,
        Math::max));
```

**`toMap` vs `groupingBy`:** different problems. One key → many values (a list per key) is `groupingBy`. One key → one merged value (a sum, a max, a concat) is `toMap` with a merge function. Reaching for `toMap` when you wanted grouping forces you to merge values prematurely.

See `to_map/ToMapBasicsRun` and `to_map/ToMapDuplicateKeyTrapRun` for live examples.

---

## `joining` — readable string assembly

```java
String csv = fruits.stream().collect(Collectors.joining(", "));
// "apple, banana, cherry"

String wrapped = fruits.stream().collect(Collectors.joining(", ", "[", "]"));
// "[apple, banana, cherry]"
```

Replaces the `StringBuilder` + first-flag loop entirely. Three overloads:

- `joining()` — concatenate, no separator.
- `joining(delimiter)` — separated by `delimiter`.
- `joining(delimiter, prefix, suffix)` — also wrapped in `prefix` / `suffix`. Prefix and suffix are *always* emitted, even on an empty stream — `joining(", ", "[", "]")` on empty input gives `"[]"`.

See `joining/JoiningBasicsRun`.

---

## Demo code in this folder

| Demo | Shows |
| --- | --- |
| `basics/ToListVsStreamToListRun` | `Collectors.toList()` is mutable; `Stream.toList()` and `Collectors.toUnmodifiableList()` reject mutation |
| `basics/ToSetBasicsRun` | `Collectors.toSet()` dedupes during collection; `Collectors.toUnmodifiableSet()` is also immutable |
| `grouping/GroupingByBasicsRun` | Manual `computeIfAbsent` loop side-by-side with the equivalent `groupingBy` one-liner |
| `grouping/GroupingByDownstreamRun` | `groupingBy` + `counting()` / `summingInt` / `averagingInt` / `mapping` — all in one stream pass |
| `to_map/ToMapBasicsRun` | Basic `toMap(keyFn, valueFn)` and the `Function.identity()` idiom for "whole element as value" |
| `to_map/ToMapDuplicateKeyTrapRun` | Two-arg `toMap` throws on duplicate keys; three-arg overload with `Integer::sum` / `Math::max` / first-wins fixes |
| `joining/JoiningBasicsRun` | Three `joining` overloads + the always-emitted prefix/suffix on empty streams + a manual `StringBuilder` comparison |

---

## Quick recall

- **`Collectors.toList()` vs `Stream.toList()`?** `Collectors.toList()` returns a mutable `ArrayList`; `Stream.toList()` (Java 16+) returns an unmodifiable list. Prefer `Stream.toList()` when the caller won't mutate.
- **Why does two-arg `toMap` throw on duplicate keys?** Deliberate fail-fast — silent overwrite hides bugs. Use the three-arg overload with a merge function (`Integer::sum`, `Math::max`, `(a, b) -> a`) to combine.
- **When `groupingBy` vs `toMap`?** `groupingBy` for one-key-to-many (a list per key); `toMap` for one-key-to-one with a merge function when collisions are expected.
- **What does `Collectors.mapping(...)` do?** Transforms elements before they reach the downstream collector — e.g., `groupingBy(dept, mapping(Employee::name, toList()))` produces names per department instead of full employees per department.
- **`Collectors.toUnmodifiableList()` vs `Stream.toList()`?** Same outcome (unmodifiable list); `Stream.toList()` is the Java 16+ shorter form, `toUnmodifiableList()` is the Java 10–15 form.
- **What does `joining(", ", "[", "]")` produce on an empty stream?** `"[]"` — prefix and suffix are always emitted.

---

## Related topics

- **Lists, Maps, Sets** — collectors produce these types; the same mutability and dedup trade-offs apply.
- **Hashing** — `toMap` and `groupingBy` produce hash-backed maps; the `equals`/`hashCode` contract on keys matters.
- **Functional interfaces and lambdas** — `keyFn`, `valueFn`, `classifier`, `mergeFunction` are all functional-interface arguments. Separate Java Foundations topic, worth a refresher if these feel opaque.



## Why this matters
`Collectors.groupingBy` is the most common stream operation in production service code — grouping transactions by status, user by country, events by type.
`toMap` is an interview trap: it looks simple but throws at runtime on duplicate keys unless you supply a merge function.
Knowing `partitioningBy`, downstream collectors like `counting()`, and `maxBy` separates candidates who have read the docs from those who have used them.

---

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


## Common Gotchas

- `groupingBy` produces a `List` value by default. Using `toMap(Order::status, ...)` instead collides on the second PAID order and throws `IllegalStateException`. This exercise is about `groupingBy`, not `toMap`.
- `Collectors.counting()` returns `Long`, not `Integer`. A map declared with `Integer` value type won't compile. Signature is `groupingBy(classifier, downstream)` — don't swap parameter order.
- The two-arg `toMap(keyMapper, valueMapper)` throws `IllegalStateException` if two elements map to the same key. Write the two-arg form here; Exercise 4 covers the collision case.
- The three-arg `toMap(keyMapper, valueMapper, mergeFunction)` resolves collisions by calling `mergeFunction.apply(existing, incoming)`. Wrong argument order (subtracting instead of adding) produces a wrong but non-crashing result that's easy to miss.
- `Collectors.joining()` works on `Stream<String>` only — forget to `.map(Order::id)` before `.collect(joining(...))` and you get a compile error. The three-arg overload is `joining(delimiter, prefix, suffix)` — argument order matters.
- `partitioningBy` always produces both keys (`true` and `false`), even if one partition is empty. `groupingBy` on a boolean classifier may omit keys for empty groups — reach for `partitioningBy` when both keys must be present.
- `Collectors.counting()` returns `Long`. `collectingAndThen(counting(), Long::intValue)` converts the per-group count to `int` — the idiomatic way to change a downstream result's type without a separate `.entrySet().stream()` pass.
- Result value type is `Optional<Order>`, not `Order` — `maxBy` always returns `Optional` because the downstream collector can't prove the group is non-empty. `.get()` works on this seed data but is a code smell — use `.orElseThrow()` to be explicit.
- `teeing(collector1, collector2, mergeFunction)` is Java 12+. The merge function receives each downstream collector's result in the order you passed them — pass `(count, sum)` and the merge arguments are `(Long count, Double sum)`.
