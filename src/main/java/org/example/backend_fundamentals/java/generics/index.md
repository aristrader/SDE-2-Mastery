---
order: 40
---

# Generics

Generics make Java APIs reusable without falling back to `Object` and casts. Study this after collections: most useful generic examples are `List<T>`, `Map<K, V>`, and collection helper methods.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `basics` | Type parameters, generic classes, pairs, and generic methods. |
| 2 | `bounds` | `T extends ...`, numeric bounds, and why bounds unlock methods. |
| 3 | `wildcards` | `?`, `? extends`, `? super`, invariance, and PECS. |
| 4 | `erasure` | Runtime behavior, erased signatures, and static generic traps. |

## Quick recall

- **Why generics?** Compile-time type safety without casts.
- **Default raw type rule?** Avoid raw `List`; use `List<T>`.
- **When to learn this?** After collections basics, before streams and fluent APIs.
