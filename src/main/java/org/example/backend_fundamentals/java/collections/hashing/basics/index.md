---
order: 10
---

# Hashing Basics

A hash function takes an input and returns a fixed-size number.

Useful hash functions are:

- deterministic: same input, same hash;
- fast: cheap to compute;
- well-distributed: values spread across the range.

Hashes are not unique IDs. Different inputs can produce the same hash because the output range is finite.

## Java object hashes

Every Java object has:

```java
int hash = object.hashCode();
```

By default, `Object.hashCode()` is identity-based. Value classes should usually override it with `equals`.

## Collisions

Two different keys can collide in two ways:

- true collision: same `hashCode`;
- bucket collision: different hashes map to the same bucket index.

```java
"Aa".hashCode() == "BB".hashCode(); // true
```

Collisions are normal. A broken `equals`/`hashCode` contract is not normal.

## Quick recall

- **Are hashes unique?** No.
- **Is a collision automatically a bug?** No.
- **Does Java give every object a hash?** Yes, via `hashCode`.
- **Default object hash semantics?** Identity-based.
