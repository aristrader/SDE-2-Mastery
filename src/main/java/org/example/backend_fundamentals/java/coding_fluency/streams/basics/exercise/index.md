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
