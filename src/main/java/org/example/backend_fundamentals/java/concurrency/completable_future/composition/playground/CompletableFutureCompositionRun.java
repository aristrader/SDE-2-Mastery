package org.example.backend_fundamentals.java.concurrency.completable_future.composition.playground;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CompletableFutureCompositionRun {
    public static void main(String[] args) {
        CompletableFuture<String> userFuture = CompletableFuture.completedFuture("user-1");
        CompletableFuture<List<String>> ordersFuture = userFuture.thenCompose(
                user -> CompletableFuture.completedFuture(List.of("order-for-" + user)));

        CompletableFuture<String> accountFuture = CompletableFuture.completedFuture("account");
        CompletableFuture<String> combined = userFuture.thenCombine(accountFuture, (user, account) ->
                user + ":" + account);

        System.out.println(ordersFuture.join());
        System.out.println(combined.join());
    }
}
