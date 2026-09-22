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
| Named `<T>` | Express one shared compile-time type relationship across arguments/return values. |
| Erasure | Generic checks happen mostly at compile time; runtime sees raw-ish types. |
| Raw type | Skips generic checks and can create heap pollution. |
| Bounded type | `<T extends Comparable<T>>` restricts the accepted type and exposes methods. |

## Quick recall

**Q. Read a producer list?**
A. `List<? extends T>`.

**Q. Write a consumer list?**
A. `List<? super T>`.

**Q. Why no `new T()`?**
A. Erasure removes the concrete runtime type.
