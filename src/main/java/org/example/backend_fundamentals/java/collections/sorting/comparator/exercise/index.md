---
order: 10
search: false
---

# Practice

## Exercise: simple-comparators - Single-field Comparator sorting

### Objective
Create external one-field orderings without changing `compareTo()`.

### Task
Create and use three comparators:

- `name` ascending
- `cgpa` descending
- `id` descending

### Checks
- Each comparator sorts correctly.
- No comparator uses `thenComparing`; save chaining for the chaining exercise.

## Exercise: comparator-chaining - Chain multiple comparator rules

### Objective
Build one multi-field comparator with utilities only.

### Task
Sort by:

1. `cgpa` descending
2. `name` ascending
3. `id` ascending

### Checks
- Use `comparing`, `thenComparing`, and primitive variants.
- Verify each tie-breaker works.

## Exercise: employee-comparator-strategy - Employee comparator strategy

### Objective
Choose `Comparator` when many business orderings exist.

### Task
Create an `Employee` class with `id`, `name`, `salary`, `age`, and `department`.
Create separate comparators for salary, age, department, and department then salary then name.
Do not implement `Comparable`.
