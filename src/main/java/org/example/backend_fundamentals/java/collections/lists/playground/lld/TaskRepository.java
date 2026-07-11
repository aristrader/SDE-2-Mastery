package org.example.backend_fundamentals.java.collections.lists.playground.lld;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private final List<Task> tasks = new ArrayList<>();

    public void add(Task task) {
        tasks.add(task);
    }

    public boolean markDone(int id) {
        for (Task task : tasks) {
            if (task.id() == id) {
                task.markDone();
                return true;
            }
        }
        return false;
    }

    public List<Task> openTasks() {
        return tasks.stream()
                .filter(task -> !task.done())
                .toList();
    }

    public List<Task> allTasks() {
        return List.copyOf(tasks);
    }
}
