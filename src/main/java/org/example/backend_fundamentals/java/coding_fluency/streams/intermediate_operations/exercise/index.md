---
title: Exercises
order: 10
search: false
---

# Stream Intermediate Operations Practice

## Exercise: intermediate-warmup - One Operation at a Time

<!-- starter-code -->
```java
import java.util.Comparator;
import java.util.List;

public class StreamIntermediateOperationsPractice {
    public static void main(String[] args) {
        List<Employee> employees = List.of(
            new Employee(1, "Asha", 29, 120_000, "Platform", true, List.of("Java", "Kafka")),
            new Employee(2, "Ben", 34, 95_000, "Payments", true, List.of("Java", "SQL")),
            new Employee(3, "Chen", 41, 145_000, "Platform", true, List.of("Kubernetes", "Docker")),
            new Employee(4, "Diya", 26, 110_000, "Growth", true, List.of("Spring", "Redis")),
            new Employee(5, "Evan", 38, 130_000, "Payments", true, List.of("Java", "AWS")),
            new Employee(6, "Farah", 31, 85_000, "Growth", false, List.of("Python", "Kafka")),
            new Employee(5, "Evan", 38, 130_000, "Payments", true, List.of("Java", "AWS"))
        );

        List<String> lines = List.of("Java Spring", "Kafka Redis", "Docker Kubernetes");
        int page = 1;
        int size = 2;

        // Write each stream expression here and print the result.
    }

    record Employee(
        long id,
        String name,
        int age,
        double salary,
        String department,
        boolean active,
        List<String> skills
    ) {}

    record EmployeeDto(long id, String name) {}
}
```

### Goal
Practice choosing the right intermediate operation before choosing the terminal operation.

### Task
Solve these with streams. Each task covers a different intermediate-operation decision.

1. Find active employees with salary greater than `100000`.
2. Convert active employees into `EmployeeDto`.
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
