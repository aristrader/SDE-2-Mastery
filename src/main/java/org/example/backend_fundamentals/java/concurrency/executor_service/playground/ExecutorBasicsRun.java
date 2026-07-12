package org.example.backend_fundamentals.java.concurrency.executor_service.playground;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorBasicsRun {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                int taskId = i;
                futures.add(executor.submit(() -> taskId));
            }

            int sum = 0;
            for (Future<Integer> future : futures) {
                sum += future.get();
            }
            if (sum != 55) {
                throw new AssertionError("unexpected sum");
            }
            System.out.println("sum=" + sum);
        } finally {
            executor.shutdown();
        }
    }
}
