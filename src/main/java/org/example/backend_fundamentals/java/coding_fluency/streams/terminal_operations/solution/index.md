---
order: 20
search: false
---

# Stream Terminal Operations Solutions

## Solution: terminal-operations - Consume a Stream

```java
long activeCount = employees.stream()
    .filter(Employee::active)
    .count();

Optional<Employee> firstHr = employees.stream()
    .filter(e -> e.department().equals("HR"))
    .findFirst();

Optional<Employee> anyIt = employees.stream()
    .filter(e -> e.department().equals("IT"))
    .findAny();

boolean anyVeryHighPaid = employees.stream().anyMatch(e -> e.salary() > 200_000);
boolean allActive = employees.stream().allMatch(Employee::active);
boolean noneUnder18 = employees.stream().noneMatch(e -> e.age() < 18);

double totalSalary = employees.stream()
    .map(Employee::salary)
    .reduce(0.0, Double::sum);

Optional<Double> maxSalary = employees.stream()
    .map(Employee::salary)
    .reduce(Double::max);

String names = employees.stream()
    .map(Employee::name)
    .reduce("", (left, right) -> left.isEmpty() ? right : left + ", " + right);
```

For real name joining, prefer `Collectors.joining(", ")`; this exercise uses `reduce()` to practice reduction.
