---
order: 30
---

# Immutable Sets

`Set.of` creates a shallowly immutable set. Every mutator throws `UnsupportedOperationException`, and construction rejects nulls and duplicates.

```java
Set<String> roles = Set.of("ADMIN", "USER");
// roles.add("AUDITOR"); // UnsupportedOperationException
```

## Duplicate fail-fast

```java
new HashSet<>(List.of("A", "A", "B")); // size 2, silent dedupe
Set.of("A", "A", "B");                 // IllegalArgumentException
```

For literal immutable sets, duplicates are usually a bug, so fail-fast is better.

## Null fail-fast

```java
Set.of("A", null); // NullPointerException
```

## Demo code

| Demo | Shows |
| --- | --- |
| `../playground/immutability/SetOfImmutabilityRun` | Mutation, duplicate, and null failures. |

## Quick recall

- **Can `Set.of` contain null?** No.
- **Can `Set.of` contain duplicates?** No, it throws.
- **Is it deeply immutable?** No. Mutable elements can still mutate internally.
