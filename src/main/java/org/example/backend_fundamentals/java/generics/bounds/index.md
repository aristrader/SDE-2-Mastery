---
order: 20
---

# Generic Bounds

Without a bound, `T` is only known as `Object`. Bounds restrict the allowed types and unlock methods on the bound.

```java
static <T extends Number> double square(T value) {
    return value.doubleValue() * value.doubleValue();
}
```

`Integer`, `Double`, and `Float` work because they extend `Number`. `String` does not.

## Multiple bounds

Use `extends` for class and interface bounds:

```java
static <T extends Number & Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) > 0 ? a : b;
}
```

There is no named type-parameter form like `<T super Integer>`. Lower bounds exist only with wildcards.

## Why bounds matter

This does not compile:

```java
static <T> double square(T value) {
    return value.doubleValue() * value.doubleValue();
}
```

The compiler only knows `T` is an `Object`, and `Object` has no `doubleValue()`.

## Quick recall

- **Bound syntax?** `<T extends Number>`.
- **Why use bounds?** To restrict allowed types and access bound methods.
- **Can named type parameters use `super`?** No.
