---
order: 30
---

# hashCode Contract

`hashCode()` returns an `int` used by hash-based collections to choose a search bucket.

## Contract

If two objects are equal by `equals`, they must have the same hash code.

```java
if (a.equals(b)) {
    assert a.hashCode() == b.hashCode();
}
```

The reverse is not required. Two unequal objects may have the same hash code; that is a collision.

## Keep fields aligned

Use the same stable identity fields in both methods:

```java
@Override
public int hashCode() {
    return Objects.hash(id, name);
}
```

If `equals` uses `id` and `name`, `hashCode` must also use `id` and `name`.

## Quick recall

- **Equal objects need equal hash codes?** Yes.
- **Equal hash codes prove objects are equal?** No.
- **Best production default?** Let the IDE, record, or Lombok generate both methods together.
