---
order: 10
search: false
---

# Stream Terminal Operations Practice

Use the common `Employee` model.

## Exercise: terminal-operations - Consume a Stream

### Goal
Pick the terminal operation that directly expresses the required result.

### Task
Solve these:

1. Count active employees.
2. Find the first employee from HR.
3. Find any employee from IT.
4. Check if any employee earns more than `200000`.
5. Check if all employees are active.
6. Check if no employee is under 18.
7. Find total salary using `reduce()`.
8. Find maximum salary using `reduce()`.
9. Concatenate all employee names using `reduce()`.

### Checks
- Use `Optional` safely for find/reduce results.
- Prefer match operations over `filter().count()` for boolean questions.
