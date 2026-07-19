---
title: Solutions
order: 20
search: false
---

# Stream Intermediate Operations Solutions

## Solution: intermediate-warmup - One Operation at a Time

```java
List<Employee> activeHighPaid = employees.stream()
    .filter(Employee::active)
    .filter(e -> e.salary() > 100_000)
    .toList();

List<EmployeeDto> activeDtos = employees.stream()
    .filter(Employee::active)
    .map(e -> new EmployeeDto(e.id(), e.name()))
    .toList();

List<String> uniqueSkills = employees.stream()
    .flatMap(e -> e.skills().stream())
    .distinct()
    .toList();

List<String> words = lines.stream().flatMap(line -> Arrays.stream(line.split("\\s+"))).toList();

List<Employee> byDeptName = employees.stream()
    .sorted(Comparator.comparing(Employee::department).thenComparing(Employee::name))
    .toList();

List<Employee> topFiveActive = employees.stream()
    .filter(Employee::active)
    .sorted(Comparator.comparingDouble(Employee::salary).reversed())
    .limit(5)
    .toList();

List<Employee> uniqueEmployees = employees.stream().distinct().toList();

List<Employee> page = employees.stream().skip((long) page * size).limit(size).toList();

List<String> loggedActiveNames = employees.stream()
    .peek(System.out::println)
    .filter(Employee::active)
    .map(Employee::name)
    .peek(System.out::println)
    .toList();
```

`distinct()` works for custom objects only when equality is implemented correctly. For top records, sort before `limit`; `limit().sorted()` only sorts the first records already seen.
