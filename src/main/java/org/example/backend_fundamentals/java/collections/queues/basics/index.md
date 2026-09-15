---
order: 10
---

# Queue Basics

Use a queue when the next item to process should be the oldest item waiting. A request worker taking the
next job, a breadth-first search visiting the next node, and a producer handing work to a consumer all
share that FIFO rule. Use the `Queue` interface when FIFO is the only behavior the code needs.

```java
Queue<String> queue = new ArrayDeque<>();
queue.offer("A");
queue.offer("B");

System.out.println(queue.peek()); // A, not removed
System.out.println(queue.poll()); // A, removed
```

The variable is typed as `Queue`, so callers do not accidentally rely on stack or indexed-list methods.
`ArrayDeque` supplies the normal in-memory implementation. Its JDK implementation uses a resizable
circular array rather than allocating one linked-node object per item; treat that as useful performance
context, not as a capacity-layout contract your application may depend on.

## Empty queue behavior is a deliberate API choice

When a worker asks for the next job, an empty queue is often normal. Use the non-throwing method family
when that is the expected outcome. Use the throwing family when an empty queue means the caller violated
its own contract.

| Operation | Throws on failure | Returns special value |
| --- | --- | --- |
| Add | `add(e)` | `offer(e)` |
| Remove head | `remove()` | `poll()` returns `null` |
| Inspect head | `element()` | `peek()` returns `null` |

`ArrayDeque` does not allow `null`, so a `null` result from `poll()` or `peek()` unambiguously means the
deque is empty. Do not assume that every `Queue` implementation shares that null rule; code using a
different implementation must follow that implementation's contract.

## Why `ArrayDeque` is the default

For ordinary single-threaded FIFO work, `ArrayDeque` has O(1) amortized insertion at the tail and O(1)
removal from the head. Compared with `LinkedList`, it usually needs fewer allocations and has better
locality, so it is the normal queue/deque default. `LinkedList` is rarely a better queue choice merely
because it also implements `Queue`.

`ArrayDeque` is not thread-safe. If producers and consumers share a queue across threads and a producer
must wait for capacity or a consumer must wait for work, use a `BlockingQueue` implementation such as
`ArrayBlockingQueue` or `LinkedBlockingQueue`; do not add `synchronized` around random `ArrayDeque`
calls and call it a complete producer-consumer design.

## `PriorityQueue` is a different rule

```java
Queue<Integer> priorities = new PriorityQueue<>();
priorities.offer(30);
priorities.offer(10);
priorities.offer(20);

System.out.println(priorities.poll()); // 10
```

`PriorityQueue` removes by natural order or a comparator, not insertion order. Its head is the smallest
(or comparator-best) item, but iterating it does **not** promise sorted order. If you need every item in
priority order, repeatedly call `poll()` or copy and sort elsewhere.

## Pick the structure from the processing rule

| Processing rule | Type to start with | Important boundary |
| --- | --- | --- |
| First accepted, first processed | `Queue` backed by `ArrayDeque` | Single-threaded or externally synchronized use. |
| Need both ends or a stack | `Deque` backed by `ArrayDeque` | `Deque` exposes the additional operations explicitly. |
| Highest/lowest priority next | `PriorityQueue` | Not FIFO and not sorted iteration. |
| Producers/consumers coordinate across threads | `BlockingQueue` | Capacity and wait policy become part of the contract. |

## Common mistakes

- Calling `remove()` in a loop where empty is normal, then treating `NoSuchElementException` as control
  flow. Use `poll()` instead.
- Using `PriorityQueue` for FIFO tasks because its name contains "queue." Its ordering rule is priority.
- Using `ArrayDeque` as a shared task queue without a concurrency primitive.
- Using `null` as an application payload in an `ArrayDeque`; it rejects null elements.

## Quick recall

- **Normal FIFO implementation?** `ArrayDeque`.
- **Empty queue: `poll()` or `remove()`?** `poll()` returns `null`; `remove()` throws.
- **Priority ordering?** `PriorityQueue`.
- **Can `ArrayDeque` coordinate producer and consumer threads?** No; use a concurrent queue or a
  `BlockingQueue` when waiting/capacity behavior matters.
- **Does iterating a `PriorityQueue` return sorted order?** No; only repeated `poll()` is ordered.
