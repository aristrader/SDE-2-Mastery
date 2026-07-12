package org.example.backend_fundamentals.java.concurrency.synchronized_keyword.playground;

public class SynchronizedCounterRun {
    public static void main(String[] args) throws InterruptedException {
        Counter counter = new Counter();
        Runnable task = () -> {
            for (int i = 0; i < 100_000; i++) {
                counter.increment();
            }
        };

        Thread first = new Thread(task);
        Thread second = new Thread(task);
        first.start();
        second.start();
        first.join();
        second.join();

        if (counter.value() != 200_000) {
            throw new AssertionError("counter lost updates");
        }
        System.out.println("counter=" + counter.value());
    }

    static final class Counter {
        private int value;

        synchronized void increment() {
            value++;
        }

        synchronized int value() {
            return value;
        }
    }
}
