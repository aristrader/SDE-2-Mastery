---
order: 20
search: false
---

# Solutions

## Solution: dynamic-dispatch - Dynamic Method Dispatch

```java
class DispatchDemo {
    static class Animal { void sound() { System.out.println("Animal"); } }
    static class Dog extends Animal { @Override void sound() { System.out.println("Dog"); } }

    public static void main(String[] args) {
        Animal animal = new Dog();
        animal.sound(); // Dog
    }
}
```
Because instance methods are dispatched dynamically at runtime based on the **actual object type** on the heap (which is a `Dog`), not the declared variable type (`Animal`).

## Solution: static-shadowing - Static Method Shadowing

```java
class StaticDispatchDemo {
    static class Animal { static void identify() { System.out.println("Static Animal"); } }
    static class Dog extends Animal { static void identify() { System.out.println("Static Dog"); } }

    public static void main(String[] args) {
        Animal animal = new Dog();
        animal.identify(); // Static Animal; prefer Animal.identify()
    }
}
```
Static methods use `invokestatic`, which binds at **compile time** to the declared type (`Animal`). There is no runtime lookup. The `Dog` class is merely shadowing the name, not overriding the behavior.

## Solution: field-shadowing - Fields are not polymorphic

```java
class FieldDispatchDemo {
    static class Animal { String name = "Animal"; }
    static class Dog extends Animal { String name = "Dog"; }

    public static void main(String[] args) {
        Animal animal = new Dog();
        System.out.println(animal.name); // Animal
    }
}
```
Fields are **not** polymorphic in Java. Field access is resolved entirely at compile time using the reference variable's type. This is why you should always encapsulate fields behind polymorphic getter methods.

## Solution: overload-or-override - Signature Near Miss

```java
class OverloadOverrideDemo {
    static class Animal {
        void feed(Number amount) { System.out.println("Animal"); }
    }

    static class DogOverload extends Animal {
        void feed(Integer amount) { System.out.println("Dog overload"); }
    }

    static class DogOverride extends Animal {
        @Override
        void feed(Number amount) { System.out.println("Dog override"); }
    }

    public static void main(String[] args) {
        Animal overloaded = new DogOverload();
        overloaded.feed(1); // Animal

        Animal overridden = new DogOverride();
        overridden.feed(1); // Dog override
    }
}
```

The compiler selects `Animal.feed(Number)` from the declared type and argument. `Dog.feed(Integer)` has a different parameter type, so it is a new overload, not an implementation of that selected method.

`DogOverload.feed(Integer)` is an overload, so the compiler selects `Animal.feed(Number)` through the declared `Animal` type. `DogOverride.feed(Number)` overrides that selected signature, so dynamic dispatch invokes it at runtime. `@Override` is the cheap guard that distinguishes the two cases.
