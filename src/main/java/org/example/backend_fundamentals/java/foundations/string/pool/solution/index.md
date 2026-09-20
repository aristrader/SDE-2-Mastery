---
order: 20
search: false
---

# String Pool Solutions

## Solution: string-pool-equality - String Pool vs Heap

```java
String a = "hello";
String b = "hello";
String c = new String("hello");

System.out.println(a == b);      // true
System.out.println(a == c);      // false
System.out.println(a.equals(c)); // true
```

`a` and `b` point to the same interned literal. `c` is a distinct object with the same content. `.equals()` compares content, so it is the production comparison.

```java
String d = "he" + "llo";
String prefix = "he";
String e = prefix + "llo";

System.out.println(a == d);      // true: compile-time constant expression
System.out.println(a == e);      // false: run-time concatenation result
System.out.println(a.equals(e)); // true
```

The `a == e` result is a demonstration of construction, not a comparison technique. Use `a.equals(e)` (or `Objects.equals(a, e)` when both may be null).
