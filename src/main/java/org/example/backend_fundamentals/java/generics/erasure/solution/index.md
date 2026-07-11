---
order: 20
search: false
---

# Type Erasure Solutions

## Solution: type-erasure-runtime-class - Type Erasure

```java
Box<String> box1 = new Box<>();
Box<Integer> box2 = new Box<>();

System.out.println(box1.getClass());
System.out.println(box2.getClass());
System.out.println(box1.getClass() == box2.getClass()); // true
```

The compiler checks `Box<String>` and `Box<Integer>` at compile time. After that, type arguments are erased; there is only one runtime `Box.class`.

## Solution: static-generic-trap - Static Generic Trap

This does not compile:

```java
class Box<T> {
    // static T value;
}
```

Static fields belong to the single shared class, but `T` belongs to a parameterized instance such as `Box<String>` or `Box<Integer>`. Since there is only one erased `Box.class`, Java cannot create a separate static `T value` per type argument.

## Solution: erasure-traps - Type erasure traps

```java
Object unknown = List.of(1, 2, 3);

if (unknown instanceof List) {
    System.out.println("It is a List at runtime");
}

List<Integer> ints = new ArrayList<>();
List<String> strings = new ArrayList<>();
System.out.println(ints.getClass() == strings.getClass()); // true
```

`instanceof List<Integer>` is illegal because `Integer` is erased. These overloads are also illegal together:

```java
// void process(List<Integer> values) {}
// void process(List<String> values) {}
```

Both erase to the same runtime signature, so Java cannot distinguish them by generic type argument.
