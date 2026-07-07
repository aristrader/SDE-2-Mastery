---
order: 20
search: false
---

# Solutions

## Solution: arrays-aslist-trap - The Fixed-Size Trap

```java
List<String> list = Arrays.asList("A", "B", "C");
list.set(0, "X"); // Succeeds. The list is now [X, B, C]
// list.add("D"); // Throws UnsupportedOperationException
```
`Arrays.asList` returns a lightweight wrapper directly over a backing array. Because Java arrays cannot change their length, you cannot add or remove elements. However, you *can* mutate the existing slots using `.set()`. 

## Solution: list-of-immutability - Fully Immutable Lists

```java
List<String> list = List.of("A", "B", "C");
// list.set(0, "X"); // Throws UnsupportedOperationException
// list.add("D");    // Throws UnsupportedOperationException

// List.of("A", null); // Throws NullPointerException at construction
```
`List.of()` returns a genuinely immutable collection. All mutation operations (add, remove, set, clear) throw `UnsupportedOperationException`. Furthermore, it proactively guards against `null` values by throwing a `NullPointerException` immediately at construction if any element is null.
