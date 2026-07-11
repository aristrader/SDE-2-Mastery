---
order: 40
---

# Type Erasure

Generics are checked by the compiler and then mostly erased from bytecode.

```java
List<String> names = new ArrayList<>();
String first = names.get(0);
```

After erasure, the runtime shape is roughly:

```java
List names = new ArrayList();
String first = (String) names.get(0);
```

The compiler inserts the cast because it already proved the code type-safe.

## Same runtime class

There is not a separate class for each type argument:

```java
Box<String> one = new Box<>();
Box<Integer> two = new Box<>();

System.out.println(one.getClass() == two.getClass()); // true
```

Both are just `Box.class` at runtime.

## Common erasure traps

These do not work because type arguments are gone at runtime:

```java
// if (list instanceof List<String>) {}

// void process(List<Integer> values) {}
// void process(List<String> values) {}
```

Both overloads erase to `process(List)`.

## Static type-parameter trap

Class type parameters belong to parameterized instances. Static fields belong to the one shared erased class:

```java
class Box<T> {
    // static T value; // compile error
}
```

There is only one `Box.class`, not one static field per `Box<String>`, `Box<Integer>`, and so on.

## Quick recall

- **When does erasure happen?** After compile-time checking.
- **Runtime class for `Box<String>` and `Box<Integer>`?** Same `Box.class`.
- **Why no `static T value`?** Static state belongs to the shared erased class.
