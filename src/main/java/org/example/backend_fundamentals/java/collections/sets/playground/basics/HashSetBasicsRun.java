package org.example.backend_fundamentals.java.collections.sets.playground.basics;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic {@link HashSet} usage and its three distinguishing characteristics.
 *
 * <p>{@code HashSet} is the default mutable set: O(1) average {@code add}/{@code contains}/
 * {@code remove}. It deduplicates (adding an existing element returns {@code false} and
 * doesn't grow the set), allows one null element, and gives no iteration-order guarantee.</p>
 *
 * <p>Internally it's backed by a {@link java.util.HashMap} where elements are keys and
 * a sentinel object is the value — every property of {@code HashMap} keys (the
 * {@code equals}/{@code hashCode} contract, bucket-walk iteration order) applies here.</p>
 */
public class HashSetBasicsRun {

    public static void main(String[] args) {
        Set<String> set = new HashSet<>();
        set.add("zebra");
        set.add("mango");
        set.add("banana");

        // Dedupe — add() returns true on first insert, false when already present
        boolean firstAdd = set.add("apple");          // not in set yet
        boolean duplicateAdd = set.add("apple");      // already in set
        System.out.println("First add('apple'):     " + firstAdd);     // true
        System.out.println("Duplicate add('apple'): " + duplicateAdd); // false
        System.out.println("Size after duplicates:  " + set.size());   // 4 (apple counted once)

        // Null is allowed (one)
        set.add(null);
        System.out.println("Contains null:          " + set.contains(null));
        System.out.println("Size with null:         " + set.size());

        System.out.println();
        System.out.println("Iteration order (no guarantee — bucket order):");
        set.forEach(s -> System.out.println("  " + s));
    }
}
