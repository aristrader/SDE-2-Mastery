---
order: 80
---

# Map Implementation Choice

Mixed map problems test implementation choice, not just syntax.

The usual interview trap is choosing the first map that works for lookup while ignoring another required behavior: ordering, sorted output, concurrency, immutability, or key safety.

## Decision checklist

Read the requirement and ask:

| Requirement phrase | Map implication |
| --- | --- |
| "fast lookup by key" | `HashMap` |
| "print in insertion order" | `LinkedHashMap` |
| "print alphabetically / sorted by key" | `TreeMap` |
| "nearest key / range query" | `TreeMap` |
| "shared by many threads" | `ConcurrentHashMap` |
| "fixed lookup table" | `Map.of` / `Map.copyOf` |
| "enum key" | `EnumMap` |

## One map may not be enough

Sometimes one requirement wants lookup speed and another wants a different ordering.

Example: student marks system:

- lookup marks by student name
- print students alphabetically
- print topper

`TreeMap<String, Integer>` handles alphabetical output naturally, but finding topper still requires scanning values.

If topper lookup becomes frequent at scale, maintain a second index:

```java
Map<String, Integer> marksByName = new HashMap<>();
TreeMap<Integer, Set<String>> namesByMarks = new TreeMap<>();
```

That is the trade-off: more write complexity and memory in exchange for faster reads.

## Common mistakes

- Using `HashMap` when output order is part of the requirement.
- Using a mutable object as a key.
- Using `TreeMap` without a valid natural order or comparator.
- Using `Map.of` and then calling `put`, `merge`, or `compute`.
- Using `HashMap` for shared mutable state across threads.

## Quick recall

- **Need sorted keys?** `TreeMap`.
- **Need insertion order?** `LinkedHashMap`.
- **Need thread-safe updates?** `ConcurrentHashMap`.
- **Need fixed read-only values?** `Map.of` / `Map.copyOf`.
- **One map cannot satisfy all read paths efficiently?** Maintain a second index only when the read pattern justifies it.
