---
order: 10
search: false
---

# Stream Basics Practice

## Exercise: list-mutability - List Collector Mutability

<!-- starter-code -->
```java
import java.util.List;
import java.util.stream.Collectors;

public class StreamBasicsListMutabilityPractice {
    public static void main(String[] args) {
        List<String> orderIds = List.of("O1", "O2", "O3");

        // Collect order IDs using Collectors.toList(), Stream.toList(), and Collectors.toUnmodifiableList().
        // Try adding "O4" to each result and print what happens.
    }
}
```

### Goal
Compare mutable and unmodifiable stream results.

### Task
Collect order IDs using:

- `Collectors.toList()`
- `Stream.toList()`
- `Collectors.toUnmodifiableList()`

Try adding one more ID to each result.

### Checks
- Identify which result allows mutation.
- Explain why returning unmodifiable results is often safer.

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
