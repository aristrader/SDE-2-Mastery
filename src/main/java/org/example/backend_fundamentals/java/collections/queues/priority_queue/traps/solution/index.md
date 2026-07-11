---
order: 20
search: false
---

# PriorityQueue Traps Solutions

## Solution: mutation-trap - Mutation Trap

```java
pq.remove(task);
task.setPriority(newPriority);
pq.offer(task);
```

Changing a field used by the comparator does not reheapify the internal array.

## Solution: arbitrary-removal - Arbitrary Removal

```java
pq.remove(value); // O(n)
```

The queue must scan to find the value. `poll()` is O(log n) because it always removes the root.
