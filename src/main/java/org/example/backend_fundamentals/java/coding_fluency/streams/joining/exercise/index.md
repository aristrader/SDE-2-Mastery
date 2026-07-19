---
order: 10
search: false
---

# joining Practice

## Exercise: joining-strings - joining

<!-- starter-code -->
```java
import java.util.List;
import java.util.stream.Collectors;

public class JoiningStringsPractice {
    public static void main(String[] args) {
        List<Order> orders = List.of(
            new Order("O1", true),
            new Order("O2", false),
            new Order("O3", false),
            new Order("O4", true),
            new Order("O5", false),
            new Order("O6", true)
        );

        // Join all order IDs.
        // Join only paid order IDs with delimiter, prefix, and suffix.
    }

    record Order(String id, boolean paid) {}
}
```

### Goal
Produce delimited strings from a stream.

### Task
1. Join all order IDs into `O1,O2,O3,O4,O5,O6`.
2. Join paid order IDs with delimiter ` | `, prefix `[`, suffix `]`.

### Checks
- You map `Order` to `String` before joining.
- The paid output is `[O1 | O4 | O6]`.

## Exercise: employee-joining - Employee Joining

<!-- starter-code -->
```java
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeJoiningPractice {
    public static void main(String[] args) {
        List<Employee> employees = List.of(
            new Employee(1, "Asha", "Platform"),
            new Employee(2, "Ben", "Payments"),
            new Employee(3, "Chen", "Platform"),
            new Employee(4, "Diya", "Growth"),
            new Employee(5, "Evan", "Payments")
        );

        // Join all names with commas.
        // Join unique department names inside square brackets.
    }

    record Employee(long id, String name, String department) {}
}
```

### Goal
Join mapped employee fields.

### Task
Using `Employee`:

1. Join all employee names separated by commas.
2. Join all unique department names inside square brackets.

### Checks
- Map to a `String` field before calling `joining`.
- Use `distinct()` for unique departments.
