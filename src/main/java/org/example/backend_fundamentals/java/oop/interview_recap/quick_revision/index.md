---
title: Quick Revision
order: 10
search: false
---

# OOP Quick Revision

| Topic | One-line recall |
| --- | --- |
| Object identity | `==` checks same reference. |
| Logical equality | `equals()` checks domain equality. |
| Hash contract | Equal objects must have equal hash codes. |
| Encapsulation | Keep invariants inside the type. |
| Inheritance | Reuse/variation through an is-a relationship. |
| Composition | Build behavior by holding collaborators. |
| Polymorphism | Runtime method dispatch based on actual object type. |
| Abstract class | Shared state/behavior plus partial implementation. |
| Interface | Contract/capability; supports multiple implementation types. |
| `final` | Prevent reassignment, override, or inheritance depending on target. |

## Quick recall

- **Hash collection requirement?** Override `equals()` and `hashCode()` together.
- **Safer default?** Composition before inheritance.
- **Dispatch target?** Actual runtime type for overridden instance methods.
