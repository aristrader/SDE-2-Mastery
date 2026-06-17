package org.example.backend_fundamentals.java.foundations.collections.maps.concurrent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates that {@link ConcurrentHashMap} is thread-safe under contention while a
 * plain {@link HashMap} is not, and that {@code ConcurrentHashMap} rejects null keys/values.
 *
 * <p>Eight threads each call {@code merge("counter", 1, Integer::sum)} 10,000 times.
 * Expected total: 80,000. {@code HashMap.merge} is not atomic — concurrent threads read
 * the same value and overwrite each other, so the final count is far less than expected
 * (lost updates). {@code ConcurrentHashMap.merge} is atomic; the count comes out exact.</p>
 *
 * <p>Note: under heavy contention, {@code HashMap} can also throw
 * {@link NullPointerException} from internal state inconsistencies. The exception count
 * here may vary by run — the qualitative result (HashMap is wrong, ConcurrentHashMap is
 * right) is stable.</p>
 */
public class ConcurrentHashMapBasicsRun {

    private static final int THREADS = 8;
    private static final int INCREMENTS_PER_THREAD = 10_000;

    public static void main(String[] args) throws InterruptedException {
        // Null rejection — both keys and values
        try {
            new ConcurrentHashMap<String, String>().put(null, "x");
        } catch (Exception e) {
            System.out.println("ConcurrentHashMap rejects null keys —   " + e.getClass().getSimpleName());
        }
        try {
            new ConcurrentHashMap<String, String>().put("k", null);
        } catch (Exception e) {
            System.out.println("ConcurrentHashMap rejects null values — " + e.getClass().getSimpleName());
        }

        System.out.println();
        System.out.println("=== HashMap with concurrent merges (lost updates expected) ===");
        runCounterDemo(new HashMap<>());

        System.out.println();
        System.out.println("=== ConcurrentHashMap with concurrent merges ===");
        runCounterDemo(new ConcurrentHashMap<>());
    }

    private static void runCounterDemo(Map<String, Integer> map) throws InterruptedException {
        map.put("counter", 0);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        int[] failures = {0};

        for (int i = 0; i < THREADS; i++) {
            pool.submit(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    try {
                        map.merge("counter", 1, Integer::sum);
                    } catch (Exception e) {
                        synchronized (failures) { failures[0]++; }
                    }
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        int expected = THREADS * INCREMENTS_PER_THREAD;
        Integer actual = map.get("counter");
        System.out.println("  Expected: " + expected);
        System.out.println("  Actual:   " + actual);
        System.out.println("  Failures (exceptions): " + failures[0]);
        System.out.println("  Result: " + (Integer.valueOf(expected).equals(actual) ? "correct" : "lost updates"));
    }
}
