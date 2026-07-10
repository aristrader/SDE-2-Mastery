---
order: 20
search: false
---

# Solutions

## Solution: shallow-immutability-trap - Shallow Immutability

```java
List<String> mutableList = new ArrayList<>();
mutableList.add("A");

Map<String, List<String>> map = Map.of("items", mutableList);
map.get("items").add("B"); // succeeds
```

`Map.of()` provides shallow immutability. It prevents changing which key points to which value, but the value object can still mutate. Use `List.copyOf()` before storing the value when the value must not change.

## Solution: map-of-mutation - Freezing Map Methods

```java
// map.computeIfAbsent("newKey", k -> new ArrayList<>());
// throws UnsupportedOperationException
```

`computeIfAbsent` writes a value when the key is absent, so it is a structural mutation and is disabled on immutable maps.
