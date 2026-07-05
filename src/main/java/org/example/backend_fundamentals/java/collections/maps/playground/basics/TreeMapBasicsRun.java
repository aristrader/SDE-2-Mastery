package org.example.backend_fundamentals.java.collections.maps.playground.basics;

import java.util.Map;
import java.util.TreeMap;

/**
 * Demonstrates {@link TreeMap}: sorted iteration by key, and its rejection of null keys.
 *
 * <p>{@code TreeMap} is a red-black tree internally — getValue/put are O(log n), not O(1) like
 * {@code HashMap}, but iteration is naturally sorted by key (or by a custom {@code Comparator}).
 * Null keys throw {@link NullPointerException} because the tree needs to compare keys to
 * place them.</p>
 */
public class TreeMapBasicsRun {

    public static void main(String[] args) {
        Map<String, Integer> treeMap = new TreeMap<>();
        treeMap.put("zebra", 1);
        treeMap.put("apple", 2);
        treeMap.put("mango", 3);
        treeMap.put("banana", 4);

        System.out.println("TreeMap iteration (sorted by key):");
        treeMap.forEach((k, v) -> System.out.println("  " + k + " -> " + v));

        try {
            treeMap.put(null, 5);                     // tree can't compare null to anything
        } catch (Exception e) {
            System.out.println();
            System.out.println("Null key rejected: " + e.getClass().getSimpleName());
        }
    }
}
