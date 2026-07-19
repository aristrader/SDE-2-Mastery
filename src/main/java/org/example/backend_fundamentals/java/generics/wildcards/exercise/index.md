---
title: Exercises
order: 10
search: false
---

# Generic Wildcards Practice

Write each task in a small runnable class. Keep the broken compiler-error lines commented after you verify the error.

## Exercise: extends-wildcard-sum - Extends Wildcard

### Goal
Use `? extends` for a list that produces numbers.

### Task
Implement:

```java
static double sum(List<? extends Number> values)
static void printNumbers(List<? extends Number> values)
```

Call both with `List<Integer>`, `List<Double>`, and `List<Float>`.
Try `values.add(1)` inside one method, leave it commented, and explain the compiler error.

### Checks
- Both `List<Integer>` and `List<Double>` compile.
- The sums print expected totals.
- You can explain why `values.add(1)` is unsafe for `List<? extends Number>`.

## Exercise: super-wildcard-defaults - Super Wildcard

### Goal
Use `? super` for a list that consumes integers.

### Task
Implement:

```java
static void addDefaults(List<? super Integer> values)
static void fill(List<? super Integer> values, int value, int count)
```

Call both with `List<Integer>`, `List<Number>`, and `List<Object>`.

### Checks
- Adding integers compiles for all three lists.
- `objects` can be filled with `[9, 9, 9]`.
- Reading from `List<? super Integer>` is treated as `Object`, not `Integer`.

## Exercise: generics-invariance - Invariance

### Goal
See why `List<Integer>` is not a `List<Number>`.

### Task
Try this and leave it commented:

```java
List<Integer> integers = new ArrayList<>();
List<Number> numbers = integers;
```

Write down why Java blocks it.

### Checks
- Your explanation includes the corruption case: adding a `Double` through `numbers`.

## Exercise: exact-type-parameter - Same Type In And Out

### Goal
Know when a named type parameter is clearer than a wildcard.

### Task
Implement:

```java
static <T> T firstOrDefault(List<T> values, T fallback)
```

Call it with `List<String>` and `List<Integer>`.

### Checks
- The return type remains the exact list element type.
- You can explain why `List<?>` would force the return type toward `Object`.
