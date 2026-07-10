---
order: 30
---

# HashMap Mechanics

`HashMap` uses hashes to find a bucket quickly, then uses `equals` inside that bucket.

## Bucket model

```text
HashMap table, default capacity 16:
[0] [1] [2] [3] ... [15]
```

For `put(key, value)`:

1. compute a spread hash from `key.hashCode()`;
2. compute bucket index with `hash & (capacity - 1)`;
3. store or replace the entry in that bucket.

For `get(key)`, Java repeats the same bucket calculation, then walks that bucket and compares keys with `equals`.

## Collision handling

HashMap uses separate chaining:

```text
bucket X -> [Aa=1] -> [BB=2]
```

When a bucket chain grows large, Java 8+ can treeify it into a red-black tree. This protects worst-case lookup from degrading too far under heavy collisions.

## Resizing and iteration order

Default capacity is 16 and default load factor is 0.75. Around 12 entries, the table resizes to 32.

Iteration walks buckets, not insertion order. After resize, entries rehash into new buckets, so iteration order can change. Do not rely on `HashMap` iteration order.

## Where hashing appears

| Type | Uses hashing? |
| --- | --- |
| `HashMap` | Yes |
| `HashSet` | Yes, backed by `HashMap` |
| `LinkedHashMap` | Yes, plus linked iteration order |
| `LinkedHashSet` | Yes, backed by `LinkedHashMap` |
| `ConcurrentHashMap` | Yes, plus concurrency control |
| `TreeMap` / `TreeSet` | No, uses comparison |

## Quick recall

- **Bucket index formula idea?** `hash & (capacity - 1)`.
- **Collision strategy?** Bucket chain, treeified if large enough.
- **Why can iteration order change?** Resize changes bucket indexes.
- **Which map preserves insertion order?** `LinkedHashMap`.
