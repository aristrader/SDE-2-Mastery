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
map.get("items").add("B"); // Succeeds!

System.out.println(map); // Prints {items=[A, B]}
```
`Map.of()` provides *shallow immutability*. It prevents you from changing which key points to which value (the structure of the map). However, if the value itself is a mutable object (like an `ArrayList`), anyone with a reference to it can still mutate its internal state. For deep immutability, you must wrap the value in `List.copyOf()` before putting it in the map.

## Solution: map-of-mutation - Freezing Map Methods

```java
// map.computeIfAbsent("newKey", k -> new ArrayList<>()); 
// Throws UnsupportedOperationException
```
Every single method that *could* mutate the map throws `UnsupportedOperationException` immediately when called on a `Map.of()` instance. `computeIfAbsent` attempts to put a value if the key is absent, which is a structural mutation, so the method is completely disabled on immutable maps.
