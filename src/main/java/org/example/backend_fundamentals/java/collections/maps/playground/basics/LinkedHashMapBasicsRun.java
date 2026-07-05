package org.example.backend_fundamentals.java.collections.maps.playground.basics;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Demonstrates that {@link LinkedHashMap} preserves insertion order during iteration,
 * while {@link HashMap} does not.
 *
 * <p>Same data, inserted in deliberately non-sorted order, into both maps. Printing
 * the contents shows {@code LinkedHashMap} matches insertion order ({@code zebra, apple,
 * mango, banana}); {@code HashMap} does not (its order depends on hash distribution).</p>
 *
 * <p>The cost: {@code LinkedHashMap} carries an extra doubly-linked-list pointer per entry
 * to maintain order. Negligible for most use cases.</p>
 */
public class LinkedHashMapBasicsRun {

    public static void main(String[] args) {
        Map<String, Integer> linkedMap = new LinkedHashMap<>();
        linkedMap.put("zebra", 1);
        linkedMap.put("apple", 2);
        linkedMap.put("mango", 3);
        linkedMap.put("banana", 4);
        linkedMap.put(null, 4);

        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("zebra", 1);
        hashMap.put("apple", 2);
        hashMap.put("mango", 3);
        hashMap.put("banana", 4);
        hashMap.put(null, 4);

        System.out.println("LinkedHashMap (insertion order):");
        linkedMap.forEach((k, v) -> System.out.println("  " + k + " -> " + v));

        System.out.println();
        System.out.println("HashMap (no order guarantee):");
        hashMap.forEach((k, v) -> System.out.println("  " + k + " -> " + v));
    }
}
