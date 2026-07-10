---
order: 20
search: false
---

# Solutions

## Solution: queue-with-arraydeque - Queue with ArrayDeque

```java
Queue<Integer> q = new ArrayDeque<>();
q.offer(10);
q.offer(20);
q.offer(30);

System.out.println(q.peek()); // 10, not removed
System.out.println(q.poll()); // 10, removed
System.out.println(q.poll()); // 20
System.out.println(q.poll()); // 30
System.out.println(q.isEmpty()); // true
```

`ArrayDeque` is usually preferred over `LinkedList` because it has less memory overhead and better cache locality.

## Solution: empty-queue - Empty Queue Methods

```java
Queue<Integer> q = new ArrayDeque<>();

System.out.println(q.peek()); // null
System.out.println(q.poll()); // null

// q.element(); // NoSuchElementException
// q.remove();  // NoSuchElementException
```

Use `peek`/`poll` when empty is normal. Use `element`/`remove` when empty should be treated as a bug.
