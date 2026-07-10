---
order: 10
search: false
---

# Practice

## Exercise: queue-with-arraydeque - Queue with ArrayDeque

### Objective
Practice FIFO queue operations using the normal implementation.

### Task
Create `Queue<Integer> q = new ArrayDeque<>();`, add `10`, `20`, `30`, then use `peek()`, `poll()`, and `isEmpty()`.

### Checks
- Which value is returned first?
- Does `peek()` remove the value?
- Does `poll()` remove the value?
- Why is `ArrayDeque` usually preferred over `LinkedList`?

## Exercise: empty-queue - Empty Queue Methods

### Objective
Compare exception-throwing and null-returning queue methods.

### Task
Create an empty queue and call:

- `peek()`
- `poll()`
- `element()`
- `remove()`

### Checks
- Which methods return `null`?
- Which methods throw `NoSuchElementException`?
