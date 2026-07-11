---
order: 30
---

# PriorityQueue

Use `PriorityQueue` when the next element is chosen by priority, not insertion order. Java's default is a min-heap: the smallest element is removed first.

## Study path

| Order | Page | Use it for |
| --- | --- | --- |
| 1 | `basics` | Natural order, min/max heap, empty queue APIs, duplicates, iteration. |
| 2 | `comparators` | Custom priority and tie-breakers. |
| 3 | `patterns` | Kth element, top-k frequency, merging sorted arrays, median stream. |
| 4 | `traps` | Mutating queued objects and arbitrary removal cost. |

```java
PriorityQueue<Integer> pq = new PriorityQueue<>();
pq.offer(10);
pq.offer(3);
pq.offer(7);

System.out.println(pq.peek()); // 3
System.out.println(pq.poll()); // 3
```

## Heap behavior

Java's `PriorityQueue` is backed by a binary heap. The heap guarantees the root is the highest-priority element; it does not keep the whole internal array sorted.

```text
[1, 3, 2, 10, 8, 7]
```

That can be a valid min-heap even though the array is not sorted.

These are priority-order operations:

```java
pq.peek();
pq.poll();
```

These are not sorted views:

```java
System.out.println(pq);

for (Integer value : pq) {
    System.out.println(value);
}
```

To process in priority order, repeatedly poll:

```java
while (!pq.isEmpty()) {
    System.out.println(pq.poll());
}
```

## PriorityQueue vs sorted collections

`PriorityQueue` and `TreeSet` solve different problems:

| Need | Use | Why |
| --- | --- | --- |
| Repeatedly get the next highest-priority item | `PriorityQueue` | Heap keeps only the root ready. |
| Keep every element in sorted order | `TreeSet` | Red-black tree maintains full sorted order. |
| Allow duplicate priorities/values | `PriorityQueue` | Queue semantics allow duplicates. |
| Enforce uniqueness by comparison | `TreeSet` | Set semantics reject comparison-equal values. |

The key interview line: a heap is **partially ordered**, while a sorted set/tree is **fully ordered**.

## Core operations

| Operation | Complexity | Notes |
| --- | ---: | --- |
| `offer()` | O(log n) | Insert and sift up. |
| `poll()` | O(log n) | Remove root and sift down. |
| `peek()` | O(1) | Inspect root. |
| `size()` | O(1) | Current element count. |
| `contains()` | O(n) | Heap is only partially ordered. |
| `remove(Object)` | O(n) | Must find arbitrary element first. |
| Constructor from collection | O(n) | Bottom-up heapify. |

Use `offer()`, `poll()`, and `peek()` for normal code. `peek()` and `poll()` return `null` when empty; `element()` and `remove()` throw.

## Min-heap and max-heap

Default min-heap:

```java
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
```

Max-heap:

```java
PriorityQueue<Integer> maxHeap =
        new PriorityQueue<>(Comparator.reverseOrder());
```

## Custom priority

For custom objects, provide either `Comparable` or a `Comparator`.

```java
class Job {
    private final int id;
    private final int priority;

    Job(int id, int priority) {
        this.id = id;
        this.priority = priority;
    }

    int getId() {
        return id;
    }

    int getPriority() {
        return priority;
    }
}
```

Highest priority first, then smaller ID first:

```java
Comparator<Job> byPriority =
        Comparator.comparingInt(Job::getPriority)
                  .reversed()
                  .thenComparingInt(Job::getId);

PriorityQueue<Job> jobs = new PriorityQueue<>(byPriority);
```

Tie-breakers make output deterministic. Be careful where `reversed()` is placed:

```java
Comparator.comparingInt(Job::getPriority)
          .thenComparingInt(Job::getId)
          .reversed();
```

This reverses the whole chain, so ID also becomes descending. If only priority should be descending, reverse before the tie-breaker.

## Common traps

- Iteration order is heap-array order, not priority order.
- Duplicates are allowed.
- `null` is not allowed.
- Mutating a priority field after insertion does not reposition the element.
- Do not use subtraction in comparators because it can overflow; use `Integer.compare`, `Long.compare`, or comparator helpers.

If priority changes, remove the object, update it, and insert it again, or use immutable queue entries.

## Common uses

- Kth largest or smallest element
- Top K frequent elements
- Merge K sorted arrays or lists
- Task scheduling
- Meeting-room allocation
- Dijkstra's shortest path
- Event processing
- Median from a stream with two heaps

## Quick recall

- **Default Java priority queue?** Min-heap.
- **Is iteration sorted?** No, poll repeatedly for priority order.
- **`offer()` / `poll()` cost?** O(log n).
- **`peek()` cost?** O(1).
- **Arbitrary `remove(Object)` cost?** O(n).
- **Duplicates?** Allowed.
- **Mutable priority fields?** Dangerous after insertion.
