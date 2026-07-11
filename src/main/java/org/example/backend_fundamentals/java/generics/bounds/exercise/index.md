---
order: 10
search: false
---

# Generic Bounds Practice

Write each task in a small runnable class. Do not start from a prebuilt skeleton; the generic method signature is part of the practice.

## Exercise: bounded-square - Bounded Generic Square

### Goal
Use a bound so the compiler knows numeric methods are available.

### Task
Write:

```java
static <T extends Number> double square(T value)
```

Return `value.doubleValue() * value.doubleValue()`.

Call it with `Integer`, `Double`, and `Float`. Comment a `String` call and explain why it fails.

## Exercise: bounds-matter - Why Bounds Matter

### Goal
Understand what changes when the bound is removed.

### Task
Temporarily remove `extends Number` from `bounded-square`.

Explain why this no longer compiles:

```java
value.doubleValue()
```

### Checks
- Your answer says unbounded `T` is only known as `Object`.
- Your answer names the method unlocked by the `Number` bound.

## Exercise: bounded-max-sum - Bounds with extends

### Goal
Use bounds to unlock methods on `T`: `compareTo` for max and `doubleValue` for sums.

### Task
Implement:

- `findMax(List<T>)` where `T` can be compared.
- `sumList(List<T>)` where `T` is a `Number`.

Then explain why `item > max` is invalid for generic objects.

### Checks
- Max of `[3, 10, 2]` is `10`.
- Sum of `[1, 2, 3]` is `6.0`.
- Your explanation names both issues: `>` works on primitives, and unbounded `T` has no comparison contract.
