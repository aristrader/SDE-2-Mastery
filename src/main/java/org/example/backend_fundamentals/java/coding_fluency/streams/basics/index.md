---
order: 10
---

# Stream Collection Basics

The first collector decision is the result type and mutability.

| Collector | Produces | Notes |
| --- | --- | --- |
| `Collectors.toList()` | mutable list | Usually an `ArrayList`; available since Java 8. |
| `Stream.toList()` | unmodifiable list | Java 16+. Prefer when the caller should not mutate. |
| `Collectors.toUnmodifiableList()` | unmodifiable list | Java 10-15 equivalent style. |
| `Collectors.toSet()` | mutable set | Deduplicates; no order guarantee. |
| `Collectors.toUnmodifiableSet()` | unmodifiable set | Java 10+. |

Choose mutability intentionally. Returning a mutable list invites callers to change it; returning an unmodifiable list documents that the stream result is final.

## Quick recall

- **Mutable list collector?** `Collectors.toList()`.
- **Unmodifiable list on Java 16+?** `stream.toList()`.
- **Set collector guarantee order?** No, not unless you collect into an ordered set explicitly.
