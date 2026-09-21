---
order: 70
---

# Exceptions

An exception is a non-local control-flow transfer: normal work stops at the throw site and Java searches outward through the call stack for a matching handler. It belongs in Foundations because this is Java control flow; framework-specific HTTP mapping comes later.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `hierarchy` | `Throwable`, `Error`, checked vs unchecked, `throw` vs `throws`. |
| 2 | `propagation` | Catching, declaring, and call-stack propagation. |
| 3 | `finally_resources` | `finally`, return traps, try-with-resources, and suppressed exceptions. |
| 4 | `design_custom` | Custom exceptions, translation, and checked-vs-unchecked design choices. |

## Design shortcut

| Situation | Usual choice |
| --- | --- |
| JVM-level failure | do not catch `Error` in normal code |
| caller has a realistic, required recovery action | checked exception or explicit result |
| violated precondition or programming mistake | unchecked exception |
| expected domain outcome, such as rejected credentials | result when it is part of normal flow |
| request-invalidating domain failure | unchecked domain exception mapped at the boundary |
| lower-layer failure crossing a boundary | translate and preserve the cause |

Do not turn every failed `if` into an exception. The type should tell the next owner what it can do; if the caller merely branches on an expected outcome, a result is usually clearer.

## Interview answer shape

For an exception-design question, answer in this order: name the failure, say who can recover, choose handle/declare/translate, then say how cleanup and the original cause survive. That prevents the common answer of “catch `Exception` and log it,” which neither recovers nor gives the caller a useful contract.

## Quick recall

- **`throw` vs `throws`?** `throw` throws an object now; `throws` declares checked exceptions on a method.
- **Checked exception means?** The compiler requires a catch or a compatible `throws` declaration.
- **Unchecked exception means?** Compiler does not force handling.
- **Resource cleanup default?** Try-with-resources.
- **Returning from `finally`?** Avoid it; it can suppress the real return or exception.
