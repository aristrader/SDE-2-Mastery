package org.example.backend_fundamentals.java.concurrency.thread_lifecycle.playground;

public class RunnableTaskRun {
    public static void main(String[] args) throws InterruptedException {
        Runnable task = new EmailTask();

        Thread worker = new Thread(task, "email-worker");
        worker.start();
        worker.join();

        System.out.println("main continues on " + Thread.currentThread().getName());
    }

    static final class EmailTask implements Runnable {
        @Override
        public void run() {
            System.out.println("send email on " + Thread.currentThread().getName());
        }
    }
}
