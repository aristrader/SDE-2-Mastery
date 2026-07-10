---
order: 20
---

# equals Contract

Override `equals` when two different object instances should count as the same logical value.

## The five rules

| Rule | Meaning |
| --- | --- |
| Reflexive | `x.equals(x)` is true |
| Symmetric | `x.equals(y)` and `y.equals(x)` agree |
| Transitive | if `x == y` logically and `y == z` logically, then `x == z` logically |
| Consistent | result stays stable while compared state is unchanged |
| Null-safe | `x.equals(null)` is false |

## Standard shape

```java
final class Employee {
    private final String id;
    private final String name;

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Employee employee)) return false;
        return Objects.equals(id, employee.id)
            && Objects.equals(name, employee.name);
    }
}
```

Prefer `final` value classes, records, or composition. Inheritance can make equality rules hard to keep symmetric and transitive when subclasses add fields.

## Quick recall

- **First line in most `equals` methods?** `if (this == other) return true;`
- **Should `equals(null)` throw?** No, return false.
- **Should `equals` compare mutable fields?** Usually no if the object is used in collections.
