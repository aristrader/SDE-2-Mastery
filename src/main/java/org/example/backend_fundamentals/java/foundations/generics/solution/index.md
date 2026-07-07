---
order: 20
search: false
---

# Generics Solutions

Each solution matches one structured practice question by ID.

## Solution: generic-pair-stack - Generic containers

`Pair<A, B>` keeps the two component types separate, so `Pair<String, Integer>` does not lose the fact that the first value is a `String` and the second is an `Integer`.

```java
A getFirst() {
    return first;
}

B getSecond() {
    return second;
}

Pair<B, A> swap() {
    return new Pair<>(second, first);
}
```

`SimpleStack<T>` should delegate to the end of the backing list so push/pop are LIFO:

```java
void push(T value) {
    values.add(value);
}

T pop() {
    return values.remove(values.size() - 1);
}

T peek() {
    return values.get(values.size() - 1);
}

boolean isEmpty() {
    return values.isEmpty();
}
```

The raw-list bug disappears because `List<String>` moves the failure from runtime to compile time. `names.add(42)` cannot compile, and the loop no longer needs a cast.

## Solution: bounded-max-sum - Bounds with extends

Use one bound for comparison and one bound for numeric conversion:

```java
static <T extends Comparable<T>> T findMax(List<T> values) {
    if (values.isEmpty()) {
        throw new IllegalArgumentException("values must not be empty");
    }
    T max = values.get(0);
    for (T value : values) {
        if (value.compareTo(max) > 0) {
            max = value;
        }
    }
    return max;
}

static <T extends Number> double sumList(List<T> values) {
    double total = 0;
    for (T value : values) {
        total += value.doubleValue();
    }
    return total;
}
```

`item > max` is wrong because generics are object types, and `>` works on primitives. Also, plain `T` is only known as `Object`, so the compiler cannot assume it has `compareTo`.

## Solution: wildcard-producer - Upper-bounded wildcards

For `? extends Number`, the list produces `Number` values safely:

```java
static void printNumbers(List<? extends Number> values) {
    for (Number value : values) {
        System.out.println(value.doubleValue());
    }
}

static double sumList(List<? extends Number> values) {
    double total = 0;
    for (Number value : values) {
        total += value.doubleValue();
    }
    return total;
}
```

You cannot add `1` to `List<? extends Number>` because the actual list might be `List<Double>`, `List<BigDecimal>`, or another `Number` subtype list. Adding an `Integer` would break that list's real element type.

## Solution: wildcard-consumer - Lower-bounded wildcards

For `? super Integer`, the list can safely consume integers:

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

Reading is less specific. The list might be `List<Object>`, so the compiler only promises that `get` returns `Object`. Assigning directly to `Integer` is unsafe without a cast and runtime check.

## Solution: erasure-traps - Type erasure traps

At runtime, both `List<Integer>` and `List<String>` are just `List`:

```java
Object unknown = List.of(1, 2, 3);
if (unknown instanceof List) {
    System.out.println("It is a List at runtime");
}

List<Integer> ints = new ArrayList<>();
List<String> strings = new ArrayList<>();
System.out.println(ints.getClass() == strings.getClass()); // true
```

The overload trap has the same cause. These two methods cannot live together:

```java
void process(List<Integer> values) {}
void process(List<String> values) {}
```

After erasure, both look like `process(List values)`, so the JVM would see duplicate signatures. Use distinct names, a discriminator parameter, or a different abstraction.
