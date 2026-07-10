---
order: 30
---

# Sorting

Sorting custom objects requires an explicit ordering rule. Use this folder for `Comparable`, `Comparator`, and ordered collections.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `comparable` | one natural/default order on the class |
| 2 | `comparator` | external and multiple business orderings |
| 3 | `sorted_collections` | `TreeSet`, `TreeMap`, `PriorityQueue`, duplicate traps |

## Quick recall

- **One natural order?** `Comparable`.
- **Many valid orderings?** `Comparator`.
- **Never compare ints how?** By subtraction; use `Integer.compare`.
- **Which collections need ordering?** `TreeSet`, `TreeMap`, `PriorityQueue`, sort operations.
