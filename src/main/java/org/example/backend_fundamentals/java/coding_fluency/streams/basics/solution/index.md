---
order: 20
search: false
---

# Stream Basics Solutions

## Solution: list-mutability - List Collector Mutability

```java
List<String> mutable = orders.stream()
    .map(Order::id)
    .collect(Collectors.toList());

List<String> unmodifiable = orders.stream()
    .map(Order::id)
    .toList();

List<String> unmodifiableJava10 = orders.stream()
    .map(Order::id)
    .collect(Collectors.toUnmodifiableList());
```

`mutable.add("O7")` works. The two unmodifiable lists throw `UnsupportedOperationException`.

## Solution: set-deduplication - Set Collector Deduplication

```java
Set<String> customerIds = orders.stream()
    .map(Order::customerId)
    .collect(Collectors.toSet());
```

The set removes duplicates. Do not assume a specific order from `Collectors.toSet()`.

## Solution: collector-basics - Basic Collectors

```java
List<Employee> activeEmployees = employees.stream()
    .filter(Employee::active)
    .toList();

Set<String> departments = employees.stream()
    .map(Employee::department)
    .collect(Collectors.toSet());
```
