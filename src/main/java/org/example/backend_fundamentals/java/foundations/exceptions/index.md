---
order: 70
---

# Exceptions

Exceptions are Java's structured failure mechanism. They stay in Foundations because they are part of core control flow; Spring-specific exception handling comes later.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `hierarchy` | `Throwable`, `Error`, checked vs unchecked, `throw` vs `throws`. |
| 2 | `propagation` | Catching, declaring, and call-stack propagation. |
| 3 | `finally_resources` | `finally`, return traps, try-with-resources, and suppressed exceptions. |
| 4 | `design_custom` | Custom exceptions, translation, and checked-vs-unchecked design choices. |

## Decision shortcut

| Situation | Usual choice |
| --- | --- |
| JVM-level failure | do not catch `Error` in normal code |
| recoverable external failure | checked exception or explicit result |
| programming bug | unchecked exception |
| domain validation failure | result or unchecked domain exception |
| lower-layer failure crossing a boundary | translate and preserve the cause |

## Quick recall

- **`throw` vs `throws`?** `throw` throws an object now; `throws` declares checked exceptions on a method.
- **Checked exception means?** Caller must catch or declare it.
- **Unchecked exception means?** Compiler does not force handling.
- **Resource cleanup default?** Try-with-resources.
- **Returning from `finally`?** Avoid it; it can suppress the real return or exception.
