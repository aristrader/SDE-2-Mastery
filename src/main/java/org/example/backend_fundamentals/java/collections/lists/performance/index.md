---
order: 40
---

# List Performance

Use `ArrayList` by default. `LinkedList` is rarely faster in real Java code because pointer chasing and poor cache locality hurt iteration and random access.

## Complexity

| Operation | `ArrayList` | `LinkedList` |
| --- | --- | --- |
| `get(index)` | O(1) | O(n) |
| append | amortized O(1) | O(1) |
| insert/remove at beginning | O(n) | O(1) after node reached |
| contains | O(n) | O(n) |

The theoretical win for linked-list insert/remove only matters if you already have the node. Java's `LinkedList` API usually starts from an index or value, so it still has to traverse.

## CopyOnWriteArrayList

`CopyOnWriteArrayList` is for read-heavy shared lists such as listener lists or config snapshots.

- Reads are lock-free.
- Iterators see a snapshot.
- Every write copies the backing array.

Do not use it for write-heavy lists.

## Demo code

| Demo | Shows |
| --- | --- |
| `../playground/performance/ArrayListVsLinkedListRandomAccessRun` | Random access cost. |
| `../playground/concurrent/CopyOnWriteSnapshotIteratorRun` | Snapshot iterator behavior. |
| `../playground/concurrent/CopyOnWriteThreadSafetyRun` | Read-heavy concurrent behavior. |

## Quick recall

- **Default list?** `ArrayList`.
- **Frequent queue/deque operations?** Prefer `ArrayDeque`, not `LinkedList`.
- **Read-heavy shared listener list?** `CopyOnWriteArrayList`.
- **Why can `LinkedList` be slower despite O(1) node insert?** You usually pay O(n) traversal plus poor locality.
