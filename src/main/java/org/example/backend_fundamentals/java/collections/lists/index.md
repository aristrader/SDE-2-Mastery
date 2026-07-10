---
order: 10
---

# Lists

Lists are ordered collections that allow duplicates. Use this folder as the list study route: start with basic operations, then immutability traps, iteration, and performance.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `basics` | `ArrayList`, `LinkedList`, common operations, `remove` overloads, `subList` |
| 2 | `immutability` | `List.of`, `Arrays.asList`, `Collections.unmodifiableList` |
| 3 | `iteration` | loops, `Iterator`, safe removal |
| 4 | `performance` | `ArrayList` vs `LinkedList`, `CopyOnWriteArrayList` |

## Pick the boring default

| Requirement | Pick |
| --- | --- |
| Growable ordered collection | `ArrayList` |
| Fixed read-only values | `List.of(...)` |
| Fixed-size array view | `Arrays.asList(...)` |
| Safe removal while traversing | `Iterator` |
| Frequent queue/deque work | `ArrayDeque`, not `LinkedList` |
| Read-heavy shared listener list | `CopyOnWriteArrayList` |

## Quick recall

- **Default list?** `ArrayList`.
- **Allows duplicates?** Yes.
- **Preserves insertion order?** Yes.
- **Use `LinkedList` as default?** No.
- **Basic list drills live where?** `basics`.
