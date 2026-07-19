---
title: Quick Revision
order: 10
search: false
---

# Generics Quick Revision

| Topic | One-line recall |
| --- | --- |
| Invariance | `List<Integer>` is not a `List<Number>`. |
| `? extends T` | Read values as `T`; do not add specific `T` values. |
| `? super T` | Add `T` values; reads are only guaranteed as `Object`. |
| Named `<T>` | Use when arguments and return value must share the same exact type. |
| Erasure | Generic checks happen mostly at compile time; runtime sees raw-ish types. |
| Raw type | Skips generic checks and can create heap pollution. |
| Bounded type | `<T extends Comparable<T>>` restricts the accepted type and exposes methods. |

## Quick recall

- **Read producer list?** `List<? extends T>`.
- **Write consumer list?** `List<? super T>`.
- **Why no `new T()`?** Erasure removes the concrete runtime type.
