---
order: 20
search: false
---

# Solutions

## Solution: string-pool-equality - String Pool vs Heap

```java
String a = "hello";
String b = "hello";
String c = new String("hello");

System.out.println(a == b);      // true
System.out.println(a == c);      // false
System.out.println(a.equals(c)); // true
```
String literals go into the String Pool, so `a` and `b` point to the exact same pooled object. Using `new String()` forces the creation of a brand new object on the regular heap, bypassing the pool. `==` checks identity (memory address), so `a == c` is false. `.equals()` checks content, which is true.

## Solution: stringbuilder-loop - StringBuilder in Loops

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append(i);
}
String result = sb.toString();
```
Strings are immutable. If you use `s += i` in a loop, Java creates a brand new `String` object on every iteration, copying the old contents over, and discarding the previous string to garbage collection. `StringBuilder` uses a mutable internal `char[]` buffer, modifying it in place without creating intermediate string objects.
