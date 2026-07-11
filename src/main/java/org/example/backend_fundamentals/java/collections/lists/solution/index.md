---
order: 20
search: false
---

# Solutions

## Solution: list-practice-path - Choose the Right List Practice

Complete the child practice pages first. The short rule: use `ArrayList` for normal ordered data, `List.of` for fixed read-only values, `Arrays.asList` only when a fixed-size array view is intended, `Iterator` for safe removal during traversal, and `CopyOnWriteArrayList` only for read-heavy shared lists.

## Solution: list-task-inbox-lld - Build a Small Task Inbox

Use `ArrayList` internally because the inbox is ordered, append-heavy, and read by index/iteration. Do not return the internal list directly.

Reference shape:

```java
class TaskRepository {
    private final List<Task> tasks = new ArrayList<>();

    void add(Task task) {
        tasks.add(task);
    }

    boolean markDone(int id) {
        for (Task task : tasks) {
            if (task.id() == id) {
                task.markDone();
                return true;
            }
        }
        return false;
    }

    List<Task> openTasks() {
        return tasks.stream().filter(task -> !task.done()).toList();
    }

    List<Task> allTasks() {
        return List.copyOf(tasks);
    }
}
```

The important part is ownership: `TaskRepository` owns mutation, callers get snapshots.
