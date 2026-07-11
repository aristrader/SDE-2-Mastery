---
order: 20
---

# Checked Handling and Propagation

Checked exceptions force a choice at each method boundary:

1. handle with `try-catch`
2. declare with `throws`
3. translate to a more useful exception at a boundary

Propagation is just the exception walking back up the call stack until a matching catch block handles it. If nothing handles it, the thread terminates with a stack trace.

## Quick recall

- **Catch where?** Where you can recover or add useful context.
- **Declare when?** When the caller is the right owner of the failure.
- **Translate when?** At layer boundaries, while preserving the cause.
