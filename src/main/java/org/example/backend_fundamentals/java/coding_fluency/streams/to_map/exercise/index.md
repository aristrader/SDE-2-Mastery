---
order: 10
search: false
---

# toMap Practice

## Exercise: tomap-unique - toMap unique keys

<!-- starter-code -->
```java
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ToMapUniquePractice {
    public static void main(String[] args) {
        List<Order> orders = List.of(
            new Order("O1", "C1", new BigDecimal("250.00")),
            new Order("O2", "C2", new BigDecimal("120.00")),
            new Order("O3", "C3", new BigDecimal("90.00"))
        );

        // Produce Map<String, Order> keyed by order ID.
    }

    record Order(String id, String customerId, BigDecimal total) {}
}
```

### Goal
Build a lookup map from unique keys.

### Task
Produce `Map<String, Order>` keyed by order ID.

### Checks
- Use `Function.identity()` for the value.
- Explain why duplicate IDs would fail.

## Exercise: tomap-duplicate - toMap duplicate key handler

<!-- starter-code -->
```java
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ToMapDuplicatePractice {
    public static void main(String[] args) {
        List<Order> orders = List.of(
            new Order("O1", "C1", new BigDecimal("250.00")),
            new Order("O2", "C2", new BigDecimal("120.00")),
            new Order("O3", "C1", new BigDecimal("90.00")),
            new Order("O4", "C3", new BigDecimal("40.00")),
            new Order("O5", "C2", new BigDecimal("75.00"))
        );

        // Build customerId -> summed total. C1 should be 340.00 and C2 should be 195.00.
    }

    record Order(String id, String customerId, BigDecimal total) {}
}
```

### Goal
Handle duplicate keys with a merge function.

### Task
Build `Map<String, BigDecimal>` keyed by `customerId`, where the value is the sum of all order totals for that customer.

Try the two-arg form first, then switch to the three-arg form.

### Checks
- Customer `C1` totals all its orders.
- The merge function adds, not overwrites.
