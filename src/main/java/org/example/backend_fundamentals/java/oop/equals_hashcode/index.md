---
order: 140
---

# Object Equality, Hash Codes, and Identity

This module answers one interview-critical question: when are two Java objects considered the same?

## Study path

1. `object_identity` — `Object`, reference identity, `==`, and default methods.
2. `equals_contract` — when and how to override `equals`.
3. `hashcode_contract` — why equal objects must produce equal hash codes.
4. `hash_collections_traps` — `HashMap`/`HashSet` lookup failures and mutable keys.
5. `identity_hashcode` — identity hash codes and rare identity-based debugging.
6. Archived imported notes live under `todo/study_plan/reference/archive/java_oop/equals_hashcode_reference_notes.md`.

Practice lives beside the focused child pages, so do object identity first, then `equals`, then `hashCode`, then hash-collection traps.

## The core rule

Default Java objects use identity semantics:

```java
Object a = new Object();
Object b = new Object();

System.out.println(a == b);      // false
System.out.println(a.equals(b)); // false
```

For value-like objects, override both `equals` and `hashCode` based on the same stable identity fields.

```java
record Student(String id, String name) {}
```

Records do this automatically using their components.

## Collections connection

Hash-based collections use two steps:

```text
hashCode() -> choose bucket
equals()   -> find exact match inside bucket
```

If `equals` says two objects are equal but their hash codes differ, `HashMap.get()` and `HashSet.contains()` can silently fail.

## Quick recall

- **Default `equals()` checks?** Reference identity.
- **Default `hashCode()` represents?** Identity-based hash.
- **When override `equals()`?** When business/value equality differs from reference identity.
- **If you override `equals()`?** Override `hashCode()` too.
- **Can unequal objects have the same hash code?** Yes, collisions are allowed.
- **Why are mutable keys dangerous?** Mutation can change the lookup bucket after insertion.
