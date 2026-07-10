---
order: 20
search: false
---

# Solutions

## Solution: arrays-aslist-trap - The Fixed-Size Trap

```java
List<String> list = Arrays.asList("A", "B", "C");
list.set(0, "X"); // works: [X, B, C]
// list.add("D"); // UnsupportedOperationException
```

`Arrays.asList` is backed by an array. Existing slots can change, but the size cannot.

## Solution: list-of-immutability - Fully Immutable Lists

```java
List<String> list = List.of("A", "B", "C");
// list.set(0, "X"); // UnsupportedOperationException
// list.add("D");    // UnsupportedOperationException
// List.of("A", null); // NullPointerException
```

`List.of` creates an unmodifiable list and rejects nulls at construction.
