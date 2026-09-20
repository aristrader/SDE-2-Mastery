---
order: 40
---

# Strings

The interview problem is separating **text value** from **object identity** while avoiding accidental allocation in hot paths. `String` is an immutable object; Java gives literals and constant expressions special interning support.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `pool` | Predict literals, `new String(...)`, compile-time constants, `==`, `.equals()`, and `intern()`. |
| 2 | `immutability` | Explain stable hash keys, safe sharing, and the secret-data exception. |
| 3 | `builder` | Repair repeated-copy construction without making a mutable buffer shared state. |
| 4 | `substring_history` | Recognize the old Java 6 retention trap without misdiagnosing modern code. |

## Rules worth saying out loud

- Compare text with `expected.equals(actual)` when `expected` is known non-null; `==` asks whether two references denote the same object.
- Do not use `new String(existingString)` unless an API specifically requires a distinct object. It does not make text safer or more immutable.
- Use normal `+` for a readable, fixed expression. Use a local `StringBuilder` for repeated, data-dependent construction.
- A Java `String` is a sequence of UTF-16 code units. User-visible character logic may need code points (`codePoints()`), not `char` indexing.

## Quick recall

- **Use `==` for String content?** No, use `.equals()`.
- **Why can `==` appear to work for literals?** Equal literals are interned to one reference; that is not a content-comparison rule.
- **`new String("hello")` does what?** Produces a distinct `String` object with equal content.
- **StringBuilder use case?** Building text in loops or multiple steps.
- **Can `char` always represent one displayed character?** No; some Unicode code points use two UTF-16 code units.
