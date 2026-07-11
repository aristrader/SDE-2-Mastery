---
order: 40
---

# PriorityQueue Traps

PriorityQueue does not automatically reheapify when an element's internal priority changes, and arbitrary removal is slower than removing the root.

## Quick recall

- **Mutate priority inside queue?** Remove, change, reinsert.
- **`remove(value)` complexity?** O(n).
- **`poll()` complexity?** O(log n).
