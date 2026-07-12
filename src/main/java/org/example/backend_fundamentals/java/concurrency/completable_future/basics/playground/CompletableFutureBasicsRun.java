package org.example.backend_fundamentals.java.concurrency.completable_future.basics.playground;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureBasicsRun {
    public static void main(String[] args) {
        CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> 42)
                .thenApply(value -> "answer=" + value);

        String result = future.join();
        if (!"answer=42".equals(result)) {
            throw new AssertionError(result);
        }
        System.out.println(result);
    }
}
