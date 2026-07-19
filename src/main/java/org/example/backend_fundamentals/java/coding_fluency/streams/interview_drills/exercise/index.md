---
order: 10
search: false
---

# Stream Interview Drills Practice

## Exercise: output-prediction - Predict Stream Output

<!-- starter-code -->
```java
import java.util.List;

public class StreamOutputPredictionPractice {
    public static void main(String[] args) {
        System.out.println(List.of(1, 2, 3, 4).stream().filter(x -> x % 2 == 0).map(x -> x * 10).toList());
        System.out.println(List.of("a", "b", "a", "c").stream().distinct().sorted().toList());
        System.out.println(List.of(1, 2, 3, 4, 5).stream().skip(2).limit(2).toList());
        System.out.println(List.of(List.of(1, 2), List.of(3, 4)).stream().flatMap(List::stream).toList());
        System.out.println(List.of(2, 4, 6, 8).stream().allMatch(x -> x % 2 == 0));
        System.out.println(List.of(2, 4, 5, 8).stream().noneMatch(x -> x % 2 == 0));
    }
}
```

### Goal
Mentally execute stream pipelines without running first.

### Task
Predict the output of each line in the starter code.

### Checks
- Explain where `filter`, `map`, `distinct`, `sorted`, `skip`, `limit`, `flatMap`, `allMatch`, and `noneMatch` affect the result.

## Exercise: find-stream-bugs - Find the Bug

<!-- starter-code -->
```java
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindStreamBugsPractice {
    public static void main(String[] args) {
        List<Employee> employees = List.of(
            new Employee(1, "Asha", 120_000),
            new Employee(2, "Ben", 95_000),
            new Employee(2, "Ben Duplicate", 105_000)
        );
        List<Employee> list = new ArrayList<>();
        Stream<Employee> stream = employees.stream();

        // Recreate each buggy pattern, then write the safe version below it.
    }

    record Employee(long id, String name, double salary) {
        long getId() {
            return id;
        }
    }
}
```

### Goal
Recognize stream bugs interviewers commonly probe.

### Task
Explain what is wrong and write the safe version:

1. `Collectors.toMap(Employee::getId, Function.identity())` when duplicate IDs exist.
2. Reusing a stream after `stream.count()`.
3. `employees.stream().findFirst().get()`.
4. `employees.parallelStream().forEach(list::add)`.
5. `.peek(e -> e.setSalary(100000))`.

## Exercise: integrated-review - Integrated Stream Review

<!-- starter-code -->
```java
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IntegratedStreamReviewPractice {
    public static void main(String[] args) {
        List<Employee> employees = List.of(
            new Employee(1, "Asha", 120_000, "IT", true, List.of("Java", "Spring")),
            new Employee(2, "Ben", 95_000, "HR", true, List.of("Excel", "SQL")),
            new Employee(3, "Chen", 210_000, "IT", true, List.of("Java", "Kafka")),
            new Employee(4, "Diya", 110_000, "Finance", false, List.of("SQL", "Python")),
            new Employee(5, "Evan", 130_000, "IT", true, List.of("Java", "AWS")),
            new Employee(6, "Farah", 85_000, "HR", true, List.of("Recruiting", "Excel")),
            new Employee(7, "Gopal", 150_000, "Platform", true, List.of("Java", "Kubernetes")),
            new Employee(8, "Hina", 140_000, "Platform", false, List.of("Docker", "Kafka")),
            new Employee(9, "Ira", 160_000, "IT", true, List.of("Java", "Spring", "Kafka"))
        );
        List<String> sentences = List.of(
            "java streams make java collection code concise",
            "streams need clear collectors",
            "java collectors group stream data"
        );

        // Solve the integrated review prompts here.
    }

    record Employee(long id, String name, double salary, String department, boolean active, List<String> skills) {}
}
```

### Goal
Combine operations only after the individual stream topics are comfortable.

### Task
Solve these:

1. Return top 3 active employee names grouped by department, ordered by salary within each department.
2. Build `department -> unique skills`.
3. Given `sentences`, return the top 3 most frequent words.
4. Find employees whose skills include both `"Java"` and `"Spring"`.

### Checks
- Use downstream collectors when grouping should also transform values.
- Use `flatMap` only for nested lists or split words.
- Sort before `limit` when selecting top records.
