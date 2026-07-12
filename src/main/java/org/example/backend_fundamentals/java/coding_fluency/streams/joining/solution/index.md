---
order: 20
search: false
---

# joining Solutions

## Solution: joining-strings - joining

```java
String allIds = orders.stream()
    .map(Order::id)
    .collect(Collectors.joining(","));

String paidIds = orders.stream()
    .filter(o -> o.status() == OrderStatus.PAID)
    .map(Order::id)
    .collect(Collectors.joining(" | ", "[", "]"));
```

## Solution: employee-joining - Employee Joining

```java
String employeeNames = employees.stream()
    .map(Employee::name)
    .collect(Collectors.joining(", "));

String departments = employees.stream()
    .map(Employee::department)
    .distinct()
    .collect(Collectors.joining(", ", "[", "]"));
```
