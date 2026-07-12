---
order: 20
---

# Functional Interfaces

A functional interface has exactly one abstract method. `@FunctionalInterface` asks the compiler to enforce that contract.

```java
@FunctionalInterface
interface Calculator {
    int add(int a, int b);
}
```

If someone adds another abstract method, compilation fails.

Default methods, static methods, and private interface methods do not count because they already have implementations.

Methods that match public methods from `Object` also do not count:

```java
@FunctionalInterface
interface NamedCheck {
    boolean test(String value);
    boolean equals(Object other); // Object override, ignored for SAM count
}
```

But `equals(String value)` would count because it overloads, not overrides, `Object.equals(Object)`.

## Signature-first thinking

Do not memorize names first. Identify the shape.

| Shape | Interface | Meaning |
| --- | --- | --- |
| `T -> R` | `Function<T, R>` | transform |
| `T -> boolean` | `Predicate<T>` | test/filter |
| `T -> void` | `Consumer<T>` | consume/side effect |
| `() -> T` | `Supplier<T>` | produce lazily |
| `(T, U) -> R` | `BiFunction<T, U, R>` | transform two inputs |
| `T -> T` | `UnaryOperator<T>` | same-type transform |
| `(T, T) -> T` | `BinaryOperator<T>` | combine two same-type values |

## Examples

```java
Function<Employee, Double> salary = employee -> employee.getSalary();
Predicate<Employee> highPaid = employee -> employee.getSalary() > 100_000;
Consumer<Employee> printer = employee -> System.out.println(employee);
Supplier<UUID> ids = () -> UUID.randomUUID();
BiFunction<String, Integer, Employee> employeeFactory =
        (name, age) -> new Employee(name, age);
UnaryOperator<String> upper = name -> name.toUpperCase();
BinaryOperator<Integer> add = (a, b) -> a + b;
```

`Supplier` is the Java interface name. Conceptually it produces values, but the type is not called `Producer`.

## Streams connection

Stream APIs are built around these shapes:

| Stream API | Functional interface |
| --- | --- |
| `filter(...)` | `Predicate<T>` |
| `map(...)` | `Function<T, R>` |
| `forEach(...)` | `Consumer<T>` |
| `orElseGet(...)` | `Supplier<T>` |
| `reduce(...)` | often `BinaryOperator<T>` |

## Primitive specializations

Generic functional interfaces box primitives. Stream APIs often use primitive specializations to avoid that overhead.

| Shape | Interface | Example use |
| --- | --- | --- |
| `T -> int` | `ToIntFunction<T>` | `mapToInt(Employee::age)` |
| `T -> double` | `ToDoubleFunction<T>` | `mapToDouble(Employee::salary)` |
| `int -> boolean` | `IntPredicate` | `IntStream.filter(...)` |
| `(int, int) -> int` | `IntBinaryOperator` | `IntStream.reduce(...)` |

Use these when the API asks for them; do not manually box just to fit `Function<T, Integer>`.

## Checked exceptions

JDK functional interfaces do not declare checked exceptions.

```java
Function<Path, String> reader = path -> Files.readString(path); // compile error
```

Handle the exception inside the lambda, use a method that does not throw checked exceptions, or keep a normal loop where exception handling is clearer.

## Quick recall

- **Functional interface rule?** Exactly one abstract method.
- **Why use `@FunctionalInterface`?** Compiler protection against accidental second abstract methods.
- **`employee -> employee.getSalary()`?** `Function<Employee, Double>`.
- **`employee -> employee.getSalary() > 100000`?** `Predicate<Employee>`.
- **`() -> UUID.randomUUID()`?** `Supplier<UUID>`.
- **`(name, age) -> new Employee(name, age)`?** `BiFunction<String, Integer, Employee>`.
- **`name -> name.toUpperCase()`?** `UnaryOperator<String>`.
- **Primitive salary extractor?** `ToDoubleFunction<Employee>` or `mapToDouble(Employee::salary)`.
