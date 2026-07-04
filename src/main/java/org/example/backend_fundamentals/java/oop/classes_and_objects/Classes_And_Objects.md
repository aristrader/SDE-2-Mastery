# Classes, Objects, and Constructors

> A comprehensive overview of Java's core object-oriented building blocks, focusing on interview-relevant concepts.

---

## 1. Classes vs. Objects

### Classes
A **class** is a blueprint. It defines what methods and attributes (fields) an object can have.
- A class can **extend exactly one class** (Java does not support multiple inheritance for classes).
- A class can **implement multiple interfaces**.

```java
// Correct Java syntax
class Dog extends Animal implements Runnable, Serializable, Cloneable {
}
```

Top-level classes can only be `public` or `package-private`. They **cannot** be `private` or `protected`. 
*Why?* If a top-level class were `private`, no other class could access it, making it completely unusable. (Nested classes, however, can be `private`).

Furthermore, **only one public top-level class is allowed per `.java` file**, and the filename must match the class name exactly. Multiple non-public top-level classes are allowed in the same file.

### Objects
An **object** is a concrete instance of a class.

```java
Person p = new Person("Swapnil");
```
Here:
- `Person` is the **class**
- `p` is the **reference variable**
- `new Person(...)` creates the **object** (instance) on the heap.

**Key Interview Point:** Java variables hold *references* to objects, not the objects themselves. Objects never have access modifiers (only fields, methods, constructors, and classes do).

---

## 2. Constructors

Constructors initialize objects. They are essential when mandatory fields must always be initialized during creation.

### Default Constructor
If you write a class with **no** constructor, Java automatically generates a no-argument default constructor:
```java
class A { } 
// Java generates: A() { }
```

### Custom Constructors
If you write **any** constructor, Java stops generating the default no-argument constructor.
```java
class A {
    A(int x) { }
}
```
Now, `new A()` will fail to compile. This is a very common source of errors in frameworks like Spring/Hibernate, which often require a no-arg constructor to instantiate beans or entities via reflection.

### Constructor Chaining (`this` and `super`)
You can chain constructors to avoid duplicated initialization logic:
- `this(...)` calls another constructor in the **same** class.
- `super(...)` calls a constructor in the **parent** class.

**Rule:** `this(...)` or `super(...)` must be the **very first statement** inside the constructor.

Constructors are **not inherited**. A child class does not inherit its parent's constructors; instead, the child's constructor automatically calls `super()` (the parent's no-arg constructor) unless you explicitly call a different `super(...)`.

---

## 3. The `this` Keyword

`this` refers to the current object. 
- **Resolving shadowing:** Distinguishing instance fields from parameters (`this.name = name;`).
- **Constructor chaining:** Calling another constructor (`this("Unknown");`).
- **Returning current object:** Useful in builder patterns (`return this;`).
- **Passing current object:** Passing the current instance to another method (`foo(this);`).

*Note:* `this` cannot be used inside `static` methods, because static methods belong to the class, not to any specific object instance.

---

## 4. Object Creation Lifecycle

What exactly happens when you call `new Person()`?
1. **JVM allocates memory** on the heap.
2. **Fields receive default values** (e.g., `int` becomes `0`, references become `null`).
3. **Field initializers execute** (`int x = 5;`).
4. **Instance initialization blocks execute** (e.g., `{ System.out.println("Init"); }`).
5. **Constructor executes**.
6. **Reference is returned**.

### Instance Initialization Blocks
Instance blocks `{ ... }` run before the constructor. They were originally used to share initialization logic across multiple constructors.
However, in modern Java and Spring Boot, they are rarely used. Constructor chaining (`this()`) or lifecycle callbacks (like `@PostConstruct`) are highly preferred for readability.
