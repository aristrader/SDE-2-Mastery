---
order: 10
---

# Generic Basics

Use generics when the same code should work for different types while preserving compile-time type safety.

Before Java 5, collections often used raw `Object`:

```java
List values = new ArrayList();
values.add("Alice");
values.add(10);

String name = (String) values.get(1); // ClassCastException at runtime
```

With generics, the compiler catches the bug:

```java
List<String> names = new ArrayList<>();
names.add("Alice");
// names.add(10); // compile error
```

## Generic classes

`T` is a type parameter. It is not special syntax beyond convention.

```java
class Box<T> {
    private T value;

    void set(T value) {
        this.value = value;
    }

    T get() {
        return value;
    }
}
```

Usage:

```java
Box<String> name = new Box<>();
Box<Integer> score = new Box<>();
```

No casts are needed when reading.

## Multiple type parameters

Use multiple parameters when the roles are different:

```java
class Pair<K, V> {
    private final K key;
    private final V value;

    Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
```

Common naming conventions:

| Name | Meaning |
| --- | --- |
| `T` | Type |
| `E` | Element |
| `K` | Key |
| `V` | Value |
| `R` | Return type |

These are conventions, not keywords.

## Generic methods

A method can be generic even if the class is not:

```java
public static <T> void print(T value) {
    System.out.println(value);
}
```

The method type parameter appears before the return type.

## Quick recall

- **Main benefit?** Compile-time type safety.
- **Why not `Object`?** It loses type information and pushes failures to runtime.
- **Generic method syntax?** Put `<T>` before the return type.
