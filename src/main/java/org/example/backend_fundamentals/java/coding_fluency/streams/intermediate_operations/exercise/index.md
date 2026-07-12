---
order: 10
search: false
---

# Stream Intermediate Operations Practice

Use the common `Employee` model with `id`, `name`, `age`, `salary`, `department`, `active`, and `skills`.

## Exercise: intermediate-warmup - One Operation at a Time

### Goal
Practice choosing the right intermediate operation before choosing the terminal operation.

### Task
Solve these with streams:

1. Find all even numbers.
2. Find all odd numbers.
3. Find employees whose salary is greater than `100000`.
4. Find active employees.
5. Remove all null values from a list.
6. Square every integer.
7. Convert every string to uppercase.
8. Extract employee names.
9. Extract employee salaries.
10. Convert `Employee` to `EmployeeDTO`.
11. Flatten a `List<List<Integer>>`.
12. Flatten employee skills into one list.
13. Split lines like `Java Spring`, `Kafka Redis`, `Docker Kubernetes` into one word list.
14. Flatten a list of comma-separated tags.
15. Sort integers ascending.
16. Sort integers descending.
17. Sort employees by salary.
18. Sort employees by department and then name.
19. Find the top 5 highest-paid employees.
20. Remove duplicate integers.
21. Remove duplicate strings.
22. Remove duplicate employees, assuming correct `equals()` and `hashCode()`.
23. Get the first 10 records.
24. Get the top 3 highest-paid employees.
25. Skip the first 5 records.
26. Implement pagination with `skip()` and `limit()`.
27. Log every employee before filtering.
28. Print every value before mapping.

### Checks
- Use `filter` only when the stream may get shorter.
- Use `map` only when each element produces one value.
- Use `flatMap` for nested or split values.
- Use `peek` only for debugging/logging.
