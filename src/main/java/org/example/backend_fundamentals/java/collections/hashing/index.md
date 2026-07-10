---
order: 20
---

# Hashing

Hashing is the prerequisite for understanding `HashMap`, `HashSet`, `LinkedHashMap`, `LinkedHashSet`, and `ConcurrentHashMap`.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `basics` | hash functions, `Object.hashCode`, and collisions |
| 2 | `hash_contract` | `equals`/`hashCode` contract and broken value objects |
| 3 | `hashmap_mechanics` | buckets, resizing, iteration order, treeification |

## Quick recall

- **What picks the bucket?** `hashCode`.
- **What confirms the exact entry?** `equals`.
- **Can unequal objects share a hash?** Yes.
- **Can equal objects have different hashes?** No, that breaks hash collections.
- **Need implementation details?** Read `hashmap_mechanics`, then `../maps/hashmap/`.
