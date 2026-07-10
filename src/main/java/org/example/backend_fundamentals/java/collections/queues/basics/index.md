---
order: 10
---

# Queue Basics

Use `Queue` when only FIFO behavior matters.

```java
Queue<String> queue = new ArrayDeque<>();
queue.offer("A");
queue.offer("B");

System.out.println(queue.peek()); // A, not removed
System.out.println(queue.poll()); // A, removed
```

## Method pairs

Prefer the non-throwing family when empty is normal.

| Operation | Throws on failure | Returns special value |
| --- | --- | --- |
| Add | `add(e)` | `offer(e)` |
| Remove head | `remove()` | `poll()` returns `null` |
| Inspect head | `element()` | `peek()` returns `null` |

## Why ArrayDeque

`ArrayDeque` stores elements in a resizable circular array. Compared with `LinkedList`, it usually has better cache locality, fewer object allocations, lower memory overhead, and less GC pressure.

Both can give O(1) insertion/removal at the ends, but `ArrayDeque` is usually faster in practice. Use it for normal queues unless a requirement forces something else.

## PriorityQueue is not FIFO

```java
Queue<Integer> priorities = new PriorityQueue<>();
priorities.offer(30);
priorities.offer(10);
priorities.offer(20);

System.out.println(priorities.poll()); // 10
```

`PriorityQueue` removes by natural order or comparator, not insertion order.

## Quick recall

- **Normal FIFO implementation?** `ArrayDeque`.
- **Empty queue: `poll()` or `remove()`?** `poll()` returns `null`; `remove()` throws.
- **Priority ordering?** `PriorityQueue`.
