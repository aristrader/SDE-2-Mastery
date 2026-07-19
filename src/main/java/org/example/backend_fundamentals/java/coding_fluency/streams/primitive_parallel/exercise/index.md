---
order: 10
search: false
---

# Primitive and Parallel Streams Practice

## Exercise: primitive-and-parallel - Numeric and Parallel Stream Choices

<!-- starter-code -->
```java
import java.util.ArrayList;
import java.util.List;

public class PrimitiveParallelStreamsPractice {
    public static void main(String[] args) {
        List<Employee> employees = List.of(
            new Employee(1, "Asha", 29, 120_000),
            new Employee(2, "Ben", 34, 95_000),
            new Employee(3, "Chen", 41, 145_000),
            new Employee(4, "Diya", 26, 110_000),
            new Employee(5, "Evan", 38, 130_000)
        );
        List<String> list = new ArrayList<>();

        // Calculate average salary and total age with primitive streams.
        // Rewrite the unsafe parallel collection without shared mutation.
    }

    record Employee(long id, String name, int age, double salary) {}
}
```

### Goal
Use primitive streams for numeric work and identify unsafe parallel stream patterns.

### Task
Using `Employee`:

1. Calculate average salary with a primitive stream.
2. Calculate total age with a primitive stream.
3. Explain why this is unsafe:

```java
employees.parallelStream()
         .forEach(list::add);
```

4. Rewrite it safely.
5. Decide whether `parallelStream()` is appropriate for HTTP calls per employee.

### Checks
- Use `mapToDouble` or `mapToInt`.
- Do not mutate shared collections from parallel streams.
- State that parallel streams are not a default performance upgrade.
