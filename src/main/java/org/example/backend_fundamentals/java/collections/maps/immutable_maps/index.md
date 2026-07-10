---
order: 30
---

# Immutable Maps

Immutable maps are useful for small read-only lookup tables: status labels, fixed config, enum-like mappings, and test fixtures.

```java
Map<String, Integer> priorities = Map.of(
    "LOW", 1,
    "MEDIUM", 2,
    "HIGH", 3
);
```

`Map.of(...)` creates an unmodifiable map. You cannot add, remove, replace, merge, or compute values after creation.

## Factory choices

| Factory | Use when |
| --- | --- |
| `Map.of()` | Empty map |
| `Map.of(k1, v1, ...)` | Small map with up to 10 entries |
| `Map.ofEntries(...)` | More than 10 entries |
| `Map.copyOf(existing)` | Immutable copy of another map |

```java
Map<String, Integer> many = Map.ofEntries(
    Map.entry("A", 1),
    Map.entry("B", 2),
    Map.entry("C", 3)
);
```

## Disabled mutations

All structural mutation methods throw `UnsupportedOperationException`:

```java
Map<String, Integer> map = Map.of("A", 1);

map.put("B", 2);                  // throws
map.remove("A");                  // throws
map.computeIfAbsent("B", k -> 2); // throws
map.merge("A", 1, Integer::sum);  // throws
```

`computeIfAbsent` and `merge` look like read-first APIs, but they may write. On an immutable map, that possibility is enough for them to fail.

## Shallow immutability

`Map.of` freezes the map structure, not the objects stored inside it.

```java
List<String> list = new ArrayList<>();
list.add("A");

Map<String, List<String>> map = Map.of("items", list);
map.get("items").add("B"); // succeeds
```

The key still points to the same list, but the list itself is mutable.

For a safer snapshot, copy mutable values before storing them:

```java
Map<String, List<String>> map = Map.of("items", List.copyOf(list));
```

Now callers cannot mutate the list through the map, and later changes to the original list are not reflected.

## Null policy

`Map.of` rejects null keys and null values:

```java
Map.of("A", null); // NullPointerException
Map.of(null, 1);   // NullPointerException
```

This keeps lookup semantics simple: `get(key) == null` means absent.

## Quick recall

- **Can you mutate `Map.of`?** No.
- **Does `Map.of` deeply freeze values?** No, it is shallow.
- **More than 10 entries?** Use `Map.ofEntries`.
- **Need immutable snapshot of an existing map?** `Map.copyOf`.
- **Null keys/values allowed?** No.
