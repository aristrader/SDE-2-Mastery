---
order: 20
---

# Hash Contract

The `equals`/`hashCode` contract has three practical rules:

1. If `a.equals(b)` is true, `a.hashCode() == b.hashCode()` must also be true.
2. If two hash codes are equal, the objects do not have to be equal.
3. Fields used by `equals`/`hashCode` should not mutate while the object is inside a hash collection.

## Default Object behavior

- `Object.equals(o)` uses identity: `this == o`.
- `Object.hashCode()` is identity-based.

That is wrong for value types:

```java
new Money(100, "USD").equals(new Money(100, "USD")); // should be true for value semantics
```

## Good hashCode

Use the same fields as `equals`:

```java
@Override
public int hashCode() {
    return Objects.hash(amount, currency);
}
```

Manual style:

```java
@Override
public int hashCode() {
    int result = 17;
    result = 31 * result + Integer.hashCode(amount);
    result = 31 * result + currency.hashCode();
    return result;
}
```

## Common bugs

- overriding `equals` but not `hashCode`;
- using mutable fields in hash/equality;
- using mutable keys like `ArrayList`, `HashMap`, or `Date`;
- persisting hash-code values across runs or systems.

## Quick recall

- **Equal objects need equal hashes?** Yes.
- **Equal hashes imply equal objects?** No.
- **Most common bug?** Override `equals` without `hashCode`.
- **Best simple fix for value keys?** Use a record or generate both methods from the same fields.
