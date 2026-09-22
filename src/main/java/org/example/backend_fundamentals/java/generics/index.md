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

**Q. Why use generics?**
A. They express the element/type relationship at compile time, preventing unsafe casts from leaking into callers.

**Q. What is the raw-type rule?**
A. Avoid raw `List`; use a parameterized type and isolate any necessary legacy boundary.

**Q. What is the learning order?**
A. Collections first, then generics before streams and fluent APIs.
