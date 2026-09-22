---
title: Interview Recap
order: 140
---

# Concurrency Interview Recap

Use this after the concurrency pages for quick interview recall.

## Quick recall

**Q. What guarantees must you keep separate?**
A. Visibility, atomicity, ordering, and mutual exclusion; one does not automatically provide the others.

**Q. What is the common `volatile` trap?**
A. It does not make `count++`, check-then-act, or a multi-field invariant atomic.

**Q. What is the main reasoning tool?**
A. Name the happens-before edge that makes one thread’s write safely observable by another.
