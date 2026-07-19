---
order: 10
search: false
---

# Advanced Collectors Practice

## Exercise: partitioningby-simple - partitioningBy

<!-- starter-code -->
```java
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PartitioningByPractice {
    public static void main(String[] args) {
        List<Order> orders = sampleOrders();
        List<Employee> employees = sampleEmployees();

        // Partition orders into paid and not-paid.
        // Partition employees by active flag and salary threshold.
    }

    static List<Order> sampleOrders() {
        return List.of(
            new Order("O1", OrderStatus.PAID, new BigDecimal("250.0")),
            new Order("O2", OrderStatus.NEW, new BigDecimal("120.0")),
            new Order("O3", OrderStatus.CANCELLED, new BigDecimal("75.0")),
            new Order("O4", OrderStatus.PAID, new BigDecimal("640.0")),
            new Order("O5", OrderStatus.NEW, new BigDecimal("414.0")),
            new Order("O6", OrderStatus.PAID, new BigDecimal("490.0"))
        );
    }

    static List<Employee> sampleEmployees() {
        return List.of(
            new Employee(1, "Asha", 120_000, true),
            new Employee(2, "Ben", 95_000, true),
            new Employee(3, "Chen", 145_000, false),
            new Employee(4, "Diya", 110_000, true),
            new Employee(5, "Evan", 85_000, false)
        );
    }

    enum OrderStatus { NEW, PAID, CANCELLED }
    record Order(String id, OrderStatus status, BigDecimal total) {}
    record Employee(long id, String name, double salary, boolean active) {}
}
```

### Goal
Split a stream into exactly two buckets.

### Task
Partition orders into paid and not-paid, producing `Map<Boolean, List<Order>>`.

Then use downstream `counting()` to get `Map<Boolean, Long>`.

Also using `Employee`:

1. Partition employees into active/inactive.
2. Partition employees into salary `>= 100000` and salary `< 100000`.
3. Count active vs inactive employees using `partitioningBy()`.

## Exercise: collectingandthen - collectingAndThen

<!-- starter-code -->
```java
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectingAndThenPractice {
    public static void main(String[] args) {
        List<Order> orders = List.of(
            new Order("O1", OrderStatus.PAID, new BigDecimal("250.0")),
            new Order("O2", OrderStatus.NEW, new BigDecimal("120.0")),
            new Order("O3", OrderStatus.CANCELLED, new BigDecimal("75.0")),
            new Order("O4", OrderStatus.PAID, new BigDecimal("640.0")),
            new Order("O5", OrderStatus.NEW, new BigDecimal("414.0")),
            new Order("O6", OrderStatus.PAID, new BigDecimal("490.0"))
        );

        // Collect paid order IDs into an unmodifiable list.
        // Group orders by status and convert counts from Long to Integer.
    }

    enum OrderStatus { NEW, PAID, CANCELLED }
    record Order(String id, OrderStatus status, BigDecimal total) {}
}
```

### Goal
Wrap a downstream collector with a finishing function.

### Task
Collect all paid order IDs into an unmodifiable list with `collectingAndThen(toList(), Collections::unmodifiableList)`.

Then group orders by status and convert `counting()` from `Long` to `Integer` using `Long::intValue`.

## Exercise: groupingby-maxby - groupingBy with maxBy downstream

<!-- starter-code -->
```java
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GroupingByMaxByPractice {
    public static void main(String[] args) {
        List<Order> orders = List.of(
            new Order("O1", OrderStatus.PAID, new BigDecimal("250.0")),
            new Order("O2", OrderStatus.NEW, new BigDecimal("120.0")),
            new Order("O3", OrderStatus.CANCELLED, new BigDecimal("75.0")),
            new Order("O4", OrderStatus.PAID, new BigDecimal("640.0")),
            new Order("O5", OrderStatus.NEW, new BigDecimal("414.0")),
            new Order("O6", OrderStatus.PAID, new BigDecimal("490.0"))
        );

        // Find highest-total order per status with groupingBy and maxBy.
    }

    enum OrderStatus { NEW, PAID, CANCELLED }
    record Order(String id, OrderStatus status, BigDecimal total) {}
}
```

### Goal
Find the maximum-value element within each group.

### Task
For each status, find the order with the highest total using `groupingBy` and `maxBy`.

### Checks
- The result value type is `Optional<Order>`.
- Use `orElseThrow()` instead of blind `.get()` when reading.

## Exercise: teeing-collectors - teeing

<!-- starter-code -->
```java
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class TeeingCollectorsPractice {
    public static void main(String[] args) {
        List<Order> orders = List.of(
            new Order("O1", new BigDecimal("250.0")),
            new Order("O2", new BigDecimal("120.0")),
            new Order("O3", new BigDecimal("75.0")),
            new Order("O4", new BigDecimal("640.0")),
            new Order("O5", new BigDecimal("414.0")),
            new Order("O6", new BigDecimal("490.0"))
        );

        // Use teeing() to compute count and revenue in one pass.
    }

    record Order(String id, BigDecimal total) {}
}
```

### Goal
Collect into two collectors simultaneously.

### Task
Use `Collectors.teeing()` to compute total order count and total revenue in one pass.

Merge into: `Orders: 6, Revenue: 1989.0`.
