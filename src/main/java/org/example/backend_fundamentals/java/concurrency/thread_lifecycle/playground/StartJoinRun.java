package org.example.backend_fundamentals.java.concurrency.thread_lifecycle.playground;

public class StartJoinRun {
    private static int value;

    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> value = 42, "worker");

        System.out.println("before start: " + worker.getState());
        worker.start();
        worker.join();

        if (value != 42) {
            throw new AssertionError("join should make worker write visible");
        }

        System.out.println("after join: " + worker.getState());
    }
}
