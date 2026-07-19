---
title: Quick Revision
order: 10
search: false
---

# Collections Quick Revision

| Topic | One-line recall |
| --- | --- |
| `ArrayList` | Dynamic array; fast index access, costly middle inserts/removes. |
| `LinkedList` | Node chain; rarely better for backend list usage. |
| `HashMap` | Bucket array; hash chooses bucket, equals confirms key. |
| `LinkedHashMap` | `HashMap` plus linked order; useful for predictable iteration and LRU. |
| `TreeMap` | Sorted map using comparison, not hash lookup. |
| `ConcurrentHashMap` | Concurrent map with safe shared access and no null keys/values. |
| `HashSet` | Backed by `HashMap` keys. |
| `PriorityQueue` | Heap-backed priority ordering, not globally sorted iteration. |
| `Comparator` | External ordering; can compose with `thenComparing`. |

## Quick recall

- **Bucket selection?** `hashCode()`.
- **Exact key match?** `equals()`.
- **Sorted keys?** `TreeMap`.
