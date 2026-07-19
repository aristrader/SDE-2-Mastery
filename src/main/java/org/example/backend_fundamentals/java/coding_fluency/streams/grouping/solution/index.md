---
order: 20
search: false
---

# groupingBy Solutions

## Solution: employee-grouping - Employee groupingBy

```java
Map<String, List<Employee>> byDepartment = employees.stream()
    .collect(Collectors.groupingBy(Employee::department));

Map<String, Long> countByDepartment = employees.stream()
    .collect(Collectors.groupingBy(Employee::department, Collectors.counting()));

Map<String, List<String>> namesByDepartment = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.mapping(Employee::name, Collectors.toList())));

Map<String, Double> averageSalaryByDepartment = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.averagingDouble(Employee::salary)));
```
