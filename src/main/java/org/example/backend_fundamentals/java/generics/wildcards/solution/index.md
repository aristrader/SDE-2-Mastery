---
title: Solutions
order: 20
search: false
---

# Generic Wildcards Solutions

## Solution: extends-wildcard-sum - Extends Wildcard

```java
static double sum(List<? extends Number> values) {
    double total = 0;
    for (Number value : values) {
        total += value.doubleValue();
    }
    return total;
}

static void printNumbers(List<? extends Number> values) {
    for (Number value : values) {
        System.out.println(value);
    }
    // values.add(1); // compile error
}
```

`List<? extends Number>` is safe for reading as `Number`. The actual list might be `List<Double>` or `List<Float>`, so adding an `Integer` could corrupt it.

## Solution: super-wildcard-defaults - Super Wildcard

```java
static void addDefaults(List<? super Integer> values) {
    values.add(10);
    values.add(20);
    values.add(30);
}

static void fill(List<? super Integer> values, int value, int count) {
    for (int i = 0; i < count; i++) {
        values.add(value);
    }
}
```

`List<Integer>`, `List<Number>`, and `List<Object>` can all consume integers. Reading from that parameter type is only safe as `Object`.

## Solution: generics-invariance - Invariance

This does not compile:

```java
List<Integer> integers = new ArrayList<>();
// List<Number> numbers = integers;
```

If Java allowed it, this would become possible:

```java
numbers.add(3.14);             // Double is a Number
Integer value = integers.get(0); // but the same list now contains a Double
```

Generics are invariant so this corruption is blocked at compile time.

## Solution: exact-type-parameter - Same Type In And Out

```java
static <T> T firstOrDefault(List<T> values, T fallback) {
    return values.isEmpty() ? fallback : values.get(0);
}

String name = firstOrDefault(List.of("Asha"), "unknown");
Integer score = firstOrDefault(List.of(10), 0);
```

Use a named `<T>` when the method needs one exact type relationship across parameters and the return value. `List<?>` would let you read only an unknown type, usually forcing callers to treat the result as `Object`.
