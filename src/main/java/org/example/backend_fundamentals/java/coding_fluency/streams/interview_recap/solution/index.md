---
title: Interview Solutions
order: 20
search: false
---

# Streams Interview Solutions

## Solution: output-prediction - Predict Stream Output

```text
[20, 40]
[a, b, c]
[3, 4]
```

Why:

- first pipeline keeps even numbers, multiplies them by `10`, then collects
- second pipeline removes duplicate `"a"`, sorts, then collects
- third pipeline skips `1` and `2`, then keeps `3` and `4`

## Solution: bug-hunt - Find Stream Bugs

1. `Collectors.toMap(Employee::id, Function.identity())` throws on duplicate keys unless a merge function is supplied.

```java
Map<Long, Employee> employeesById = employees.stream()
    .collect(Collectors.toMap(
        Employee::id,
        Function.identity(),
        (first, second) -> first));
```

2. A stream is single-use after a terminal operation.

```java
long count = employees.stream().count();
Optional<Employee> first = employees.stream().findFirst();
```

3. `findFirst().get()` can throw when the stream is empty.

```java
Employee first = employees.stream()
    .findFirst()
    .orElseThrow();
```

4. `parallelStream().forEach(names::add)` mutates shared non-thread-safe state.

```java
List<String> names = employees.parallelStream()
    .map(Employee::name)
    .toList();
```

5. `peek` is for debugging/logging, not business mutation. Use `map` to create changed values, or use a normal loop when mutation is the actual goal.

## Solution: integrated-collectors - Solve One Integrated Collector Problem

```java
Map<String, List<String>> activeNamesByDepartment = employees.stream()
    .filter(Employee::active)
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.mapping(Employee::name, Collectors.toList())));

Map<String, Set<String>> uniqueSkillsByDepartment = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.flatMapping(
            employee -> employee.skills().stream(),
            Collectors.toSet())));

Map<String, List<String>> topActiveNamesByDepartment = employees.stream()
    .filter(Employee::active)
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.collectingAndThen(
            Collectors.toList(),
            departmentEmployees -> departmentEmployees.stream()
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .limit(3)
                .map(Employee::name)
                .toList())));
```
