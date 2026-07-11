---
order: 40
---

# Custom Exceptions and Design

Create a custom exception when the exception type communicates a useful domain or boundary meaning.

Keep exception objects simple:

- call `super(message)`
- call `super(message, cause)` when translating
- add fields only for useful structured data
- avoid setters

Modern Spring-style backend code usually favors unchecked domain exceptions plus centralized handling. Checked exceptions still fit when callers can realistically recover.

## Quick recall

- **Preserve root cause?** Pass it as `cause`.
- **Own `message` field?** No, `Throwable` already has one.
- **Custom checked exception?** When caller recovery is part of the contract.
- **Custom unchecked exception?** Programming error or domain validation failure.
