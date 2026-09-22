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

**Q. When does a lambda fit?**
A. When the target is one functional-interface behavior, not when a named type or stateful object makes the contract clearer.

**Q. What shape is `T -> boolean`?**
A. `Predicate<T>`.

**Q. When is `Student::name` clearer than `s -> s.name()`?**
A. When it is the same direct call with no extra mapping, filtering, or captured context.

**Q. When does a record fit?**
A. A transparent data carrier with generated constructor, accessors, equality, and hash code; it is only shallowly immutable.

**Q. Where should `Optional` normally appear?**
A. At a return-value absence boundary, not routinely in fields or parameters.
