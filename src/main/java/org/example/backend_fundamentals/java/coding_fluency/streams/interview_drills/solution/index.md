---
order: 20
search: false
---

# Stream Interview Drills Solutions

## Solution: mixed-stream-pipelines - Mixed Stream Questions

```java
List<String> activeNames = employees.stream().filter(Employee::active).map(Employee::name).toList();

List<String> topFiveActiveNames = employees.stream()
    .filter(Employee::active)
    .sorted(Comparator.comparingDouble(Employee::salary).reversed())
    .limit(5)
    .map(Employee::name)
    .toList();

Set<String> departmentsOverFive = employees.stream()
    .collect(Collectors.groupingBy(Employee::department, Collectors.counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 5)
    .map(Map.Entry::getKey)
    .collect(Collectors.toSet());

Set<String> uniqueSkills = employees.stream()
    .flatMap(e -> e.skills().stream())
    .collect(Collectors.toSet());

long javaSkillCount = employees.stream()
    .filter(e -> e.skills().contains("Java"))
    .count();

double activeAverageSalary = employees.stream()
    .filter(Employee::active)
    .mapToDouble(Employee::salary)
    .average()
    .orElse(0.0);

List<Employee> bySalaryDesc = employees.stream()
    .sorted(Comparator.comparingDouble(Employee::salary).reversed())
    .toList();

String largestDepartment = employees.stream()
    .collect(Collectors.groupingBy(Employee::department, Collectors.counting()))
    .entrySet().stream()
    .max(Map.Entry.comparingByValue())
    .map(Map.Entry::getKey)
    .orElseThrow();

Map<String, List<String>> namesByDepartment = employees.stream()
    .collect(Collectors.groupingBy(Employee::department, Collectors.mapping(Employee::name, Collectors.toList())));

Optional<Employee> highestPaidActive = employees.stream()
    .filter(Employee::active)
    .max(Comparator.comparingDouble(Employee::salary));

Map<String, Double> totalSalaryByDepartment = employees.stream()
    .collect(Collectors.groupingBy(Employee::department, Collectors.summingDouble(Employee::salary)));

Set<String> duplicateDepartments = employees.stream()
    .collect(Collectors.groupingBy(Employee::department, Collectors.counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .collect(Collectors.toSet());

Map<String, Double> averageSalaryByDepartment = employees.stream()
    .collect(Collectors.groupingBy(Employee::department, Collectors.averagingDouble(Employee::salary)));

Map<String, Long> countByDepartment = employees.stream()
    .collect(Collectors.groupingBy(Employee::department, Collectors.counting()));
```

## Solution: output-prediction - Predict Stream Output

| Case | Output |
| --- | --- |
| 1 | `[20, 40]` |
| 2 | `[a, b, c]` |
| 3 | `[3, 4]` |
| 4 | `[1, 2, 3, 4]` |
| 5 | `true` |
| 6 | `false` |

## Solution: find-stream-bugs - Find the Bug

1. Two-argument `toMap` throws `IllegalStateException` on duplicate IDs. Add a merge function.
2. A stream is single-use. Create a new stream for the second traversal.
3. `findFirst()` returns `Optional`; `.get()` can throw. Use `orElseThrow`, `orElse`, or handle absence.
4. `parallelStream().forEach(list::add)` mutates shared state from multiple threads. Collect the result instead.
5. `peek` should not perform business mutation. Use `map` for transformation or a loop for explicit mutation.

## Solution: loop-to-streams - Convert Loops to Streams

```java
List<Employee> active = employees.stream().filter(Employee::active).toList();
List<String> names = employees.stream().map(Employee::name).toList();
double totalSalary = employees.stream().mapToDouble(Employee::salary).sum();
Map<String, List<Employee>> byDepartment = employees.stream().collect(Collectors.groupingBy(Employee::department));
Optional<Employee> highestPaid = employees.stream().max(Comparator.comparingDouble(Employee::salary));
Set<String> departments = employees.stream().map(Employee::department).collect(Collectors.toSet());
```

## Solution: choose-stream-api - Choose the Correct API

| Requirement | API |
| --- | --- |
| Remove unwanted elements | `filter` |
| Convert `Employee` to `EmployeeDTO` | `map` |
| Flatten employee skills | `flatMap` |
| Build `Map<Id, Employee>` | `Collectors.toMap` |
| Group by department | `Collectors.groupingBy` |
| Active/inactive split | `Collectors.partitioningBy` |
| At least one HR employee | `anyMatch` |
| Total salary | `mapToDouble(...).sum()` or `reduce` |
| Average salary | `mapToDouble(...).average()` |
| First matching employee | `findFirst` |
| Any matching employee | `findAny` |
| Remove duplicates | `distinct` |
| Top 10 records | `sorted(...).limit(10)` if "top" means ranked; otherwise `limit(10)` |

## Solution: stream-challenges - Challenge Problems

```java
Map<String, List<String>> topTenActiveNamesByDepartment = employees.stream()
    .filter(Employee::active)
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.collectingAndThen(
            Collectors.toList(),
            list -> list.stream()
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .limit(10)
                .map(Employee::name)
                .toList())));

Map<String, Set<String>> uniqueSkillsByDepartment = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.flatMapping(e -> e.skills().stream(), Collectors.toSet())));

Map<String, String> highestPaidNameByDepartment = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.collectingAndThen(
            Collectors.maxBy(Comparator.comparingDouble(Employee::salary)),
            employee -> employee.map(Employee::name).orElseThrow())));

LinkedHashMap<String, Double> averageSalaryDesc = employees.stream()
    .collect(Collectors.groupingBy(Employee::department, Collectors.averagingDouble(Employee::salary)))
    .entrySet().stream()
    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
    .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue,
        (left, right) -> left,
        LinkedHashMap::new));

List<String> topWords = sentences.stream()
    .flatMap(sentence -> Arrays.stream(sentence.toLowerCase().split("\\W+")))
    .filter(word -> !word.isBlank())
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .limit(10)
    .map(Map.Entry::getKey)
    .toList();

Map<String, Long> skillFrequency = employees.stream()
    .flatMap(e -> e.skills().stream())
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

String departmentWithHighestAverageSalary = employees.stream()
    .collect(Collectors.groupingBy(Employee::department, Collectors.averagingDouble(Employee::salary)))
    .entrySet().stream()
    .max(Map.Entry.comparingByValue())
    .map(Map.Entry::getKey)
    .orElseThrow();

List<Employee> javaAndSpring = employees.stream()
    .filter(e -> e.skills().contains("Java") && e.skills().contains("Spring"))
    .toList();
```

Employees sharing at least one skill needs an indexed approach; a pure stream-only version is possible but less readable.

```java
Map<String, List<Employee>> bySkill = employees.stream()
    .flatMap(employee -> employee.skills().stream().map(skill -> Map.entry(skill, employee)))
    .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

Set<Employee> sharingSkill = bySkill.values().stream()
    .filter(list -> list.size() > 1)
    .flatMap(List::stream)
    .collect(Collectors.toSet());
```

Top 5 active IT employees above the IT department average:

```java
double itAverageSalary = employees.stream()
    .filter(e -> e.department().equals("IT"))
    .mapToDouble(Employee::salary)
    .average()
    .orElse(0.0);

List<String> names = employees.stream()
    .filter(Employee::active)
    .filter(e -> e.department().equals("IT"))
    .filter(e -> e.salary() > itAverageSalary)
    .sorted(Comparator.comparingDouble(Employee::salary).reversed())
    .limit(5)
    .map(Employee::name)
    .toList();
```
