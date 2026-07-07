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
Attempt to modify an existing element: `list.set(0, "X");`.
Attempt to add a new element: `list.add("D");`.

### Checks
- Which operation succeeds?
- Which operation throws an exception, and what is the exception?

## Exercise: list-of-immutability - Fully Immutable Lists

### Goal
Experience the strict immutability provided by the Java 9 `List.of()` factory methods.

### Task
Create a list using `List<String> list = List.of("A", "B", "C");`.
Attempt to modify an existing element: `list.set(0, "X");`.
Attempt to add a `null` element: `List.of("A", null);`.

### Checks
- Do any mutation operations succeed on a `List.of` instance?
- What happens when you try to pass `null` to the factory method?
