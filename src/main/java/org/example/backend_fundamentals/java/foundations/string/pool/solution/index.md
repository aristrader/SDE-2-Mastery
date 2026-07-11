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

`a` and `b` point to the same pooled literal. `c` points to a separate heap object. `.equals()` compares content.
