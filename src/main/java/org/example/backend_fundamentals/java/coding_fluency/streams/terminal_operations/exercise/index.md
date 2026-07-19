---
order: 10
search: false
---

# Stream Terminal Operations Practice

## Exercise: terminal-operations - Consume a Stream

<!-- starter-code -->
```java
import java.util.List;
import java.util.Optional;

public class StreamTerminalOperationsPractice {
    public static void main(String[] args) {
        List<Employee> employees = List.of(
            new Employee(1, "Asha", 29, 120_000, "IT", true),
            new Employee(2, "Ben", 34, 95_000, "HR", true),
            new Employee(3, "Chen", 41, 210_000, "IT", true),
            new Employee(4, "Diya", 26, 110_000, "Finance", false),
            new Employee(5, "Evan", 38, 130_000, "HR", true),
            new Employee(6, "Farah", 17, 45_000, "Internship", true)
        );

        // Write each terminal operation and print the result.
    }

    record Employee(long id, String name, int age, double salary, String department, boolean active) {}
}
```

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
