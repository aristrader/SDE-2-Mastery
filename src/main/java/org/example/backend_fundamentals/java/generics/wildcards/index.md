---
order: 30
---

# Generic Wildcards

Wildcards are bounds where the exact type name is intentionally unknown.

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

## Quick recall

- **Read from list?** `? extends T`.
- **Write into list?** `? super T`.
- **Read from `? super Integer`?** Only `Object` is guaranteed.
- **Why invariant?** To prevent type corruption.
