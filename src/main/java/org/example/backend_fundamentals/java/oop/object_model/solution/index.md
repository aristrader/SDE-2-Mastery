---
order: 20
search: false
---

# Solutions

## Solution: pass-by-value - Pass by Value of the Reference

```java
public class Main {
    static void rename(Dog d) {
        d.setName("Max");   // mutates the object on the heap — caller sees this
        d = new Dog("Fido"); // reassigns the LOCAL pointer copy — caller doesn't see this
    }

    public static void main(String[] args) {
        Dog dog = new Dog("Rex");
        rename(dog);
        System.out.println(dog.getName()); // Prints "Max"
    }
}
```
Java is pass-by-value. The callee receives a copy of the pointer. It can follow the pointer to mutate the heap object, but it cannot change where the caller's variable points.

## Solution: reference-equality - The `==` operator vs `.equals()`

```java
String s1 = new String("hello");
String s2 = new String("hello");

System.out.println(s1 == s2);      // false
System.out.println(s1.equals(s2)); // true
```
The `==` operator checks if the two references point to the exact same memory address on the heap. Because we used the `new` keyword twice, there are two distinct objects. `.equals()` is overridden by the `String` class to check logical content equality.

## Solution: narrowing-cast - ClassCastException at runtime

```java
Animal a = new Dog();
// Cat c = (Cat) a; // COMPILES fine, but throws ClassCastException at runtime
```
It compiles because the compiler only knows that `a` is an `Animal`, and an `Animal` *could* be a `Cat`. However, at runtime, the JVM checks the actual object type on the heap. Since it is a `Dog`, it cannot be cast to a `Cat`, throwing the exception. Always use `instanceof` to guard narrowing casts.
