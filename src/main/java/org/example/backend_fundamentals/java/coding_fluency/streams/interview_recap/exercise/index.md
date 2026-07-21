---
title: Interview Practice
order: 10
search: false
---

# Streams Interview Practice

## Exercise: output-prediction - Predict Stream Output

<!-- starter-code -->
```java
import java.util.List;

public class StreamOutputPredictionPractice {
    public static void main(String[] args) {
        System.out.println(List.of(1, 2, 3, 4).stream()
            .filter(x -> x % 2 == 0)
            .map(x -> x * 10)
            .toList());

        System.out.println(List.of("a", "b", "a", "c").stream()
            .distinct()
            .sorted()
            .toList());

        System.out.println(List.of(1, 2, 3, 4, 5).stream()
            .skip(2)
            .limit(2)
            .toList());
    }
}
```

### Goal
Mentally execute common stream operations without running the code first.

### Task
Predict the output.

```java
System.out.println(List.of(1, 2, 3, 4).stream()
    .filter(x -> x % 2 == 0)
    .map(x -> x * 10)
    .toList());

System.out.println(List.of("a", "b", "a", "c").stream()
    .distinct()
    .sorted()
    .toList());

System.out.println(List.of(1, 2, 3, 4, 5).stream()
    .skip(2)
    .limit(2)
    .toList());
```

### Checks
- Explain where `filter`, `map`, `distinct`, `sorted`, `skip`, and `limit` affect the result.
- Identify the terminal operation that starts each pipeline.

## Exercise: bug-hunt - Find Stream Bugs

<!-- starter-code -->
```java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamBugHuntPractice {
    public static void main(String[] args) {
        List<Employee> employees = List.of(
            new Employee(1, "Asha", 120_000),
            new Employee(2, "Ben", 95_000),
            new Employee(2, "Ben Duplicate", 105_000)
        );
        List<String> names = new ArrayList<>();

        // Recreate each buggy pattern from the task, then write the safe version below it.
    }

    record Employee(long id, String name, double salary) {}
}
```

### Goal
Recognize the stream mistakes interviewers commonly probe.

### Task
Explain what is wrong in each snippet and write the safer shape.

```java
Collectors.toMap(Employee::id, Function.identity());

Stream<Employee> stream = employees.stream();
stream.count();
stream.findFirst();

employees.stream().findFirst().get();

List<String> names = new ArrayList<>();
employees.parallelStream()
    .map(Employee::name)
    .forEach(names::add);

employees.stream()
    .peek(employee -> employee.setSalary(100_000))
    .toList();
```

### Checks
- Cover duplicate `toMap` keys.
- Cover single-use streams.
- Cover empty `Optional`.
- Cover shared mutation in parallel streams.
- Cover why `peek` is not for business mutation.

## Exercise: integrated-collectors - Solve One Integrated Collector Problem

<!-- starter-code -->
```java
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IntegratedCollectorsPractice {
    public static void main(String[] args) {
        List<Employee> employees = List.of(
            new Employee("Asha", "IT", 120_000, true, List.of("Java", "Spring")),
            new Employee("Ben", "HR", 95_000, true, List.of("Excel", "SQL")),
            new Employee("Chen", "IT", 210_000, true, List.of("Java", "Kafka")),
            new Employee("Diya", "Finance", 110_000, false, List.of("SQL", "Python")),
            new Employee("Evan", "IT", 130_000, true, List.of("Java", "AWS")),
            new Employee("Farah", "HR", 85_000, true, List.of("Recruiting", "Excel")),
            new Employee("Gopal", "Platform", 150_000, true, List.of("Java", "Kubernetes")),
            new Employee("Hina", "Platform", 140_000, false, List.of("Docker", "Kafka")),
            new Employee("Ira", "IT", 160_000, true, List.of("Java", "Spring", "Kafka"))
        );

        // Build the three requested maps here.
    }

    record Employee(String name, String department, double salary, boolean active, List<String> skills) {}
}
```

### Goal
Combine stream operations only after the individual APIs are clear.

### Task
Given employees with `name`, `department`, `salary`, `active`, and `skills`, build:

1. `department -> active employee names`
2. `department -> unique skills`
3. top 3 active employee names by department, ordered by salary inside each department

### Checks
- Use `filter` before grouping active employees.
- Use downstream collectors when grouping should also transform values.
- Use `flatMapping` for `skills`.
- Sort before `limit` when selecting top records.
