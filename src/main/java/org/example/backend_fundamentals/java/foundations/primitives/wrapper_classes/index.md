---
order: 30
---

# Wrapper Classes

Every primitive in Java (`int`, `boolean`, `double`, etc.) has a corresponding object "Wrapper" class (`Integer`, `Boolean`, `Double`, etc.).

---

## Why Wrapper Classes exist

1. **Generics:** Java Generics (`List<T>`, `Map<K, V>`) do not support primitives. You cannot write `List<int>`. You must use `List<Integer>`.
2. **Nullability:** Primitives cannot be `null`. An `int` defaults to `0`. If you need to represent the *absence* of a value (e.g., in a database mapping or API payload), you must use the `Integer` object, which can be `null`.
3. **Utility Methods:** Wrapper classes provide helpful static methods, like `Integer.parseInt("123")` or `Double.isNaN(value)`.

---

## Autoboxing and Unboxing

Since Java 5, the compiler automatically converts between primitives and their wrapper objects to make code cleaner.

**Autoboxing:** Converting a primitive to a wrapper.
```java
Integer a = 10; // Compiler rewrites to: Integer.valueOf(10)
```

**Unboxing:** Converting a wrapper to a primitive.
```java
int b = a; // Compiler rewrites to: a.intValue()
```

**The Danger of Unboxing:** If the wrapper object is `null`, unboxing it throws a `NullPointerException`.
```java
Integer count = null;
int c = count; // Throws NullPointerException! (Calling null.intValue())
```

---

## The Integer Cache Trap

Wrapper classes are Objects. To compare objects, you should always use `.equals()`.
However, because of the "Integer Cache", using `==` sometimes gives the illusion that it works.

Java caches `Integer` objects for values between **-128 and 127**.
When you use autoboxing (`Integer a = 100`), Java returns a shared, pre-allocated object from the cache.

```java
Integer a = 100;
Integer b = 100;
System.out.println(a == b); // true! Both point to the exact same cached object.

Integer x = 1000;
Integer y = 1000;
System.out.println(x == y); // false! 1000 is outside the cache. Two separate objects are created.
System.out.println(x.equals(y)); // true. Content is identical.
```

**Rule:** Never use `==` to compare Wrapper objects. Always use `.equals()`.

---

## Quick recall

**Q. What is autoboxing?**
A. The automatic conversion the Java compiler makes between the primitive types and their corresponding object wrapper classes (e.g., `int` to `Integer`).

**Q. When does unboxing throw a NullPointerException?**
A. When the wrapper reference is `null` and the compiler attempts to extract the primitive value (e.g., assigning a `null` `Integer` to an `int` variable).

**Q. Why does `Integer a = 100; Integer b = 100; a == b;` return true, but fails for 1000?**
A. Java caches wrapper objects for values between -128 and 127. Values in this range return the exact same cached reference, so `==` (reference comparison) succeeds. 1000 is outside the cache, so two distinct objects are created.
