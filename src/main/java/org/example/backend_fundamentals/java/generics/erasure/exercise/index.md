---
order: 10
search: false
---

# Type Erasure Practice

Write each task in a small runnable class. For illegal examples, keep the code commented and explain the compiler/runtime reason.

## Exercise: type-erasure-runtime-class - Type Erasure

### Goal
Observe that generic type arguments do not create separate runtime classes.

### Task
Create `Box<String>` and `Box<Integer>`.

Print:

```java
box1.getClass()
box2.getClass()
box1.getClass() == box2.getClass()
```

### Checks
- The class comparison prints `true`.
- Your explanation says generic type information is erased after compile-time checking.

## Exercise: static-generic-trap - Static Generic Trap

### Goal
Understand why static fields cannot use a class type parameter.

### Task
Try this and leave it commented:

```java
class Box<T> {
    static T value;
}
```

Explain why Java disallows it.

### Checks
- Your explanation says static fields belong to the one shared class.
- Your explanation says `T` belongs to a parameterized instance.

## Exercise: erasure-traps - Type erasure traps

### Goal
See what generic type information survives at runtime and what gets erased.

### Task
Demonstrate three erasure rules:

- `list instanceof List<Integer>` is illegal; raw `List` is the runtime check.
- `process(List<Integer>)` and `process(List<String>)` cannot be overloaded together.
- `List<Integer>` and `List<String>` have the same runtime class.

### Checks
- Runtime class comparison prints `true`.
- You can explain why overloaded `process(List<Integer>)` and `process(List<String>)` have the same erased signature.
