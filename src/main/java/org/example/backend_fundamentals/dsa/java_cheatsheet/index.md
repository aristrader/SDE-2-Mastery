---
order: 40
---

# Java Collections Cheat Sheet

Use this for quick syntax recall during DSA practice.

## Imports

```java
import java.util.*;
```

## ArrayList

```java
List<Integer> list = new ArrayList<>();
list.add(10);
list.add(20);

int first = list.get(0);
int n = list.size();
boolean empty = list.isEmpty();

list.set(0, 99);
list.remove(list.size() - 1);      // remove by index
list.remove(Integer.valueOf(99));  // remove by value

Collections.sort(list);                         // ascending
list.sort(Collections.reverseOrder());          // descending
```

## Stack

Use `Deque` as a stack.

```java
Deque<Integer> stack = new ArrayDeque<>();
stack.push(10);
stack.push(20);

int x = stack.peek();  // 20
stack.pop();           // removes and returns 20

boolean empty = stack.isEmpty();
int n = stack.size();
```

## Queue

```java
Queue<Integer> q = new ArrayDeque<>();
q.offer(10);
q.offer(20);

int x = q.peek();  // 10
q.poll();          // removes and returns 10

boolean empty = q.isEmpty();
int n = q.size();
```

Prefer `offer`, `peek`, `poll` in DSA because they are safe on empty/full edge cases. `add`, `element`, `remove` may throw exceptions.

## PriorityQueue

By default, Java `PriorityQueue` is a min heap.

```java
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
minHeap.offer(10);
minHeap.offer(30);
minHeap.offer(20);

int smallest = minHeap.peek();  // 10
minHeap.poll();                 // removes and returns 10
```

Max heap syntax:

```java
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());
maxHeap.offer(10);
maxHeap.offer(30);
maxHeap.offer(20);

int largest = maxHeap.peek();  // 30
```

Priority queue of arrays:

```java
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
pq.offer(new int[] {5, 100});

int[] item = pq.peek();
```

Safer comparator when values can be large:

```java
PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[0], b[0]));
```

## HashSet

Average `O(1)` lookup. No sorting.

```java
Set<Integer> set = new HashSet<>();
set.add(10);
set.add(5);

boolean exists = set.contains(10);
set.remove(10);

int n = set.size();
```

## TreeSet

Stores unique values in sorted order.

```java
TreeSet<Integer> set = new TreeSet<>();
set.add(10);
set.add(5);

int smallest = set.first();
int largest = set.last();

Integer floor = set.floor(7);    // <= 7
Integer ceiling = set.ceiling(7); // >= 7
```

## HashMap

Average `O(1)` lookup. No key sorting.

```java
Map<String, Integer> map = new HashMap<>();
map.put("alice", 10);

int value = map.get("alice");
int defaultValue = map.getOrDefault("bob", 0);

map.put("apple", map.getOrDefault("apple", 0) + 1);

boolean exists = map.containsKey("alice");
map.remove("alice");

for (Map.Entry<String, Integer> entry : map.entrySet()) {
    String key = entry.getKey();
    int count = entry.getValue();
}
```

## TreeMap

Stores key-value pairs sorted by key.

```java
TreeMap<Integer, String> map = new TreeMap<>();
map.put(10, "ten");
map.put(5, "five");

int smallestKey = map.firstKey();
int largestKey = map.lastKey();

Integer floor = map.floorKey(7);     // <= 7
Integer ceiling = map.ceilingKey(7); // >= 7
```

## Arrays

```java
int[] arr = {3, 1, 2};
Arrays.sort(arr);

int n = arr.length;
```

Sort object arrays descending:

```java
Integer[] arr = {3, 1, 2};
Arrays.sort(arr, Collections.reverseOrder());
```

## Common traps

| Trap | Correct point |
| --- | --- |
| Java `PriorityQueue` is max heap | No. Default is min heap |
| `poll()` only removes | No. It removes and returns the value |
| `peek()` removes | No. It only reads |
| Use `Stack<Integer>` | Prefer `Deque<Integer> stack = new ArrayDeque<>()` |
| `int[]` can sort descending with `Collections.reverseOrder()` | No. Use `Integer[]` or custom logic |
| Comparator `a[0] - b[0]` is always safe | Can overflow; use `Integer.compare(a[0], b[0])` |

## Quick recall

**Q. Java min heap syntax?**  
A. `PriorityQueue<Integer> pq = new PriorityQueue<>();`

**Q. Java max heap syntax?**  
A. `PriorityQueue<Integer> pq = new PriorityQueue<>(Collections.reverseOrder());`

**Q. Stack syntax for interviews?**  
A. `Deque<Integer> stack = new ArrayDeque<>();`

**Q. Queue read/remove functions?**  
A. `peek()` reads, `poll()` removes and returns.
