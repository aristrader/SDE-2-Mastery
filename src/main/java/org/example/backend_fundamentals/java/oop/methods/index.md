---
order: 20
---

# Methods and Signatures

Methods are Java's functions attached to classes.

## Method Signatures
A method signature in Java consists of **only** two things:
1. The method name.
2. The parameter types and their order.

**Crucially, the return type is NOT part of the method signature.**
This means you cannot overload a method by only changing its return type. The compiler will reject it because it cannot figure out which method to call if you ignore the return value.

```java
// Valid overload (different parameters)
public void print(int x) { ... }
public void print(String x) { ... }

// INVALID overload (same signature, different return type)
public int calculate(int x) { ... }
public double calculate(int x) { ... } // Compile Error!
```

## Pass-by-value
Java methods always receive a copy of the arguments (pass-by-value). For primitives, the value itself is copied. For objects, the reference (pointer) is copied.

## Variable arguments

Varargs (`...`) allow a method to accept zero or multiple arguments of the same type without writing many overloads.

### Internal behavior

Under the hood, the compiler treats a varargs parameter as an array:

```java
public void print(int... nums) {
    for (int n : nums) {
        System.out.println(n);
    }
}
```

Inside the method, `nums` is an `int[]`. Calling `print()` passes an empty array; calling `print(1, 2)` passes an array with two elements.

### Rules

1. The varargs parameter must be last: `void log(int level, String... messages)`.
2. A method can have only one varargs parameter.

### Real-world usage

Common real-world examples: `String.format(String format, Object... args)` and `System.out.printf(String format, Object... args)`.

## Quick recall

- **Java argument passing?** Always pass-by-value.
- **Object argument value is?** A copy of the reference.
- **Overload by return type only?** No.
- **Varargs compile to?** Array parameter.
- **Varargs position?** Last parameter only.
