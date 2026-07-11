---
order: 10
search: false
---

# Practice

## Exercise: list-practice-path - Choose the Right List Practice

### Objective
Use this page as the parent checkpoint for the list study path.

### Task
Complete the child practice pages in order:

1. `basics`
2. `immutability`
3. `iteration`
4. `performance`

### Checks
- Can you choose between `ArrayList`, `List.of`, `Arrays.asList`, and `CopyOnWriteArrayList`?
- Can you explain when list practice should move to Sets, Maps, or Sorting instead?

## Exercise: list-task-inbox-lld - Build a Small Task Inbox

### Objective
Use multiple Java files to model a small list-backed task inbox.

### Task
Open the Code tab and use the `lld/TaskInboxRun.java` playground group.

Implement or extend the support classes so the inbox can:

1. Add tasks in insertion order.
2. Mark one task as done by id.
3. Return only open tasks.
4. Return all tasks as a defensive copy so callers cannot mutate internal state.

### Checks
- Keep the storage list private inside the repository.
- Use `ArrayList` for the mutable internal list.
- Return `List.copyOf(...)` or a new `ArrayList<>(...)` when exposing stored tasks.
- Keep the runner small; the real behavior should live in support classes.
