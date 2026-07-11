---
order: 10
---

# String Pool

String literals are interned in the string pool. Equal literals reuse the same pooled object.

```java
String a = "hello";
String b = "hello";
System.out.println(a == b); // true
```

`new String("hello")` forces a separate object:

```java
String c = new String("hello");
System.out.println(a == c);      // false
System.out.println(a.equals(c)); // true
```

The pool is inside the heap in modern Java. It is still garbage-collected like other heap data when strings become unreachable.

## Quick recall

- **Literals with same text?** Same pooled object.
- **`new String(...)`?** Separate object.
- **Content comparison?** `.equals()`.
