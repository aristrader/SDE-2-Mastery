---
order: 30
---

# Generic Wildcards

Wildcards are bounds where the exact type name is intentionally unknown.

## Interview answer shape

Java generics are invariant: `List<Integer>` is not a `List<Number>`. Wildcards loosen method parameters without losing type safety.

Use this verbal shortcut:

- `? extends T`: I only need to read `T` values from the structure.
- `? super T`: I need to put `T` values into the structure.
- Plain `T`: I need the same exact type across multiple parameters or return values.

## Unbounded wildcard

```java
void printAll(List<?> values)
```

Use `?` when the exact element type does not matter.

## `? extends`

```java
double sum(List<? extends Number> values)
```

Accepts `List<Integer>`, `List<Double>`, and `List<Number>`.

You can safely read `Number` values, but cannot add numbers:

```java
Number n = values.get(0);
// values.add(1); // compile error
```

The actual list might be `List<Double>`, so adding an `Integer` would be unsafe.

## `? super`

```java
void addDefaults(List<? super Integer> values)
```

Accepts `List<Integer>`, `List<Number>`, and `List<Object>`.

You can safely add integers, but reads are only guaranteed as `Object`:

```java
values.add(10);
Object value = values.get(0);
```

## Invariance

`Integer extends Number`, but `List<Integer>` is not a `List<Number>`.

```java
List<Integer> integers = new ArrayList<>();
// List<Number> numbers = integers; // compile error
```

If Java allowed it, code could add a `Double` through `numbers` and corrupt the underlying `List<Integer>`.

## PECS

Use `extends` when the collection produces values for you. Use `super` when the collection consumes values from you. If you both read and write the same exact type, use a named type parameter instead.

## Common API shapes

```java
static double total(List<? extends Number> values) {
    return values.stream().mapToDouble(Number::doubleValue).sum();
}

static void addDefaults(List<? super Integer> values) {
    values.add(0);
    values.add(1);
}

static <T> T first(List<T> values, T fallback) {
    return values.isEmpty() ? fallback : values.get(0);
}
```

Do not force callers to pass `List<Number>` when the method only reads numbers. `List<? extends Number>` accepts `List<Integer>`, `List<Long>`, and `List<Double>`.

Do not use `? extends` when the method needs to add values. The compiler rejects it because the actual list might be a narrower subtype.

## Quick recall

- **Read from list?** `? extends T`.
- **Write into list?** `? super T`.
- **Read from `? super Integer`?** Only `Object` is guaranteed.
- **Why invariant?** To prevent type corruption.
- **When use named `<T>`?** When multiple arguments or the return value must be the same exact type.
