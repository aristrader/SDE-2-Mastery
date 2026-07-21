---
order: 20
search: false
---

# Primitive and Parallel Streams Solutions

## Solution: primitive-and-parallel - Numeric and Parallel Stream Choices

```java
double averageSalary = employees.stream()
    .mapToDouble(Employee::salary)
    .average()
    .orElse(0.0);

int totalAge = employees.stream()
    .mapToInt(Employee::age)
    .sum();
```

This is unsafe because many threads mutate the same list:

```java
employees.parallelStream()
         .map(Employee::name)
         .forEach(names::add);
```

Collect safely instead:

```java
List<String> names = employees.parallelStream()
    .map(Employee::name)
    .toList();
```

Do not use `parallelStream()` by default for HTTP or database calls. It uses the common ForkJoinPool and can create contention.
