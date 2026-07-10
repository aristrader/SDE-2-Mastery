---
order: 60
---

# Coding Fluency

Coding fluency is the layer after core syntax, OOP, generics, and collections. These topics make Java code shorter and safer, but they are easier to misuse if the earlier mechanics are weak.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `method_references` | Replacing simple lambdas with method references. |
| 2 | `optional` | Making absence explicit without turning every value into `Optional`. |
| 3 | `streams` | Collector-heavy transformations after collection basics. |
| 4 | `records` | Immutable data carriers with generated equality. |
| 5 | `immutable_objects` | Object design that is safe for maps, sets, and threads. |
| 6 | `immutable_collections` | Read-only collection factories and defensive copies. |
| 7 | `lombok` | Boilerplate reduction with clear trade-offs. |

## Quick recall

- **Need to pass `Student::name` instead of `s -> s.name()`?** Method references.
- **Need a DTO with generated constructor/accessors/equality?** Records.
- **Need a read-only list/map/set?** Immutable collections, but remember shallow immutability.
- **Need null absence in return values?** `Optional`, not fields or parameters by default.
