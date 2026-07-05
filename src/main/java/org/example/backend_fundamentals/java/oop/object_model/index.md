---
order: 100
---

# Java Object Model

> The rules the JVM uses to represent, store, and dispatch against every object. Understanding this makes `==` vs `.equals()`, reference semantics, virtual dispatch, and casting click into place.

---

## The 7 load-bearing rules

### 1. Objects live on the heap; references live on the stack

```java
Dog d = new Dog(); // heap: the Dog object  |  stack: `d` (a 4/8-byte pointer)
```

When you assign or pass `d`, you copy the **pointer**, not the object. Two variables can point at the same object.

---

### 2. All variables are references (except primitives)

```java
Dog a = new Dog("Rex");
Dog b = a;         // b holds the same reference — one object, two handles
b.setName("Max");
System.out.println(a.getName()); // "Max" — a and b see the same object
```

Java is **pass-by-value of the reference**. The callee gets a copy of the pointer; it can mutate the pointed-to object, but cannot make the caller's variable point elsewhere.

Think of a piece of paper with an address written on it. Java hands the callee a **photocopy**:

- The callee can go to that address and repaint the house (`d.setName("Max")`) — the caller sees the change because it's the same object.
- If the callee writes a new address on its copy (`d = new Dog(...)`), the caller's original paper is untouched.

```java
void rename(Dog d) {
    d.setName("Max");   // mutates the object — caller sees this
    d = new Dog("Rex"); // reassigns the LOCAL copy — caller doesn't see this
}

Dog dog = new Dog("Rex");
rename(dog);
System.out.println(dog.getName()); // "Max", not "Rex"
```

Demo: `PassByValueRun.java` in this package.

---

### 3. Every class implicitly extends `Object`

`Object` is the root of the class hierarchy. Methods you get for free:

| Method | Default behaviour |
|--------|-------------------|
| `equals(Object o)` | `==` (reference identity) |
| `hashCode()` | derived from identity |
| `toString()` | `ClassName@hashCode` |
| `getClass()` | runtime class of the object |
| `clone()` | shallow copy (requires `Cloneable`) |
| `wait()` / `notify()` / `notifyAll()` | intrinsic lock operations |

Override `equals` + `hashCode` together or neither — the contract requires both.

---

### 4. `==` tests reference identity; `.equals()` tests logical equality

```java
String s1 = new String("hello");
String s2 = new String("hello");

s1 == s2        // false — different heap objects
s1.equals(s2)   // true  — same character sequence
```

String literals are interned (pooled), so `"hello" == "hello"` is `true` — but don't rely on that. Always use `.equals()`.

---

### 5. Every object has a header the JVM uses internally

The JVM prefixes every object with two words (64-bit JVM, compressed oops):

| Word | Contents |
|------|----------|
| **Mark word** (8 bytes) | identity hash, lock state, GC age |
| **Class pointer** (4 bytes compressed) | pointer to the class metadata (`.class` file) |

Arrays add a third word for the length. You never touch the header directly, but it explains:
- Why `Object.hashCode()` can return identity-based values even before you call it.
- Why locking (`synchronized`) is "free" — the mark word already has the slot.

---

### 6. Method dispatch is virtual by default

When you call a method on a reference, the JVM looks up the **actual runtime type** of the object, not the declared variable type. This is `invokevirtual`.

```java
Animal a = new Dog();
a.speak(); // calls Dog.speak(), not Animal.speak()
```

Exceptions (not virtual):
- `static` methods → `invokestatic` (bound at compile time to the declared type).
- `private` methods → `invokespecial` (not overridable, so no dispatch needed).
- `final` methods → JIT can de-virtualize as an optimization.

This is why hiding a `static` method in a subclass isn't overriding — it just shadows the name.

---

### 7. Casting and `instanceof` are runtime checks

Widening (subtype → supertype) is implicit and safe:

```java
Dog d = new Dog();
Animal a = d;  // always safe — Dog IS-A Animal
```

Narrowing (supertype → subtype) requires an explicit cast and is checked at runtime:

```java
Animal a = new Dog();
Dog d = (Dog) a;     // ok — runtime type really is Dog
Cat c = (Cat) a;     // ClassCastException at runtime
```

Use `instanceof` before narrowing to guard:

```java
if (a instanceof Dog d) {  // pattern match (Java 16+)
    d.fetch();
}
```

---

## `equals` + `hashCode` contract

The contract matters most when objects go into `HashMap`, `HashSet`, or any hash-based structure.


| Rule | Consequence of breaking it |
|------|---------------------------|
| If `a.equals(b)` → `a.hashCode() == b.hashCode()` | Objects that are "equal" land in different buckets → `get` never finds them |
| If `a.hashCode() == b.hashCode()` → `a.equals(b)` may or may not be true | Collisions are allowed — equality is the tiebreaker |
| `equals` must be reflexive, symmetric, transitive, consistent, and `null`-safe | Violating any rule breaks collections silently |

---

## Object lifecycle

```
new Dog()
  │
  ▼
heap allocation  (JVM zeroes the memory: ints → 0, refs → null, booleans → false)
  │
  ▼
constructor runs  (super() first — Object's ctor, then up the chain, then yours)
  │
  ▼
object in use
  │
  ▼
no more reachable references  →  eligible for GC
  │
  ▼
GC collects  (finalize() deprecated; use try-with-resources or Cleaner instead)
```

Key point: fields are zeroed **before** your constructor body runs, so an unset `int` field is `0`, not garbage.

---

## Quick recall

**Q. What does "Java is pass-by-value" mean for objects?**
A. The reference (pointer) is copied. The callee can mutate the object but cannot make the caller's variable point elsewhere.

**Q. Why does `new String("x") == new String("x")` return `false`?**
A. Two different heap objects; `==` compares addresses. Use `.equals()`.

**Q. What breaks when you override `equals` but not `hashCode`?**
A. Equal objects can have different hash codes → they land in different buckets in `HashMap`/`HashSet` → `get`/`contains` silently fails.

**Q. Why can't you override a `static` method?**
A. Static methods use `invokestatic`, bound to the declared type at compile time. There's no runtime lookup — hiding it in a subclass just shadows the name.

**Q. What happens to fields before your constructor runs?**
A. JVM zeroes them: `int` → `0`, reference → `null`, `boolean` → `false`. Your constructor then sets them.

**Q. When does a `ClassCastException` occur?**
A. At runtime during a narrowing cast when the actual type of the object doesn't match the target type.
