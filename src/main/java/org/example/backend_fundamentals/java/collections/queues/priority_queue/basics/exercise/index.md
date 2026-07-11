---
order: 10
search: false
---

# PriorityQueue Basics Practice

## Exercise: basic-min-heap - Basic Min-Heap

Create `PriorityQueue<Integer>`, insert `10, 4, 7, 1, 8, 3`, print the queue, `peek()`, then all values using `poll()`.

## Exercise: empty-priority-queue - Empty PriorityQueue

Call `peek()`, `poll()`, `element()`, and `remove()` on an empty queue. Note which return `null` and which throw.

## Exercise: max-heap - Max-Heap

Create a max-heap using `Comparator.reverseOrder()`. Expected poll order: `10, 8, 7, 4, 3, 1`.

## Exercise: string-natural-order - Strings

Insert `banana`, `apple`, `orange`, `grape` and observe natural lexicographical order.

## Exercise: duplicate-elements - Duplicate Elements

Insert `5, 5, 5, 2, 2, 8`. Verify duplicates are retained and explain why this differs from `TreeSet`.

## Exercise: iteration-trap - Iteration Trap

Insert `20, 5, 15, 1, 10, 8`. Compare `for` iteration with repeated `poll()`.

## Exercise: build-from-collection - Build from a Collection

Construct `new PriorityQueue<>(list)` and compare its O(n) heapify cost with O(n log n) repeated insertion.
