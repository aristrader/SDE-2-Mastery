---
order: 30
---

# Method References

A method reference is shorthand for a lambda that only calls one existing method or constructor.

```java
employees.stream()
    .map(employee -> employee.name())
    .toList();

employees.stream()
    .map(Employee::name)
    .toList();
```

Use a method reference when it makes the receiver and method clearer. Keep a lambda when the body has extra logic.

## Four forms

| Form | Example | Equivalent lambda |
| --- | --- | --- |
| Static method | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Bound instance method | `System.out::println` | `x -> System.out.println(x)` |
| Unbound instance method | `String::toUpperCase` | `s -> s.toUpperCase()` |
| Constructor | `ArrayList::new` | `() -> new ArrayList<>()` |

Bound means a specific receiver object is captured. Unbound means the receiver is supplied later as the first argument.

## Static references

Static references work when the lambda just forwards arguments.

```java
Predicate<Order> highValue = OrderRules::isHighValue;
Function<String, Integer> parser = Integer::parseInt;
```

This cannot become a method reference because it has extra logic:

```java
order -> order.total() != null && OrderRules.isHighValue(order)
```

## Bound vs unbound

`System.out::println` is bound because `System.out` is one concrete `PrintStream`.

```java
Consumer<String> printer = System.out::println;
```

`String::length` is unbound because Java calls `length()` on whichever `String` arrives.

```java
Function<String, Integer> length = String::length;
```

For streams, unbound references are common because each stream element becomes the receiver.

## Constructor references

Constructor references match the target functional interface signature.

```java
Supplier<List<String>> emptyList = ArrayList::new;
Function<Order, OrderDto> toDto = OrderDto::new;
BiFunction<String, BigDecimal, Invoice> invoiceFactory = Invoice::new;
```

If `OrderDto::new` is assigned to `Function<Order, OrderDto>`, Java looks for a constructor like `OrderDto(Order order)`.

## Comparator examples

Method references fit `Comparator.comparing` because it needs a key extractor.

```java
Comparator<Employee> byName = Comparator.comparing(Employee::name);

Comparator<Employee> bySalaryDesc =
    Comparator.comparingDouble(Employee::salary).reversed();

Comparator<Employee> byDepartmentThenName =
    Comparator.comparing(Employee::department)
        .thenComparing(Employee::name);
```

Without method references:

```java
Comparator<Employee> byName =
    Comparator.comparing(employee -> employee.name());

Comparator<Employee> bySalaryDesc =
    Comparator.comparingDouble(employee -> employee.salary()).reversed();
```

## When not to use them

Do not force a method reference when the lambda is doing composition, branching, arithmetic, null checks, or multiple calls.

```java
order -> order.status().name()          // two calls
employee -> employee.salary() * 1.10    // arithmetic
user -> user == null ? "NA" : user.id() // branching
```

Readable lambdas are better than clever method references.

## Quick recall

- **Method reference purpose?** Shorter syntax for a lambda that only delegates to one method/constructor.
- **`System.out::println` form?** Bound instance method reference.
- **`String::toUpperCase` form?** Unbound instance method reference.
- **`ClassName::new` target type?** Whatever functional interface matches the constructor parameters.
- **Can `x -> x.a().b()` become one method reference?** No.
- **Comparator key extractor?** `Comparator.comparing(Employee::name)`.
