---
order: 20
search: false
---

# PriorityQueue Patterns Solutions

## Solution: kth-largest-element - Kth Largest Element

```java
PriorityQueue<Integer> heap = new PriorityQueue<>();
for (int n : nums) {
    heap.offer(n);
    if (heap.size() > k) heap.poll();
}
return heap.peek();
```

## Solution: k-smallest-elements - K Smallest Elements

```java
PriorityQueue<Integer> heap = new PriorityQueue<>(Comparator.reverseOrder());
for (int n : nums) {
    heap.offer(n);
    if (heap.size() > k) heap.poll();
}
```

The heap keeps only the k smallest values seen so far.

## Solution: top-k-frequent-numbers - Top K Frequent Numbers

```java
Map<Integer, Integer> freq = new HashMap<>();
for (int n : nums) freq.put(n, freq.getOrDefault(n, 0) + 1);

PriorityQueue<Integer> heap = new PriorityQueue<>(Comparator.comparingInt(freq::get));
for (int n : freq.keySet()) {
    heap.offer(n);
    if (heap.size() > k) heap.poll();
}
```

## Solution: merge-sorted-arrays - Merge Sorted Arrays

```java
record Cursor(int value, int array, int index) {}

PriorityQueue<Cursor> heap = new PriorityQueue<>(Comparator.comparingInt(Cursor::value));
```

Push the first value from each array. Each poll emits one value and pushes the next value from the same array.

## Solution: two-heaps-median - Two Heaps

```java
PriorityQueue<Integer> low = new PriorityQueue<>(Comparator.reverseOrder());
PriorityQueue<Integer> high = new PriorityQueue<>();
```

Keep sizes balanced so `low` has either the same size as `high` or one extra. Median is `low.peek()` for odd count, otherwise average both roots.
