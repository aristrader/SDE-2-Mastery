package org.example.backend_fundamentals.java.collections.sets.playground.concurrent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates the thread-safe set obtained via {@link ConcurrentHashMap#newKeySet()}.
 *
 * <p>Java does not have a {@code ConcurrentHashSet} class. The standard way to getValue a
 * thread-safe set is {@link ConcurrentHashMap#newKeySet()} (Java 8+), which is backed
 * by a {@link ConcurrentHashMap} and inherits all of its concurrency guarantees:
 * lock-free reads, bucket-level striped locks for writes, atomic compound operations.</p>
 *
 * <p>Eight threads each insert 10,000 unique integers concurrently. Expected size: 80,000.
 * {@link HashSet} under the same workload typically loses inserts (size lower than expected)
 * because non-atomic bucket updates clobber each other. {@code ConcurrentHashMap.newKeySet()}
 * always returns the exact expected size.</p>
 */
public class ConcurrentHashSetBasicsRun {

    private static final int THREADS = 8;
    private static final int ADDS_PER_THREAD = 10_000;

    public static void main(String[] args) throws InterruptedException {
        Set<Integer> concurrentSet = ConcurrentHashMap.newKeySet();

        // Null rejection — same as ConcurrentHashMap
        try {
            concurrentSet.add(null);
        } catch (Exception e) {
            System.out.println("ConcurrentHashMap.newKeySet rejects null — " + e.getClass().getSimpleName());
        }

        System.out.println();
        System.out.println("=== HashSet with concurrent adds (lost adds expected) ===");
        runConcurrentAdds(new HashSet<>());

        System.out.println();
        System.out.println("=== ConcurrentHashMap.newKeySet() with concurrent adds ===");
        runConcurrentAdds(concurrentSet);
    }

    private static void runConcurrentAdds(Set<Integer> set) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        AtomicInteger failures = new AtomicInteger();

        for (int t = 0; t < THREADS; t++) {
            final int base = t * ADDS_PER_THREAD;
            pool.submit(() -> {
                for (int i = 0; i < ADDS_PER_THREAD; i++) {
                    try {
                        set.add(base + i);
                    } catch (Exception e) {
                        failures.incrementAndGet();
                    }
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        int expected = THREADS * ADDS_PER_THREAD;
        System.out.println("  Expected size: " + expected);
        System.out.println("  Actual size:   " + set.size());
        System.out.println("  Failures:      " + failures.get());
        System.out.println("  Result: " + (set.size() == expected ? "correct" : "lost adds"));
    }
}
