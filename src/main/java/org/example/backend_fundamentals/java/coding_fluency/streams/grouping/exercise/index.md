---
order: 10
search: false
---

# groupingBy Practice

## Exercise: groupingby-simple - groupingBy simple

### Goal
Bucket a flat list into groups.

### Task
Group all orders by `OrderStatus`, producing `Map<OrderStatus, List<Order>>`.

### Checks
- Print how many orders are in each bucket.
- Explain why this is `groupingBy`, not `toMap`.

## Exercise: groupingby-counting - groupingBy with downstream counting

### Goal
Aggregate within each group.

### Task
Produce `Map<OrderStatus, Long>` using `groupingBy` with `Collectors.counting()`.

### Checks
- The value type is `Long`, not `Integer`.
- The downstream collector is the second argument.

## Exercise: employee-grouping - Employee groupingBy

### Goal
Group employees and aggregate inside each group.

### Task
Using `Employee`:

1. Group employees by department.
2. Count employees department-wise.
3. Group employee names department-wise.
4. Find average salary department-wise.
5. Find highest-paid employee department-wise.
6. Group employees by age.
7. Group employees by city, assuming a `city` field exists.

### Checks
- Use default `groupingBy` when the value should be a list.
- Use downstream collectors for count, mapping, average, and maximum.
