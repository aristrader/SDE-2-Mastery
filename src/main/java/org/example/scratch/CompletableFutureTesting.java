package org.example.scratch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletableFutureTesting {
    public static void main(String[] args) {

        Set<String> operation = new HashSet<>();
        operation.add("1");
        operation.add("2");
        if(operation.stream().anyMatch(op -> op ==  "0" || op == "2")){
            System.out.println("HI MATCH FOUND!!");
        }

        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
                System.out.println("Thread woke up after 5 seconds.");
                return 1;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return -1;
            }
        });

        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                System.out.println("2nd future executed");
                return 2;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return -1;
            }
        });

        List<CompletableFuture<Integer>> futures = new ArrayList<>();
        futures.add(future1);
        futures.add(future2);

        // you cannot control the execution of the futures , which ever will finish first, will finish first.
        // but what you do with the results can be controlled and you can do is
        // when ever you are doing something with the result of the completed future in .thenAccept
        // you can use a join and you work will be then done in the sequential manner.
        // very helpful when you want the things to run in parallel but want that the results of one should depend on the other.

//        for (CompletableFuture<?> future : futures) {
//            future.thenAccept(System.out::println);
//        }
//
//        for (CompletableFuture<?> future : futures) {
//            future.thenAccept(System.out::println).join();
//        }

        final AtomicInteger sum = new AtomicInteger();
        for (CompletableFuture<?> future : futures) {
            if(sum.get() !=1){
                future.thenAccept(x -> {
                    sum.set(sum.get() + (Integer) x);
                }).join();
            } else {
                System.out.println("SEQUENTIAL HOGAI RESULTS KI PROCESSING");
            }
            System.out.println(sum);
        }

//        try {
//            TimeUnit.SECONDS.sleep(10);
//        } catch (Exception e){
//            System.out.println("EXCEPTION OCCURRED");
//        }


//        // Wait for the future to complete
//        future1.join(); // Blocks until the future completes
//        future2.join();
    }
}
