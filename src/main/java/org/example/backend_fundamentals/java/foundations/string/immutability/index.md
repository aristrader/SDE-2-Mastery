---
order: 20
---

# String Immutability

Once a `String` is created, its character sequence cannot change. Methods that look mutating return a new string.

```java
String s = "hello";
s.toUpperCase();
System.out.println(s); // hello

s = s.toUpperCase();
System.out.println(s); // HELLO
```

## Why immutability matters

| Reason | Why it matters |
| --- | --- |
| String pool safety | many references can share one pooled string |
| Security | paths, class names, URLs, and credentials cannot change after validation |
| hashCode caching | cached hash stays valid for map/set lookup |
| Thread safety | readers do not need synchronization |

## Why `String` is final

`final` does not create immutability by itself. Private state plus no mutators create immutability. `final class String` protects that contract from subclasses that could otherwise introduce mutability.

## Quick recall

- **Does `toUpperCase()` mutate?** No, it returns a new string.
- **Why final?** To prevent subclasses from breaking the immutability contract.
- **Why safe as HashMap key?** Content and hash are stable.
