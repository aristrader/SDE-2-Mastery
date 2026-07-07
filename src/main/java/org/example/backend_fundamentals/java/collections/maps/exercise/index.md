---
order: 10
search: false
---

# Practice

## Exercise: shallow-immutability-trap - Shallow Immutability

### Goal
Understand that `Map.of()` freezes the map structure, but does not freeze mutable value objects.

### Task
Create a `List<String> mutableList = new ArrayList<>(); mutableList.add("A");`.
Create a map: `Map<String, List<String>> map = Map.of("items", mutableList);`.
Attempt to add an item to the list through the map: `map.get("items").add("B");`.
Print the map.

### Checks
- Did the `add("B")` operation succeed? Why doesn't `Map.of` prevent this?

## Exercise: map-of-mutation - Freezing Map Methods

### Goal
Understand the breadth of methods that `Map.of()` disables.

### Task
Using the map from the previous exercise, try to safely calculate a new value using `map.computeIfAbsent("newKey", k -> new ArrayList<>());`.

### Checks
- What exception is thrown? 
- Why does `computeIfAbsent` fail even if you think you are just "reading" a frozen map?
