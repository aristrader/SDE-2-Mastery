---
order: 20
search: false
---

# Solutions

## Solution: map-practice-path - Choose the Right Map Practice

Use the child modules as the solution path:

- `basics` for core operations and duplicate keys
- `frequency_patterns` for counters and grouping indexes
- `immutable_maps` for `Map.of` traps
- `hashmap` for bucket internals and mutable-key behavior
- `linked_hashmap` for insertion order, access order, and LRU cache behavior
- `treemap` for sorted keys and range/navigation queries
- `concurrent_hashmap` for atomic updates and no-null concurrency rules
- `implementation_choice` for combined implementation choices

The short rule: default to `HashMap`, use `LinkedHashMap` for predictable insertion order, use `TreeMap` for sorted keys/navigation, use `ConcurrentHashMap` for shared mutable state, and use `Map.of` only for shallowly immutable read-only maps.
