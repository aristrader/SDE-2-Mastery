package org.example.backend_fundamentals.java.collections.lists.playground.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Demonstrates how {@link CopyOnWriteArrayList} avoids the fail-fast iterator behaviour
 * of {@link ArrayList}.
 *
 * <p>Modifying an {@code ArrayList} during iteration throws
 * {@link java.util.ConcurrentModificationException}. {@code CopyOnWriteArrayList} gives
 * each iterator a snapshot of the array at iterator-creation time — subsequent writes
 * don't affect that iterator, and no exception is thrown. Trade-off: every write copies
 * the whole array. Use when reads vastly outnumber writes (listeners, caches).</p>
 */
public class CopyOnWriteSnapshotIteratorRun {

    public static void main(String[] args) {

        // ── ArrayList: modifying during iteration is fail-fast ────────────────────
        System.out.println("=== ArrayList — fail-fast iterator ===");
        List<String> arrayList = new ArrayList<>();
        arrayList.add("A");
        arrayList.add("B");
        arrayList.add("C");

        try {
            for (String s : arrayList) {
                System.out.println("  reading: " + s);
                if (s.equals("A")) {
                    arrayList.add("D");          // mutation during iteration → throws
                }
            }
        } catch (Exception e) {
            System.out.println("  Threw: " + e.getClass().getSimpleName());
        }

        // ── CopyOnWriteArrayList: iterator sees a snapshot ────────────────────────
        System.out.println();
        System.out.println("=== CopyOnWriteArrayList — iterator sees a snapshot ===");
        List<String> cowList = new CopyOnWriteArrayList<>();
        cowList.add("A");
        cowList.add("B");
        cowList.add("C");

        for (String s : cowList) {
            System.out.println("  reading: " + s);
            if (s.equals("A")) {
                cowList.add("D");                // no exception; iterator continues over the original snapshot
            }
        }

        // The write DID happen — it just wasn't visible to the iterator above.
        System.out.println("Final list contents: " + cowList);
    }
}
