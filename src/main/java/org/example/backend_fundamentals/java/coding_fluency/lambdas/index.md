---
order: 10
---

# Lambdas

Lambdas did not give Java a new capability. Before Java 8, the same behavior was written with anonymous classes. Lambdas removed the ceremony around single-method behavior.

Anonymous class:

```java
Collections.sort(employees, new Comparator<Employee>() {
    @Override
    public int compare(Employee a, Employee b) {
        return Double.compare(a.getSalary(), b.getSalary());
    }
});
```

Lambda:

```java
(a, b) -> Double.compare(a.getSalary(), b.getSalary())
```

The business logic is the comparison. `new`, `Comparator`, anonymous class syntax, `@Override`, and braces are boilerplate.

## Functional interface target

A lambda always has a target type. That type must be a functional interface: an interface with exactly one abstract method.

```java
Runnable task = () -> System.out.println("Hello");
Comparator<Employee> bySalary =
        (a, b) -> Double.compare(a.getSalary(), b.getSalary());
```

Java knows `() -> ...` implements `Runnable.run()` and `(a, b) -> ...` implements `Comparator.compare(...)`.

If an interface has two unrelated abstract methods, Java cannot know which method the lambda implements.

```java
interface Broken {
    void f();
    void g();
}
```

## Lambda syntax

Single parameter:

```java
x -> x * x
```

Multiple parameters:

```java
(a, b) -> a + b
```

No parameters:

```java
() -> System.out.println("Hello")
```

Expression body:

```java
x -> x * x
```

Block body:

```java
x -> {
    System.out.println(x);
    return x * x;
}
```

Expression-bodied lambdas return the expression implicitly. Block-bodied lambdas use explicit `return` when a value is required.

## Type inference

The target functional interface gives Java the parameter types.

```java
Comparator<Employee> bySalary =
        (a, b) -> Double.compare(a.getSalary(), b.getSalary());
```

Because the left side is `Comparator<Employee>`, Java infers `a` and `b` as `Employee`.

## Effectively final variables

Lambdas can capture local variables only if they are final or effectively final.

```java
String prefix = "EMP-";

employees.stream()
        .map(employee -> prefix + employee.id())
        .toList();
```

This is allowed because `prefix` is not reassigned. This is not:

```java
String prefix = "EMP-";
prefix = "USER-";

employees.stream()
        .map(employee -> prefix + employee.id()); // compile error
```

The rule avoids confusing closures over changing stack variables. If state must change, use a clearer loop or a proper mutable object with a narrow scope.

## Target type ambiguity

The same lambda body can match different functional interfaces. Java needs context.

```java
Predicate<String> nonBlank = value -> !value.isBlank();
Function<String, Boolean> nonBlankFunction = value -> !value.isBlank();
```

When overloads accept different functional interfaces with compatible lambda shapes, the compiler may need an explicit cast or a more specific method call.

## Method references

Method references are shorter syntax for lambdas that only call an existing method.

```java
x -> System.out.println(x)
System.out::println

s -> s.length()
String::length

(a, b) -> Integer.compare(a, b)
Integer::compare
```

Use a method reference when it improves readability. Keep a lambda when there is extra logic:

```java
employee -> employee.getSalary() * 1.1
```

## Common misconceptions

- Lambdas are not new runtime magic; anonymous classes could already express the behavior.
- A lambda is not typeless; it is assigned to a functional interface.
- "One method" is imprecise. The rule is exactly one abstract method.
- Default, static, and private interface methods do not break functional-interface status because they are not abstract.
- Object method declarations such as `equals(Object)`, `hashCode()`, and `toString()` do not count as functional-interface abstract methods.

## Quick recall

- **Why were lambdas introduced?** To remove anonymous-class boilerplate for single-method behavior.
- **What type is a lambda?** A functional interface target type.
- **Why only one abstract method?** Otherwise Java cannot infer which method the lambda implements.
- **When is `return` optional?** In expression-bodied lambdas.
- **When should you avoid method references?** When the lambda does more than directly call one method.
- **Can a lambda capture reassigned local variables?** No, only final or effectively final locals.
