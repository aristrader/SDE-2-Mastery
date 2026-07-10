---
order: 40
---

# Maps

Maps store key-value pairs. Use this folder as the map study route: start with basic operations, then patterns, immutability, implementation internals, and finally mixed interview problems.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `basics` | `put`, `get`, replacement, missing keys |
| 2 | `frequency_patterns` | counters, grouping, lookup-table problems |
| 3 | `immutable_maps` | `Map.of`, shallow immutability, disabled mutators |
| 4 | `hashmap` | bucket array, load factor, resize, treeification, mutable keys |
| 5 | `linked_hashmap` | insertion order, access order, LRU cache pattern |
| 6 | `treemap` | sorted keys, range queries, navigable operations |
| 7 | `concurrent_hashmap` | atomic updates, no-null rule, concurrent access |
| 8 | `implementation_choice` | combined map implementation choices |

## Pick the map

| Need | Default |
| --- | --- |
| General key-value lookup | `HashMap` |
| Preserve insertion order | `LinkedHashMap` |
| Build a simple LRU cache | `LinkedHashMap` with `accessOrder=true` |
| Sorted keys or nearest-key lookup | `TreeMap` |
| Enum keys | `EnumMap` |
| Shared mutable map across threads | `ConcurrentHashMap` |
| Small read-only literal map | `Map.of(...)` / `Map.ofEntries(...)` |

## Rules to memorize

- Map keys are unique; putting the same key replaces the old value.
- `HashMap` allows one null key; `ConcurrentHashMap` allows no null keys or values.
- `HashMap` order is not insertion order; use `LinkedHashMap` when order is part of the requirement.
- `TreeMap` uses comparison, not hashing.
- Mutable hash keys can become unreachable after insertion.

## Quick recall

- **Default map?** `HashMap`.
- **Insertion-order map?** `LinkedHashMap`.
- **Sorted-key map?** `TreeMap`.
- **Concurrent counts/lookups?** `ConcurrentHashMap`.
- **Enum-keyed map?** `EnumMap`.
