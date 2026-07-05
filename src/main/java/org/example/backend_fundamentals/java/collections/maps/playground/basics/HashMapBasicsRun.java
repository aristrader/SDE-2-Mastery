package org.example.backend_fundamentals.java.collections.maps.playground.basics;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic {@link HashMap} usage and its two distinguishing characteristics.
 *
 * <p>{@code HashMap} is the default map: O(1) average getValue/put. It allows <em>one</em> null
 * key and any number of null values, and it gives <em>no guarantee</em> about iteration
 * order — the order is determined by hash buckets and may change as the map grows.</p>
 *
 * <p>Compare with {@code LinkedHashMapBasicsRun} (insertion order) and
 * {@code TreeMapBasicsRun} (sorted by key).</p>
 */
public class HashMapBasicsRun {

    public static void main(String[] args) {
        // Keys are deliberately chosen with multi-character names so their hash codes
        // scatter across buckets. Single-character keys like "a","b","c","d" hash to
        // 97,98,99,100 → buckets 1,2,3,4 — which makes iteration LOOK alphabetical
        // and hides the fact that HashMap doesn't actually guarantee order.
        Map<String, String> map = new HashMap<>();
        map.put("zebra", "z");
        map.put("apple", "a");
        map.put("mango", "m");
        map.put("banana", "b");

        // HashMap accepts one null key and any number of null values.
        map.put(null, "the null key");
        System.out.println("Get null after first put:  " + map.get(null));

        // Inserting the same key replaces the previous value — true for any Map.
        map.put(null, "the null key was inserted again");
        System.out.println("Get null after second put: " + map.get(null));

        // Null values are allowed too.
        map.put("nullValue", null);
        System.out.println("Get 'nullValue' (null):    " + map.get("nullValue"));
        System.out.println();
        System.out.println("Iteration order (no guarantee — depends on hash buckets):");
        map.forEach((k, v) -> System.out.println("  " + k + " -> " + v));
    }
}
