---
order: 10
search: false
---

# Practice

## Exercise: queue-simulation - Queue Simulation

### Objective
Model a real FIFO line.

### Task
Use a queue to simulate a service counter:

1. Alice enters.
2. Bob enters.
3. Serve one person.
4. Print the queue after each step.

### Checks
- Who is served first?
- What remains in the queue?

## Exercise: browser-history-mini-simulation - Browser History Mini Simulation

### Objective
Use two stacks to model back/forward browser history.

### Task
Use two `Deque<String>` instances:

- `backStack`
- `forwardStack`

Visit three pages, go back once, then go forward once.

### Checks
- What is the current page after back?
- What is the current page after forward?

## Exercise: compare-implementations - Compare Queue Implementations

### Objective
Choose the right queue/deque implementation.

### Task
Answer these prompts:

1. Why use the `Queue` interface as the variable type?
2. What is the difference between `offer/poll/peek` and `add/remove/element`?
3. Why is `ArrayDeque` often faster than `LinkedList`?
4. Why avoid `Stack`?
5. When would you use `PriorityQueue`?

### Checks
- Mention memory overhead.
- Mention cache locality.
- Mention FIFO vs priority ordering.
