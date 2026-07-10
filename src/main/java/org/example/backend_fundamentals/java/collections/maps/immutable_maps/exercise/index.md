---
order: 10
search: false
---

# Practice

## Exercise: shallow-immutability-trap - Shallow Immutability

### Goal
Understand that `Map.of()` freezes the map structure, but does not freeze mutable value objects.

### Task
Create `List<String> mutableList = new ArrayList<>(); mutableList.add("A");`.
Create `Map<String, List<String>> map = Map.of("items", mutableList);`.
Attempt `map.get("items").add("B");`.

### Checks
- Did `add("B")` succeed?
- Why doesn't `Map.of` prevent this?

## Exercise: map-of-mutation - Freezing Map Methods

### Goal
Understand the breadth of methods that `Map.of()` disables.

### Task
Using the map from the previous exercise, try `map.computeIfAbsent("newKey", k -> new ArrayList<>());`.

### Checks
- What exception is thrown?
- Why does `computeIfAbsent` fail even if it looks like a read-first method?
