---
order: 30
---

# Sorted Collections

`TreeSet`, `TreeMap`, `PriorityQueue`, and sort operations need ordering. The element or key must implement `Comparable`, or the collection must receive a `Comparator`.

## TreeSet and TreeMap trap

Sorted collections use comparison for duplicate detection. If `compareTo()` or `compare()` returns `0`, `TreeSet` treats the objects as duplicates even when `equals()` would say they are different.

```java
// compareTo uses only id
new Student(1, "Alice").compareTo(new Student(1, "Bob")) == 0

// TreeSet keeps only one of them
```

For predictable behavior, natural ordering should usually be consistent with `equals`: if `equals()` is true, `compareTo()` should return `0`.

## Which collections require ordering?

| Collection/API | Ordering source |
| --- | --- |
| `TreeSet` | element `Comparable` or constructor `Comparator` |
| `TreeMap` | key `Comparable` or constructor `Comparator` |
| `PriorityQueue` | natural order or constructor `Comparator` |
| `Collections.sort` / `List.sort` | natural order or provided comparator |

## Quick recall

- **Can `TreeSet` drop an object?** Yes, when comparison returns `0`.
- **Does `HashSet` care about `Comparable`?** No.
- **Does `PriorityQueue` preserve insertion order?** No.
