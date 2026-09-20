---
order: 20
---

# String Immutability

Once a `String` is created, its character sequence cannot change. Methods that look mutating return a new string; rebinding a variable is not mutation of the original object.

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

The mechanism is stable state: no caller can observe a partially modified value or invalidate a hash after insertion. That enables safe sharing and pooling. It does **not** make the variable holding the reference immutable—another thread can still reassign a shared field unless that field is safely published and protected.

## Why `String` is final

`final` does not create immutability by itself. Private state plus no mutators create immutability. `final class String` protects that contract from subclasses that could otherwise introduce mutability.

## Boundary: do not keep secrets in `String`

Immutability is a downside for passwords and other wipeable secrets: once created, a `String` cannot be cleared in place, so its lifetime is controlled by garbage collection. Prefer the API's credential type or a `char[]` when the API permits it, and clear that array in a `finally` block. This reduces exposure; it does not make secret handling magically safe.

## Unicode boundary

`String.length()` and `charAt()` count UTF-16 code units, not necessarily user-visible characters. A supplementary code point occupies two `char` values. For character-oriented validation or iteration, state whether the domain means code units, code points, or grapheme clusters before choosing the API.

## Quick recall

- **Does `toUpperCase()` mutate?** No, it returns a new string.
- **Why is `String` final?** To prevent a subclass from breaking its immutable contract.
- **Why safe as HashMap key?** Its content and hash cannot change after insertion.
- **Does immutable `String` make a shared field safe?** No; publication and reassignment are separate concerns.
- **Why avoid String for a password?** It cannot be wiped deterministically.
