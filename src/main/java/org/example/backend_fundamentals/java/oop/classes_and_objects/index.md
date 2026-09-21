---
order: 10
---

# Classes, Objects, and Constructors

The practical question is not “what syntax declares a class?” It is: **how do I make it impossible to create an object in an invalid state, and what has happened before its constructor body runs?** Those two answers drive bugs in entities, domain objects, and inheritance hierarchies.

## Class, object, and reference

A **class** declares state (fields) and behavior (methods). An **object** is one runtime instance of that class. A variable of class type holds a reference that can point at an object, point at a different object later, or be `null`.

```java
Order first = new Order("o-42");
Order alias = first;
alias.cancel();               // changes the same Order object
```

`first` and `alias` are separate reference variables; they do not contain two copies of the order. This distinction explains aliasing bugs and why passing an object to a method can let that method mutate its state.

A class extends at most one class but can implement multiple interfaces:

```java
class AuditJob extends BaseJob implements Runnable, AutoCloseable {
}
```

A top-level class is `public` or package-private, never `private` or `protected`. A source file may contain one public top-level class whose name matches the file name, plus package-private top-level classes. Nested classes have different access rules; cover them in the nested-classes topic.

## Constructors establish invariants

A constructor has the class name and no result type. `new` creates the instance and invokes the selected constructor; the constructor should establish the **invariants**—conditions that must be true for every usable instance.

```java
final class Percentage {
    private final int value;

    Percentage(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("percentage must be 0..100");
        }
        this.value = value;
    }
}
```

Do validation at this boundary when later code cannot safely interpret an invalid value. Do not use a constructor merely as a bag of setters: objects that can be half initialized force every later caller to re-check the same rules.

Java supplies a default no-argument constructor only when the class declares **no constructor at all**. Declaring any constructor removes it:

```java
class ConnectionConfig {
    ConnectionConfig(String host) {
    }
}

// new ConnectionConfig(); // does not compile
```

JPA entities need a public or protected no-argument constructor because the provider constructs them reflectively. Ordinary Spring components do not generally need one: constructor injection is the normal choice. Keep framework requirements specific rather than adding no-arg constructors everywhere.

## Constructor chains and initialization order

Constructors are not inherited. A subclass constructor either delegates with `this(...)` to another constructor in the same class, or invokes `super(...)` on its superclass; it cannot do both directly. If it does neither, Java inserts `super()` and compilation fails if the parent has no accessible no-argument constructor.

In this repository's Java 8 baseline, `this(...)` or `super(...)` must be the first statement. `this` and instance members are unavailable before superclass construction completes.

Trace this code from `new Child()`:

```java
class Parent {
    private final String parentField = mark("parent field");

    Parent() {
        mark("parent constructor");
    }
}

class Child extends Parent {
    private final String childField = mark("child field");

    {
        mark("child initializer block");
    }

    Child() {
        mark("child constructor");
    }
}
```

For the new object, storage is first prepared with default field values. Java then completes the superclass construction before the subclass: parent field/instance initializers, parent constructor body, child field/instance initializers in source order, then the child constructor body. The exact allocation strategy is a JVM implementation concern; the initialization ordering is the language contract that your code can rely on.

### The constructor dispatch trap

Do not call an overridable method from a constructor. Dynamic dispatch still selects the child override, but child fields have not been initialized yet.

```java
class Parent {
    Parent() {
        describe();
    }

    void describe() { }
}

class Child extends Parent {
    private String label = "ready";

    @Override
    void describe() {
        System.out.println(label); // can print null during Parent construction
    }
}
```

The naive design reuses a convenient hook. It fails because the hook observes partial subclass state. Prefer a `final`/`private` constructor helper, constructor arguments, or a factory that calls extension hooks only after construction completes.

## `this` is an instance reference

Use `this` to disambiguate a field from a parameter, to delegate to another constructor, to pass the current object, or to return the current object from a fluent API.

```java
class Customer {
    private final String id;

    Customer(String id) {
        this.id = id;
    }
}
```

There is no current object in a static method, so `this` is illegal there. Instance initialization blocks can share setup across constructors, but `this(...)` is usually clearer. Spring lifecycle callbacks solve a different problem: they run after dependency injection, not as a substitute for an object’s own invariants.

## Quick recall

- **What does a class-typed variable hold?** A reference to an object, or `null`; aliases can refer to the same object.
- **When is a default constructor generated?** Only when the class declares no constructor.
- **Are constructors inherited?** No; a subclass constructor delegates to a superclass constructor.
- **What is the constructor-order trap?** An overridden method can run before subclass fields are initialized.
- **Why use constructor validation?** It establishes invariants once, so later code can trust a usable object.
- **Can `this` appear in a static method?** No; static code has no current instance.
