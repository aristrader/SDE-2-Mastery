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
