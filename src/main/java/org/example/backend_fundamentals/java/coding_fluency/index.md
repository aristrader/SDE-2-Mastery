---
order: 60
---

# Coding Fluency

Coding fluency is the layer after core syntax, OOP, generics, and collections. These topics make Java code shorter and safer, but they are easier to misuse if the earlier mechanics are weak.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `lambdas` | Anonymous-class boilerplate, lambda syntax, target typing. |
| 2 | `functional_interfaces` | Function, Predicate, Consumer, Supplier, and operator shapes. |
| 3 | `method_references` | Replacing simple lambdas with method references. |
| 4 | `optional` | Making absence explicit without turning every value into `Optional`. |
| 5 | `streams` | Stream lifecycle first, then collector-heavy transformations. |
| 6 | `records` | Immutable data carriers with generated equality. |
| 7 | `immutable_objects` | Object design that is safe for maps, sets, and threads. |
| 8 | `immutable_collections` | Read-only collection factories and defensive copies. |
| 9 | `lombok` | Boilerplate reduction with clear trade-offs. |

## Quick recall

- **Need to replace anonymous class boilerplate?** Lambdas.
- **Need to identify `T -> boolean`?** `Predicate<T>`.
- **Need to pass `Student::name` instead of `s -> s.name()`?** Method references.
- **Need a DTO with generated constructor/accessors/equality?** Records.
- **Need a read-only list/map/set?** Immutable collections, but remember shallow immutability.
- **Need null absence in return values?** `Optional`, not fields or parameters by default.
