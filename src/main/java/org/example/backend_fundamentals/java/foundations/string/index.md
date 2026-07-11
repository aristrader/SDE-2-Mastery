---
order: 40
---

# Strings

Strings are immutable object values with special JVM support through literals and the string pool.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `pool` | Literals, `new String(...)`, `==` vs `.equals()`, and interning. |
| 2 | `immutability` | Why `String` is immutable and why the class is `final`. |
| 3 | `builder` | Efficient string construction with `StringBuilder`. |
| 4 | `substring_history` | The old Java 6 substring memory-retention trap. |

## Quick recall

- **Use `==` for String content?** No, use `.equals()`.
- **Literal `"hello"` lives where?** String pool.
- **`new String("hello")` does what?** Creates a separate heap object.
- **StringBuilder use case?** Building text in loops or multiple steps.
