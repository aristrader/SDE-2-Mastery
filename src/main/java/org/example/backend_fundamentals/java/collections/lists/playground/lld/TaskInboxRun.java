package org.example.backend_fundamentals.java.collections.lists.playground.lld;

public class TaskInboxRun {
    public static void main(String[] args) {
        TaskRepository repository = new TaskRepository();
        repository.add(new Task(1, "Review list basics"));
        repository.add(new Task(2, "Practice defensive copies"));
        repository.add(new Task(3, "Run the multi-file workspace"));

        repository.markDone(2);

        System.out.println("All tasks:");
        repository.allTasks().forEach(System.out::println);

        System.out.println();
        System.out.println("Open tasks:");
        repository.openTasks().forEach(System.out::println);
    }
}
