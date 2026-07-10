---
order: 10
search: false
---

# Practice

## Exercise: arrays-aslist-trap - The Fixed-Size Trap

### Goal
Understand that `Arrays.asList()` returns a list that is mutable in content but fixed in size.

### Task
Create a list using `List<String> list = Arrays.asList("A", "B", "C");`.
Attempt to modify an existing element with `set`.
Attempt to add a new element with `add`.

### Checks
- Which operation succeeds?
- Which operation throws, and what is the exception?

## Exercise: list-of-immutability - Fully Immutable Lists

### Goal
Experience the stricter immutability provided by `List.of()`.

### Task
Create a list using `List<String> list = List.of("A", "B", "C");`.
Attempt to modify an existing element.
Attempt to create `List.of("A", null)`.

### Checks
- Do any mutation operations succeed?
- What happens when you pass `null`?
