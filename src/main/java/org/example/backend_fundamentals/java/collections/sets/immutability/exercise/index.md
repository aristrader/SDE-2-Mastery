---
order: 10
search: false
---

# Practice

## Exercise: set-of-duplicates - Silent Dedupe vs Fail Fast

### Goal
Observe the difference in how `HashSet` and `Set.of()` handle duplicate inputs.

### Task
Create a `HashSet` using `new HashSet<>(List.of("A", "A", "B"))` and print its size.
Then try `Set.of("A", "A", "B")`.

### Checks
- What is the size of the `HashSet`?
- What exception is thrown by `Set.of`?

## Exercise: null-handling - Null Handling

### Objective
Understand null behavior.

### Task
Experiment with:

- `HashSet`
- `LinkedHashSet`
- `TreeSet`
- `Set.of`

Observe which accept null and which do not.
