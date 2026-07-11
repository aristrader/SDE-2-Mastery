---
order: 30
---

# Java Collections

Collections are Java's standard object containers. Use them when size, lookup, ordering, uniqueness, or concurrency behavior matters more than a fixed-size array.

## Core interfaces

| Interface | Use when |
| --- | --- |
| `List<E>` | ordered sequence, duplicates allowed, index access matters |
| `Set<E>` | uniqueness matters |
| `Map<K, V>` | key-value lookup matters |
| `Queue<E>` / `Deque<E>` | processing order matters |

Program to the interface when callers do not need implementation-specific behavior:

```java
List<String> names = new ArrayList<>();
Map<String, Integer> counts = new HashMap<>();
```

## Main study path

- `lists` — basics, immutability, iteration, performance.
- `hashing` — basics, hash contract, `HashMap` mechanics.
- `sorting` — `Comparable`, `Comparator`, sorted collections.
- `maps` — basics, frequency/grouping patterns, immutability, implementations, implementation choice.
- `sets` — basics, ordering, immutability, hash contract, concurrency.
- `queues` — basics, deque, queue patterns.
- `cheatsheet` — quick revision table.

## Pick the boring default

| Need | Default |
| --- | --- |
| Growable sequence | `ArrayList` |
| Deduplicate values | `HashSet` |
| Preserve insertion order while deduping | `LinkedHashSet` |
| Key-value lookup | `HashMap` |
| Predictable map iteration order | `LinkedHashMap` |
| Sorted keys/elements | `TreeMap` / `TreeSet` |
| Shared concurrent counts/lookups | `ConcurrentHashMap` |
| FIFO processing | `ArrayDeque` |
| Priority-based processing | `PriorityQueue` |

## Quick recall

**Q. Why is `List` often the variable type but `ArrayList` the constructor?**
A. The caller needs the `List` contract; the implementation can remain replaceable.

**Q. Which collections depend on `equals`/`hashCode`?**
A. Hash-based collections: `HashMap`, `HashSet`, `LinkedHashMap`, `LinkedHashSet`.

**Q. Which collections depend on ordering?**
A. `TreeSet`, `TreeMap`, `PriorityQueue`, and explicit sort operations.

**Q. Where did array basics move?**
A. `java/foundations/arrays/index.md`.
