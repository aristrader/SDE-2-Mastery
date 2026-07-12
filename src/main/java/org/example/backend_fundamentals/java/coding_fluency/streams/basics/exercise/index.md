---
order: 10
search: false
---

# Stream Basics Practice

## Exercise: list-mutability - List Collector Mutability

### Goal
Compare mutable and unmodifiable stream results.

### Task
Collect order IDs using:

- `Collectors.toList()`
- `Stream.toList()`
- `Collectors.toUnmodifiableList()`

Try adding one more ID to each result.

### Checks
- Identify which result allows mutation.
- Explain why returning unmodifiable results is often safer.

## Exercise: set-deduplication - Set Collector Deduplication

### Goal
Use a set collector to dedupe stream values.

### Task
Collect customer IDs into a `Set<String>`.

### Checks
- Duplicate customer IDs appear once.
- You do not depend on iteration order.

## Exercise: collector-basics - Basic Collectors

### Goal
Choose simple collectors for lists and sets.

### Task
Using `Employee`:

1. Collect all active employees into a list.
2. Find all unique departments.

### Checks
- Use `filter(...).toList()` or `collect(toList())` for the active employees.
- Use `map(Employee::department)` before collecting departments into a set.
