---
title: Exercises
order: 10
search: false
---

# Stream Intermediate Operations Practice

Use the common `Employee` model with `id`, `name`, `age`, `salary`, `department`, `active`, and `skills`.

## Exercise: intermediate-warmup - One Operation at a Time

### Goal
Practice choosing the right intermediate operation before choosing the terminal operation.

### Task
Solve these with streams. Each task covers a different intermediate-operation decision.

1. Find active employees with salary greater than `100000`.
2. Convert active employees into `EmployeeDTO`.
3. Flatten all employee skills into one unique skill list.
4. Split lines like `Java Spring`, `Kafka Redis`, `Docker Kubernetes` into one word list.
5. Sort employees by department and then name.
6. Find the top 5 highest-paid active employees.
7. Remove duplicate employees, assuming correct `equals()` and `hashCode()`.
8. Implement pagination with `skip()` and `limit()`.
9. Add debug logging before filtering without changing business objects.

### Checks
- Use `filter` only when the stream may get shorter.
- Use `map` only when each element produces one value.
- Use `flatMap` for nested or split values.
- Put `sorted()` before `limit()` when selecting top records.
- Use `peek` only for debugging/logging.
