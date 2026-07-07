---
order: 30
---

# Static and Final

> Deep dive into class-level vs instance-level members, and immutability guarantees.

---

## 1. The `static` Keyword

`static` members belong to the **class itself**, not to any specific object instance.

### Static Fields
There is only **one copy** of a static variable, regardless of how many objects are created (even if 0 objects exist).
```java
class Counter {
    static int count; // Shared across all instances
}
```

### Static Methods
Static methods can be called without creating an object (`ClassName.method()`).
- **Important:** A static method has no implicit `this`. Therefore, it **cannot** access instance variables or instance methods directly. It can only access other static members, unless it is explicitly passed an object reference.
- Static methods **cannot be overridden**. They are hidden/shadowed instead. Method dispatch for static methods is resolved at compile time based on the declared reference type (`invokestatic`).

### Static Initialization Blocks
```java
class Test {
    static {
        System.out.println("Loaded");
    }
}
```
A static block runs **exactly once** when the JVM initializes the class. 
*When does the JVM initialize the class?* Not necessarily at application startup! Initialization usually occurs on the first active use, such as:
1. Creating the first instance of the class.
2. Accessing a static field.
3. Invoking a static method.

### What else can be static?
Apart from fields, methods, and blocks, **nested classes** can also be static (see `../nested_classes/index.md`).

---

## 2. The `final` Keyword

The meaning of `final` depends on what you attach it to.

### Final Variables (Primitives)
```java
final int x = 10;
x++; // Compilation error! (equivalent to x = x + 1)
```
The value cannot be reassigned.

### Final References (Objects)
This is a high-ROI interview question: **Does `final` mean immutable?**
**No.** `final` on a reference means the *pointer* cannot be reassigned. The object itself can still be mutated!

```java
final List<String> list = new ArrayList<>();

list.add("A"); // LEGAL. The object is mutable.
list = new ArrayList<>(); // ILLEGAL. Cannot reassign the reference.
```

### Final Methods
A final method **cannot be overridden** by subclasses.

### Final Classes
A final class **cannot be extended**.
*Classic Interview Question:* Why is `String` marked as `final` in Java?
- It preserves **immutability**. If `String` could be subclassed, someone could create a mutable `String` implementation and pass it into methods expecting immutable strings, breaking security guarantees, correctness, and cached `hashCode` behavior relied upon by Java collections like `HashMap`.
