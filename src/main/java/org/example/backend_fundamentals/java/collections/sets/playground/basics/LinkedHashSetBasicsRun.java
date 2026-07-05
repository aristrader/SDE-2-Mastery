package org.example.backend_fundamentals.java.collections.sets.playground.basics;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Demonstrates that {@link LinkedHashSet} preserves insertion order during iteration,
 * while {@link HashSet} does not.
 *
 * <p>Same data, inserted in deliberately non-sorted order, into both sets. {@code LinkedHashSet}
 * iterates in insertion order ({@code zebra, apple, mango, banana}); {@code HashSet} does not
 * (its order depends on hash-bucket distribution).</p>
 *
 * <p>Cost: an extra doubly-linked-list pointer per element. Negligible for most use cases.</p>
 */
public class LinkedHashSetBasicsRun {

    public static void main(String[] args) {
        Set<String> linked = new LinkedHashSet<>();
        linked.add("zebra");
        linked.add("apple");
        linked.add("mango");
        linked.add("banana");
        linked.add(null);

        Set<String> hash = new HashSet<>();
        hash.add("zebra");
        hash.add("apple");
        hash.add("mango");
        hash.add("banana");
        hash.add(null);

        System.out.println("LinkedHashSet (insertion order):");
        linked.forEach(s -> System.out.println("  " + s));

        System.out.println();
        System.out.println("HashSet (no order guarantee):");
        hash.forEach(s -> System.out.println("  " + s));
    }
}
