---
title: Interview Recap
order: 90
---

# Generics Interview Recap

Use this only after the normal generics pages. It is a short interview pass, not the full learning path.

## Quick recall

**Q. What is the main generic-type trap?**
A. `List<Integer>` is not a subtype of `List<Number>`; parameterized types are invariant.

**Q. What is the practical wildcard rule?**
A. PECS: use `extends` to read a producer and `super` to write a consumer.

**Q. What runtime boundary matters?**
A. Erasure removes most type arguments, so some generic checks are compile-time only.
