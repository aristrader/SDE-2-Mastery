---
order: 10
search: false
---

# Practice

## Exercise: set-of-duplicates - Silent Dedupe vs Fail Fast

### Goal
Observe the difference in how `HashSet` and `Set.of()` handle duplicate inputs.

### Task
Create a `HashSet` using a list with duplicates: `new HashSet<>(List.of("A", "A", "B"));`. Print its size.
Create an immutable set using the factory method with duplicates: `Set.of("A", "A", "B");`.

### Checks
- What is the size of the `HashSet`? Did it warn you about the duplicate?
- What exception is thrown by `Set.of`? Why is throwing an exception often better for literal collections?

## Exercise: linked-hash-set - Preserving Insertion Order

### Goal
See how `LinkedHashSet` solves the unpredictable iteration order of a regular `HashSet`.

### Task
Create a `HashSet<Integer>`. Add the numbers `10`, `1`, `5`, and `20`. Print the set.
Create a `LinkedHashSet<Integer>`. Add the same numbers in the same order. Print the set.

### Checks
- Which set prints the elements exactly in the order you inserted them (`[10, 1, 5, 20]`)?
- Which set prints them in a seemingly random bucket order?
