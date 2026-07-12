---
order: 80
---

# Immutable Collections

Immutable collection factories create collections that reject structural changes. They are useful for configuration, lookup tables, safe return values, and defensive copies.

```java
List<String> roles = List.of("ADMIN", "REVIEWER");
Set<String> countries = Set.of("IN", "US", "SG");
Map<String, Integer> limits = Map.of("LOW", 10, "HIGH", 100);
```

Trying to add, remove, or replace entries throws `UnsupportedOperationException` at runtime.

## Factory methods

| API | Use it for | Notes |
| --- | --- | --- |
| `List.of(...)` | small immutable lists | rejects nulls |
| `Set.of(...)` | small immutable sets | rejects nulls and duplicates |
| `Map.of(...)` | up to 10 entries | rejects null keys/values and duplicate keys |
| `Map.ofEntries(...)` | 11+ map entries | use `Map.entry(k, v)` |
| `List.copyOf(...)` | immutable snapshot | rejects nulls |
| `Set.copyOf(...)` | immutable snapshot set | duplicate inputs collapse |
| `Map.copyOf(...)` | immutable snapshot map | rejects nulls |

The implementation classes are JDK internals. Do not check `instanceof ArrayList` or depend on class names like `ListN`.

## Snapshot vs view

`List.copyOf` creates an independent immutable snapshot.

```java
List<String> source = new ArrayList<>(List.of("A", "B"));
List<String> snapshot = List.copyOf(source);

source.add("C");

System.out.println(snapshot); // [A, B]
```

`Collections.unmodifiableList` creates an unmodifiable view over a backing list.

```java
List<String> source = new ArrayList<>(List.of("A", "B"));
List<String> view = Collections.unmodifiableList(source);

source.add("C");

System.out.println(view); // [A, B, C]
```

The view cannot be mutated through `view.add(...)`, but it still reflects mutations made through `source`.

## `Arrays.asList` trap

`Arrays.asList(array)` returns a fixed-size list backed by the array.

```java
String[] values = {"A", "B"};
List<String> list = Arrays.asList(values);

values[0] = "X";

System.out.println(list); // [X, B]
```

It allows `set`, but not `add` or `remove`.

## Nulls, duplicates, and ordering

`List.of(null)`, `Set.of(null)`, and `Map.of("a", null)` throw `NullPointerException` during construction.

`Set.of("A", "A")` throws `IllegalArgumentException` because duplicates are rejected eagerly.

Iteration order for `Set.of` and `Map.of` is not a contract. If order matters, choose an ordered collection explicitly before making a defensive copy or expose a `List`.

## Shallow immutability

An immutable collection prevents changing the collection structure. It does not freeze mutable elements inside it.

```java
List<StringBuilder> values = List.of(new StringBuilder("A"));
values.get(0).append("B"); // allowed
```

Use immutable element types when the full object graph must be immutable.

## Quick recall

- **Mutation exception?** `UnsupportedOperationException`.
- **Does `Map.of` allow nulls?** No.
- **`Map.of` entry limit?** 10 key-value pairs; use `Map.ofEntries` after that.
- **`List.copyOf` vs `unmodifiableList`?** Snapshot vs view.
- **`Arrays.asList` backed by array?** Yes.
- **`Set.of("a", "a")`?** Throws `IllegalArgumentException`.
- **Deeply immutable elements?** Not guaranteed.
