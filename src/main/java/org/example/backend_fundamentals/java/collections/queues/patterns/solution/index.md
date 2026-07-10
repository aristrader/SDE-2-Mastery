---
order: 20
search: false
---

# Solutions

## Solution: queue-simulation - Queue Simulation

```java
Queue<String> line = new ArrayDeque<>();
line.offer("Alice");
line.offer("Bob");

String served = line.poll(); // Alice
System.out.println(served);
System.out.println(line); // [Bob]
```

FIFO means the first person who enters is served first.

## Solution: browser-history-mini-simulation - Browser History Mini Simulation

```java
Deque<String> backStack = new ArrayDeque<>();
Deque<String> forwardStack = new ArrayDeque<>();

String current = "home";
backStack.push(current);
current = "search";
backStack.push(current);
current = "product";

forwardStack.push(current);
current = backStack.pop(); // search

backStack.push(current);
current = forwardStack.pop(); // product
```

After back, current is `search`. After forward, current is `product`.

## Solution: compare-implementations - Compare Queue Implementations

Use `Queue` as the variable type when the code only needs FIFO behavior. It keeps callers independent of the concrete implementation.

`offer/poll/peek` return special values on failure, usually `false` or `null`. `add/remove/element` throw exceptions.

`ArrayDeque` is usually faster than `LinkedList` because it stores elements in a resizable array instead of separate node objects. That means less memory overhead and better cache locality.

Avoid `Stack`; use `ArrayDeque` through `Deque` for stack behavior.

Use `PriorityQueue` when removal order is based on natural ordering or a comparator, not insertion order.
