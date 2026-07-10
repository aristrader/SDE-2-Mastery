---
order: 20
search: false
---

# Solutions

## Solution: basic-min-heap - Basic Min-Heap

```java
PriorityQueue<Integer> pq = new PriorityQueue<>();
for (int n : List.of(10, 4, 7, 1, 8, 3)) {
    pq.offer(n);
}

System.out.println(pq);      // internal heap order, not sorted
System.out.println(pq.peek()); // 1

while (!pq.isEmpty()) {
    System.out.println(pq.poll());
}
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
for (int n : List.of(10, 4, 7, 1, 8, 3)) {
    pq.offer(n);
}
while (!pq.isEmpty()) {
    System.out.println(pq.poll());
}
```

## Solution: string-natural-order - Strings

```java
PriorityQueue<String> pq = new PriorityQueue<>();
for (String s : List.of("banana", "apple", "orange", "grape")) {
    pq.offer(s);
}
while (!pq.isEmpty()) {
    System.out.println(pq.poll());
}
```

## Solution: string-length-priority - String Length

```java
PriorityQueue<String> pq = new PriorityQueue<>(
        Comparator.comparingInt(String::length)
                  .thenComparing(Comparator.naturalOrder())
);
```

## Solution: student-by-cgpa - Student by CGPA

```java
record Student(int id, String name, double cgpa) {}

PriorityQueue<Student> pq = new PriorityQueue<>(
        Comparator.comparingDouble(Student::cgpa).reversed()
);
```

Poll until empty to get highest CGPA first.

## Solution: comparator-tie-breaking - Comparator Tie-Breaking

```java
PriorityQueue<Student> pq = new PriorityQueue<>(
        Comparator.comparingDouble(Student::cgpa).reversed()
                  .thenComparing(Student::name)
                  .thenComparingInt(Student::id)
);
```

Put `reversed()` before the tie-breakers so only CGPA is descending.

## Solution: duplicate-elements - Duplicate Elements

`PriorityQueue` allows duplicates because it is a queue, not a set. `TreeSet` removes values whose comparison returns `0` because set membership is unique.

## Solution: iteration-trap - Iteration Trap

Iteration walks the internal heap array. Repeated `poll()` repeatedly removes the root and restores heap order, so it produces priority order.

## Solution: kth-largest-element - Kth Largest Element

```java
int[] nums = {3, 2, 1, 5, 6, 4};
int k = 2;
PriorityQueue<Integer> heap = new PriorityQueue<>();

for (int n : nums) {
    heap.offer(n);
    if (heap.size() > k) {
        heap.poll();
    }
}

System.out.println(heap.peek()); // 5
```

## Solution: k-smallest-elements - K Smallest Elements

```java
int[] nums = {9, 4, 7, 1, 3, 6, 2};
int k = 3;
PriorityQueue<Integer> heap = new PriorityQueue<>(Comparator.reverseOrder());

for (int n : nums) {
    heap.offer(n);
    if (heap.size() > k) {
        heap.poll();
    }
}

List<Integer> result = new ArrayList<>(heap);
Collections.sort(result);
```

## Solution: top-k-frequent-numbers - Top K Frequent Numbers

```java
int[] nums = {1, 1, 1, 2, 2, 3};
int k = 2;
Map<Integer, Integer> freq = new HashMap<>();
for (int n : nums) {
    freq.put(n, freq.getOrDefault(n, 0) + 1);
}

PriorityQueue<Integer> heap = new PriorityQueue<>(
        Comparator.comparingInt(freq::get)
);
for (int n : freq.keySet()) {
    heap.offer(n);
    if (heap.size() > k) {
        heap.poll();
    }
}
```

The heap contains the top `k` values by frequency.

## Solution: merge-sorted-arrays - Merge Sorted Arrays

```java
record Entry(int value, int arrayIndex, int elementIndex) {}

int[][] arrays = {{1, 4, 7}, {2, 5, 8}, {3, 6, 9}};
PriorityQueue<Entry> pq = new PriorityQueue<>(Comparator.comparingInt(Entry::value));
List<Integer> merged = new ArrayList<>();

for (int i = 0; i < arrays.length; i++) {
    pq.offer(new Entry(arrays[i][0], i, 0));
}

while (!pq.isEmpty()) {
    Entry current = pq.poll();
    merged.add(current.value());

    int nextIndex = current.elementIndex() + 1;
    if (nextIndex < arrays[current.arrayIndex()].length) {
        pq.offer(new Entry(arrays[current.arrayIndex()][nextIndex], current.arrayIndex(), nextIndex));
    }
}
```

## Solution: task-scheduler-simulation - Task Scheduler Simulation

```java
record Task(int id, String name, int priority, long createdAt) {}

PriorityQueue<Task> tasks = new PriorityQueue<>(
        Comparator.comparingInt(Task::priority).reversed()
                  .thenComparingLong(Task::createdAt)
                  .thenComparingInt(Task::id)
);
```

Repeated `poll()` processes highest priority, then oldest task, then smallest ID.

## Solution: mutation-trap - Mutation Trap

Changing an object already inside the queue does not re-heapify it. Fix by removing it, changing it, then inserting it again, or by using immutable task entries.

## Solution: arbitrary-removal - Arbitrary Removal

`pq.remove(value)` is O(n) because the queue must first scan the heap array to find that value. `poll()` is O(log n) because it always removes the known root.

## Solution: build-from-collection - Build from a Collection

```java
List<Integer> values = List.of(8, 3, 5, 1, 9);
PriorityQueue<Integer> pq = new PriorityQueue<>(values);
```

The collection constructor can heapify in O(n). Repeated insertion costs O(n log n).

## Solution: two-heaps-median - Two Heaps

```java
PriorityQueue<Integer> low = new PriorityQueue<>(Comparator.reverseOrder());
PriorityQueue<Integer> high = new PriorityQueue<>();

for (int n : List.of(5, 15, 1, 3)) {
    if (low.isEmpty() || n <= low.peek()) {
        low.offer(n);
    } else {
        high.offer(n);
    }

    if (low.size() > high.size() + 1) {
        high.offer(low.poll());
    } else if (high.size() > low.size()) {
        low.offer(high.poll());
    }

    double median = low.size() == high.size()
            ? (low.peek() + high.peek()) / 2.0
            : low.peek();
    System.out.println(median);
}
```
