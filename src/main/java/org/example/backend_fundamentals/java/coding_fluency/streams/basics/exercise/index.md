---
order: 10
search: false
---

# Stream Basics Practice

## Exercise: list-mutability - List Collector Mutability

<!-- starter-code -->
```java
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StreamBasicsListMutabilityPractice {
    public static void main(String[] args) {
        List<String> orderIds = List.of("O1", "O2", "O3");

        // Compare Collectors.toList(), Collectors.toCollection(ArrayList::new),
        // Stream.toList(), and Collectors.toUnmodifiableList().
    }
}
```

### Goal
Compare specified and unspecified stream-result contracts.

### Task
Collect order IDs using:

- `Collectors.toList()`
- `Collectors.toCollection(ArrayList::new)`
- `Stream.toList()`
- `Collectors.toUnmodifiableList()`

Explain the mutability guarantee for each. Attempt a mutation only where the contract makes its result predictable.

### Checks
- Identify that `Collectors.toList()` does not promise mutability or implementation.
- Use `toCollection(ArrayList::new)` when a mutable result is required.
- Explain why unmodifiable results are often safer, and why the two unmodifiable APIs differ for null elements.

## Exercise: set-deduplication - Set Collector Deduplication

<!-- starter-code -->
```java
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StreamBasicsSetDeduplicationPractice {
    public static void main(String[] args) {
        List<String> customerIds = List.of("C1", "C2", "C1", "C3", "C2", "C4");

        // Collect customer IDs into a Set<String> and print it.
    }
}
```

### Goal
Use a set collector to dedupe stream values.

### Task
Collect customer IDs into a `Set<String>`.

### Checks
- Duplicate customer IDs appear once.
- You do not depend on iteration order.
