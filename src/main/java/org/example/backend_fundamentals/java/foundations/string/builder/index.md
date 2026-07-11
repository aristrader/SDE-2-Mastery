---
order: 30
---

# StringBuilder

Use `StringBuilder` when building a string through repeated appends.

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append(i);
}
String result = sb.toString();
```

Loop concatenation creates unnecessary intermediate strings:

```java
String result = "";
for (int i = 0; i < 1000; i++) {
    result = result + i;
}
```

Single-statement concatenation is usually optimized by the compiler/JVM. The common problem is repeated concatenation inside loops.

## Quick recall

- **Loop string building?** Use `StringBuilder`.
- **Thread-safe?** No; confine it to one thread.
- **Final output?** Call `toString()`.
