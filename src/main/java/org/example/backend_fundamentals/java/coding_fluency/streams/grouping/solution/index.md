---
order: 20
search: false
---

# groupingBy Solutions

## Solution: groupingby-simple - groupingBy simple

```java
Map<OrderStatus, List<Order>> byStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::status));
```

Use `groupingBy` because each status can have multiple orders.

## Solution: groupingby-counting - groupingBy with downstream counting

```java
Map<OrderStatus, Long> counts = orders.stream()
    .collect(Collectors.groupingBy(Order::status, Collectors.counting()));
```

`counting()` returns `Long`.

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

Map<String, Optional<Employee>> highestPaidByDepartment = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::department,
        Collectors.maxBy(Comparator.comparingDouble(Employee::salary))));

Map<Integer, List<Employee>> byAge = employees.stream()
    .collect(Collectors.groupingBy(Employee::age));

Map<String, List<Employee>> byCity = employees.stream()
    .collect(Collectors.groupingBy(Employee::city));
```
