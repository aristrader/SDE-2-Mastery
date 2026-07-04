package org.example.backend_fundamentals.java.collections.lists.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates that {@link CopyOnWriteArrayList} is thread-safe without external locking,
 * while a plain {@link ArrayList} throws under the same workload.
 *
 * <p>Four reader threads iterate the list 100 times each. One writer thread appends 1000
 * elements. No synchronisation. {@code ArrayList}'s fail-fast iterators detect the
 * concurrent modification and throw; {@code CopyOnWriteArrayList} iterators work over
 * snapshots and complete cleanly. Exact exception counts vary by run; qualitative result
 * is stable.</p>
 */
public class CopyOnWriteThreadSafetyRun {

    private static final int READERS = 4;
    private static final int READS_PER_THREAD = 100;
    private static final int INITIAL_SIZE = 100;
    private static final int WRITES = 1000;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ArrayList with concurrent reader + writer ===");
        runDemo(new ArrayList<>());

        System.out.println();
        System.out.println("=== CopyOnWriteArrayList with concurrent reader + writer ===");
        runDemo(new CopyOnWriteArrayList<>());
    }

    private static void runDemo(List<Integer> list) throws InterruptedException {
        for (int i = 0; i < INITIAL_SIZE; i++) {
            list.add(i);
        }

        AtomicInteger successfulReads = new AtomicInteger();
        AtomicInteger exceptions = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(READERS + 1);

        // Reader threads — each iterates the list READS_PER_THREAD times
        for (int i = 0; i < READERS; i++) {
            pool.submit(() -> {
                for (int round = 0; round < READS_PER_THREAD; round++) {
                    try {
                        long sum = 0;
                        for (int n : list) {                  // iterator pinned at this moment
                            sum += n;
                        }
                        successfulReads.incrementAndGet();
                    } catch (Exception e) {
                        exceptions.incrementAndGet();
                    }
                }
            });
        }

        // Writer thread — appends WRITES new elements
        pool.submit(() -> {
            for (int i = INITIAL_SIZE; i < INITIAL_SIZE + WRITES; i++) {
                list.add(i);
            }
        });

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("  Successful iterations: " + successfulReads.get() + " / " + (READERS * READS_PER_THREAD));
        System.out.println("  Exceptions thrown:     " + exceptions.get());
        System.out.println("  Final list size:       " + list.size());
    }
}
