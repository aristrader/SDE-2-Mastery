---
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
```

`List<? extends Number>` is safe for reading as `Number`. Adding is blocked:

```java
// values.add(1); // compile error
```

The actual list might be `List<Double>` or `List<Float>`, so adding an `Integer` could corrupt it.

## Solution: wildcard-producer - Upper-bounded wildcards

```java
static void printNumbers(List<? extends Number> values) {
    for (Number value : values) {
        System.out.println(value);
    }
    // values.add(1); // compile error
}

static double sumList(List<? extends Number> values) {
    double total = 0;
    for (Number value : values) {
        total += value.doubleValue();
    }
    return total;
}
```

`? extends Number` means the list produces numbers. The exact subtype is unknown, so adding a specific `Integer` is unsafe.

## Solution: super-wildcard-defaults - Super Wildcard

```java
static void addDefaults(List<? super Integer> values) {
    values.add(10);
    values.add(20);
    values.add(30);
}
```

`? super Integer` means the list can consume integers. Reading gives only `Object` because the actual list might be `List<Object>`.

## Solution: wildcard-consumer - Lower-bounded wildcards

```java
static void addNumbers(List<? super Integer> values) {
    for (int i = 1; i <= 5; i++) {
        values.add(i);
    }
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
