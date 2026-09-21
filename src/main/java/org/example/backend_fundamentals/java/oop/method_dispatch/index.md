---
order: 120
---
# Dispatch: what Java decides now and later

The compiler first proves that a call is legal through the **declared reference type**. For an overridable instance method, the JVM then chooses the implementation from the **runtime object type**. Keep those two decisions separate.

```java
Animal animal = new Dog();
animal.sound();
```

The compiler requires `Animal` to declare or inherit `sound()`. At runtime, Java invokes `Dog.sound()` if `Dog` overrides it. A `Dog`-only method is not callable through `animal` without a safe type boundary such as a pattern/type check or an explicit cast.

## Overloading is not overriding

**Overloading** is compile-time selection among methods with the same name and different parameter lists. **Overriding** supplies a subclass implementation for an inherited instance method. Parameter types must match the inherited method after Java's overriding rules; a covariant return type may narrow the return to a subtype.

```java
class Animal {
    void feed(Number amount) { }
    String sound() { return "animal"; }
}

class Dog extends Animal {
    void feed(Integer amount) { }       // overload, not override

    @Override
    String sound() { return "dog"; }   // override
}
```

The naive failure is assuming `Dog.feed(Integer)` replaces `Animal.feed(Number)`. It does not: an `Animal` reference calling `feed(1)` selects `feed(Number)` at compile time, then dynamic dispatch can only vary an actual override of that selected signature. `@Override` would reject the accidental overload immediately.

## What is and is not dynamically dispatched

| Member | Selected from | Consequence |
| --- | --- | --- |
| overridable instance method | runtime object type | subtype behavior can vary |
| `final` instance method | inherited declaration | subclasses cannot replace it |
| `private` method | declaring class | it is not inherited or overridden |
| `static` method | compile-time reference/class | same-named subclass method hides it |
| field | compile-time reference type | same-named subclass field hides it |

```java
Animal animal = new Dog();
animal.sound();             // Dog implementation
animal.identify();          // Animal static method
System.out.println(animal.name); // Animal field
```

Do not call static methods through an instance; use `Animal.identify()`. It makes the compile-time binding visible and avoids an IDE warning.

## Override boundaries

An override may widen access (`protected` to `public`) but not narrow it. It may throw fewer or narrower checked exceptions than the inherited declaration, never broader checked ones. These rules preserve the contract that callers compiled against the parent type already understand.

Prefer a composition boundary when a subclass cannot honor that contract. Forcing an override that rejects normal parent inputs is a design smell, not a dispatch trick.

## Quick recall

- **What does the compiler check for `Animal a = new Dog(); a.sound()`?** That `Animal` exposes `sound()`.
- **What chooses the overriding implementation?** The runtime `Dog` object.
- **Does a different parameter type override?** No; it creates an overload.
- **Can an override reduce visibility or broaden checked exceptions?** No.
- **Are static methods and fields polymorphic?** No; same-named members are hidden using compile-time type rules.
- **Why use `@Override`?** It catches a declaration that accidentally overloads instead of overrides.
