---
order: 20
search: false
---

# Generic Bounds Solutions

## Solution: bounded-square - Bounded Generic Square

```java
static <T extends Number> double square(T value) {
    return value.doubleValue() * value.doubleValue();
}
```

Calls:

```java
System.out.println(square(4));      // 16.0
System.out.println(square(2.5));    // 6.25
System.out.println(square(3.0f));   // 9.0
// square("4"); // does not compile
```

`String` fails because it is not a subtype of `Number`.

## Solution: bounds-matter - Why Bounds Matter

Without the bound:

```java
static <T> double square(T value) {
    return value.doubleValue() * value.doubleValue(); // compile error
}
```

The compiler only knows that `T` is an `Object`. `Object` does not have `doubleValue()`. The `extends Number` bound tells the compiler every allowed `T` has `doubleValue()`.

## Solution: bounded-max-sum - Bounds with extends

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

Use `compareTo`, not `>`, for objects. The `Comparable` bound gives the comparison contract; the `Number` bound gives `doubleValue()`.
