---
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
```

Call it with `List<Integer>`, `List<Double>`, and `List<Float>`.

Try `values.add(1)` inside the method, leave it commented, and explain the compiler error.

## Exercise: wildcard-producer - Upper-bounded wildcards

### Goal
Practice `? extends` for producer inputs: read values safely, but do not add values.

### Task
Implement:

- `printNumbers(List<? extends Number>)`
- `sumList(List<? extends Number>)`

Then try to add an `Integer` inside `printNumbers` and explain the compiler error.

### Checks
- Both `List<Integer>` and `List<Double>` compile.
- The sums print `6.0` and `4.0`.
- You can explain why `values.add(1)` is unsafe for `List<? extends Number>`.

## Exercise: super-wildcard-defaults - Super Wildcard

### Goal
Use `? super` for a list that consumes integers.

### Task
Implement:

```java
static void addDefaults(List<? super Integer> values)
```

Add `10`, `20`, and `30`. Call it with `List<Integer>`, `List<Number>`, and `List<Object>`.

### Checks
- Adding integers compiles for all three lists.
- Reading as `Integer` directly does not compile.

## Exercise: wildcard-consumer - Lower-bounded wildcards

### Goal
Practice `? super` for consumer inputs: add integers safely, but read back only as `Object`.

### Task
Implement:

- `addNumbers(List<? super Integer>)` adding `1` through `5`.
- `fill(List<? super Integer>, int value, int count)`.

Call both with `List<Integer>`, `List<Number>`, and `List<Object>`.

### Checks
- `integers` and `numbers` contain `[1, 2, 3, 4, 5]`.
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
