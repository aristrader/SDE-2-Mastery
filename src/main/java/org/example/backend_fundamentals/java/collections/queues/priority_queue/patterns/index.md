---
order: 30
---

# PriorityQueue Patterns

Priority queues are useful for "best next" problems: kth element, top-k frequency, merging sorted streams, and median from a stream.

## Quick recall

- **Kth largest?** Min-heap of size k.
- **K smallest?** Max-heap of size k.
- **Merge sorted arrays?** Heap stores one cursor per array.
- **Running median?** Two heaps.
