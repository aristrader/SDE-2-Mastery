---
order: 20
search: false
---

# PriorityQueue Basics Solutions

## Solution: basic-min-heap - Basic Min-Heap

```java
PriorityQueue<Integer> pq = new PriorityQueue<>();
List.of(10, 4, 7, 1, 8, 3).forEach(pq::offer);
System.out.println(pq.peek()); // 1
while (!pq.isEmpty()) System.out.println(pq.poll());
```

## Solution: empty-priority-queue - Empty PriorityQueue

```java
PriorityQueue<Integer> pq = new PriorityQueue<>();
System.out.println(pq.peek()); // null
System.out.println(pq.poll()); // null
pq.element();                  // NoSuchElementException
pq.remove();                   // NoSuchElementException
```

## Solution: max-heap - Max-Heap

```java
PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.reverseOrder());
List.of(10, 4, 7, 1, 8, 3).forEach(pq::offer);
while (!pq.isEmpty()) System.out.println(pq.poll());
```

## Solution: string-natural-order - Strings

```java
PriorityQueue<String> pq = new PriorityQueue<>();
List.of("banana", "apple", "orange", "grape").forEach(pq::offer);
while (!pq.isEmpty()) System.out.println(pq.poll());
```

## Solution: duplicate-elements - Duplicate Elements

```java
PriorityQueue<Integer> pq = new PriorityQueue<>();
List.of(5, 5, 5, 2, 2, 8).forEach(pq::offer);
while (!pq.isEmpty()) System.out.println(pq.poll());
```

`PriorityQueue` is a queue, not a set, so duplicates are allowed.

## Solution: iteration-trap - Iteration Trap

```java
for (int n : pq) System.out.println(n);      // heap array order
while (!pq.isEmpty()) System.out.println(pq.poll()); // priority order
```

Iteration is not sorted. Only repeated `poll()` gives priority order.

## Solution: build-from-collection - Build from a Collection

```java
PriorityQueue<Integer> pq = new PriorityQueue<>(List.of(9, 4, 7, 1, 3));
while (!pq.isEmpty()) System.out.println(pq.poll());
```

The collection constructor heapifies in O(n); repeated `offer` is O(n log n).
