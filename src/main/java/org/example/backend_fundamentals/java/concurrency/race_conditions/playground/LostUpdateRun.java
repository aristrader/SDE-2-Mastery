package org.example.backend_fundamentals.java.concurrency.race_conditions.playground;

import java.util.concurrent.atomic.AtomicInteger;

public class LostUpdateRun {
    private static int unsafe;
    private static final AtomicInteger safe = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {
        Runnable task = () -> {
            for (int i = 0; i < 100_000; i++) {
                unsafe++;
                safe.incrementAndGet();
            }
        };

        Thread first = new Thread(task);
        Thread second = new Thread(task);
        first.start();
        second.start();
        first.join();
        second.join();

        System.out.println("unsafe=" + unsafe);
        System.out.println("safe=" + safe.get());
    }
}
