package org.example.backend_fundamentals.java.collections.lists.playground.lld;

public class Task {
    private final int id;
    private final String title;
    private boolean done;

    public Task(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int id() {
        return id;
    }

    public boolean done() {
        return done;
    }

    public void markDone() {
        done = true;
    }

    @Override
    public String toString() {
        return (done ? "[done] " : "[open] ") + id + " - " + title;
    }
}
