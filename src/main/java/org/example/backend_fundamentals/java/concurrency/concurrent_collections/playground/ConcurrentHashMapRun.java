package org.example.backend_fundamentals.java.concurrency.concurrent_collections.playground;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapRun {
    public static void main(String[] args) {
        ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();

        counts.merge("/users", 1, Integer::sum);
        counts.merge("/users", 1, Integer::sum);

        if (counts.get("/users") != 2) {
            throw new AssertionError("merge should count atomically per key");
        }

        boolean inserted = counts.putIfAbsent("/orders", 1) == null;
        boolean replaced = counts.replace("/orders", 1, 2);

        System.out.println("inserted=" + inserted + ", replaced=" + replaced);
    }
}
