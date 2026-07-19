---
order: 10
search: false
---

# groupingBy Practice

## Exercise: employee-grouping - Employee groupingBy

<!-- starter-code -->
```java
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeeGroupingPractice {
    public static void main(String[] args) {
        List<Employee> employees = List.of(
            new Employee(1, "Asha", 29, 120_000, "Platform"),
            new Employee(2, "Ben", 34, 95_000, "Payments"),
            new Employee(3, "Chen", 41, 145_000, "Platform"),
            new Employee(4, "Diya", 26, 110_000, "Growth"),
            new Employee(5, "Evan", 38, 130_000, "Payments"),
            new Employee(6, "Farah", 29, 85_000, "Growth")
        );

        // Group by department, then add count, names, and average salary downstream collectors.
    }

    record Employee(long id, String name, int age, double salary, String department) {}
}
```

### Goal
Use `groupingBy` with the common downstream collectors.

### Task
Using `employees`:

1. Group employees by department.
2. Count employees department-wise.
3. Group employee names department-wise.
4. Find average salary department-wise.

### Checks
- Use default `groupingBy` when the value should be a list.
- Use downstream collectors for count, mapping, and average.
