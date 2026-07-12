---
order: 30
---

# Stream Terminal Operations

Terminal operations consume a stream and produce a final result or side effect. They also trigger all lazy intermediate operations before them.

## Collecting and side effects

Use `collect` or `toList` when the pipeline should produce data.

```java
List<String> activeNames = employees.stream()
    .filter(Employee::active)
    .map(Employee::name)
    .toList();
```

Use `forEach` only for side effects such as printing, publishing, or calling an external API. Do not use it to build a result list when a collector can do that.

```java
employees.stream()
    .filter(Employee::active)
    .forEach(employee -> audit(employee.id()));
```

## Finding and matching

`findFirst` respects encounter order. `findAny` allows any matching element and matters more with parallel streams.

```java
Optional<Employee> firstHr = employees.stream()
    .filter(employee -> employee.department().equals("HR"))
    .findFirst();
```

Match operations short-circuit:

```java
boolean hasHighEarner = employees.stream()
    .anyMatch(employee -> employee.salary() > 200_000);

boolean everyoneActive = employees.stream()
    .allMatch(Employee::active);

boolean noMinor = employees.stream()
    .noneMatch(employee -> employee.age() < 18);
```

Prefer `anyMatch()` over `filter(...).count() > 0`; it can stop early.

## reduce

`reduce` combines elements into one value.

```java
double totalSalary = employees.stream()
    .map(Employee::salary)
    .reduce(0.0, Double::sum);
```

The identity value must be neutral:

- sum identity: `0`
- product identity: `1`
- string concatenation identity: `""`

The no-identity overload returns `Optional<T>` because the stream might be empty.

```java
Optional<Employee> highestPaid = employees.stream()
    .reduce((left, right) -> left.salary() >= right.salary() ? left : right);
```

## Optional handling

Do not blindly call `.get()` on a result from `findFirst`, `findAny`, `max`, `min`, or identity-free `reduce`.

```java
Employee first = employees.stream()
    .filter(Employee::active)
    .findFirst()
    .orElseThrow();
```

## Quick recall

- **What triggers the stream?** A terminal operation.
- **Need a result collection?** Prefer `toList()` or `collect(...)`.
- **First in encounter order?** `findFirst`.
- **Existence check?** `anyMatch`, not `filter().count() > 0`.
- **All satisfy a predicate?** `allMatch`.
- **No elements satisfy a predicate?** `noneMatch`.
- **Reduce without identity returns what?** `Optional<T>`.
