---
order: 20
---

# Stream Intermediate Operations

Intermediate operations transform a stream into another stream. They are lazy: no element is processed until a terminal operation consumes the pipeline.

## Filtering and mapping

`filter` keeps or removes elements using a `Predicate<T>`.

```java
List<Employee> active = employees.stream()
    .filter(Employee::active)
    .toList();
```

`map` converts one element into one other value using a `Function<T, R>`.

```java
List<String> names = employees.stream()
    .map(Employee::name)
    .toList();
```

Use `filter` when the count may shrink. Use `map` when each input produces one output.

## flatMap

`flatMap` is for one-to-many transformations where the nested streams should become one flat stream.

```java
List<String> skills = employees.stream()
    .flatMap(employee -> employee.skills().stream())
    .distinct()
    .toList();
```

Mental model:

```text
Stream<Employee> -> Stream<Stream<String>> -> Stream<String>
```

Without `flatMap`, you keep a nested shape.

## Sorting, distinct, limit, skip

`sorted()` uses natural ordering. `sorted(comparator)` uses explicit ordering.

```java
List<Employee> byDepartmentThenName = employees.stream()
    .sorted(Comparator.comparing(Employee::department)
        .thenComparing(Employee::name))
    .toList();
```

`distinct()` uses `equals()` and `hashCode()`. If custom objects are not deduping, check those methods first.

`limit(n)` keeps the first `n` after earlier pipeline steps. `skip(n)` discards the first `n`; together they can implement simple pagination.

```java
List<Employee> page = employees.stream()
    .skip((long) pageNumber * pageSize)
    .limit(pageSize)
    .toList();
```

## peek

`peek` is mainly for debugging/logging inside a pipeline.

```java
List<String> names = employees.stream()
    .peek(employee -> log.debug("before filter {}", employee))
    .filter(Employee::active)
    .map(Employee::name)
    .toList();
```

Do not use `peek` to mutate business objects. If the goal is mutation, a loop is usually clearer.

## Common traps

- `map` instead of `flatMap` leaves nested collections.
- `distinct` on custom objects depends on `equals()` and `hashCode()`.
- `sorted().limit(5)` finds top items; `limit(5).sorted()` sorts only the first five original elements.
- `peek` does not run without a terminal operation.

## Quick recall

- **Keep only matching elements?** `filter`.
- **Convert each element?** `map`.
- **Flatten nested lists/streams?** `flatMap`.
- **Top 5 by salary?** `sorted(comparing salary reversed).limit(5)`.
- **Pagination?** `skip(offset).limit(pageSize)`.
- **Debug inside a stream?** `peek`, but avoid mutation.
