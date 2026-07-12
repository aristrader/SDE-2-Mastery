---
order: 10
search: false
---

# joining Practice

## Exercise: joining-strings - joining

### Goal
Produce delimited strings from a stream.

### Task
1. Join all order IDs into `O1,O2,O3,O4,O5,O6`.
2. Join paid order IDs with delimiter ` | `, prefix `[`, suffix `]`.

### Checks
- You map `Order` to `String` before joining.
- The paid output is `[O1 | O4 | O6]`.

## Exercise: employee-joining - Employee Joining

### Goal
Join mapped employee fields.

### Task
Using `Employee`:

1. Join all employee names separated by commas.
2. Join all unique department names inside square brackets.

### Checks
- Map to a `String` field before calling `joining`.
- Use `distinct()` for unique departments.
