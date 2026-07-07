---
order: 30
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
