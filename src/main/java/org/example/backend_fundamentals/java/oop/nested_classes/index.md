---
order: 90
---

# Nested Classes

Nested classes are classes declared inside another class. They are useful when a helper type belongs tightly to one outer type and should not be exposed as a separate top-level concept.

Java has two interview-relevant forms:

- **Static nested class**: does not need an outer object.
- **Inner class**: non-static nested class; needs an outer object and can access outer instance members.

## Static Nested Class

```java
class Outer {
    static class Helper {
        void run() {
            System.out.println("No outer instance needed");
        }
    }
}

Outer.Helper helper = new Outer.Helper();
helper.run();
```

A static nested class behaves like a normal class scoped inside its outer class. It can access static members of the outer class directly, but it cannot access outer instance fields unless you pass an outer object explicitly.

Use it when the nested type is conceptually owned by the outer type but does not need per-object state from the outer instance.

## Inner Class

```java
class Outer {
    private final String name = "outer";

    class Inner {
        void print() {
            System.out.println(name);
        }
    }
}

Outer outer = new Outer();
Outer.Inner inner = outer.new Inner();
inner.print();
```

An inner class carries an implicit reference to its enclosing `Outer` instance. That is why it is created with `outer.new Inner()`.

Use it when the nested object is truly bound to one outer object and needs direct access to its instance state.

## Static Context Rule

`main()` is static, so it has no implicit outer object. That means:

```java
StaticNested nested = new StaticNested(); // ok
Inner inner = new Inner();                // not ok inside static context
```

For an inner class, create the outer object first:

```java
Outer outer = new Outer();
Outer.Inner inner = outer.new Inner();
```

## Interview Distinction

| Question | Static nested class | Inner class |
| --- | --- | --- |
| Needs outer instance? | No | Yes |
| Can be created from `main()` directly? | Yes | No |
| Has implicit outer reference? | No | Yes |
| Can access outer instance fields directly? | No | Yes |
| Best use | Scoped helper type | Object tied to outer state |

## Common Gotcha

An inner class can accidentally keep the outer object alive because it stores an implicit reference to it. For long-lived callbacks, listeners, or background tasks, prefer a static nested class unless you truly need the outer instance.

## Demo code in this folder

- `NestedClassDemo.java` shows static nested class creation, inner class creation, and the outer-instance access rule.

## Quick recall

**Q. What is the main difference between a static nested class and an inner class?**
A. A static nested class does not need an outer object. An inner class needs an outer object and has an implicit reference to it.

**Q. Why does `new Inner()` fail inside `main()`?**
A. `main()` is static and has no current outer instance. Use `outer.new Inner()`.

**Q. Why prefer static nested classes for helpers?**
A. They avoid an implicit outer reference and make ownership clearer.
