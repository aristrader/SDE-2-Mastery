---
order: 10
---

# Foundations

Java foundations are the language basics used by every later module: primitive values, wrappers, arrays, strings, control flow, enums, and exception handling. Exceptions stay here because they are part of Java's core control-flow and error model, even though production exception design comes back later in backend topics.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `primitives` | Value types, defaults, and pass-by-value basics. |
| 2 | `wrapper_semantics` | Boxing, unboxing, wrapper nullability, and `Integer` cache traps. |
| 3 | `arrays` | Fixed-size indexed storage before collection APIs. |
| 4 | `string` | Immutability, comparison, builders, and common string traps. |
| 5 | `control_flow` | Branching, loops, switch, and operators. |
| 6 | `enums` | Type-safe constants with fields and behavior. |
| 7 | `exceptions` | Checked vs unchecked errors and handling strategy. |

## Quick recall

- **Need a nullable number?** Use a wrapper such as `Integer`, not `int`.
- **Need resizable storage?** Move from arrays to collections.
- **Need named fixed states?** Use an enum, not string constants.
- **Need recoverable handling?** Catch narrowly; do not swallow exceptions.
