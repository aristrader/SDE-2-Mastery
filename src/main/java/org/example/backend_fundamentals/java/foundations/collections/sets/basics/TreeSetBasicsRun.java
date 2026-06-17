package org.example.backend_fundamentals.java.foundations.collections.sets.basics;

import java.util.Set;
import java.util.TreeSet;

/**
 * Demonstrates {@link TreeSet}: sorted iteration by element, and its rejection of null.
 *
 * <p>{@code TreeSet} is a red-black tree internally — {@code add}/{@code contains}/
 * {@code remove} are O(log n), not O(1) like {@code HashSet}, but iteration is naturally
 * sorted by natural ordering (or by a custom {@code Comparator}). Null elements throw
 * {@link NullPointerException} because the tree needs to compare them to place them.</p>
 */
public class TreeSetBasicsRun {

    public static void main(String[] args) {
        Set<String> tree = new TreeSet<>();
        tree.add("zebra");
        tree.add("apple");
        tree.add("mango");
        tree.add("banana");

        System.out.println("TreeSet iteration (sorted):");
        tree.forEach(s -> System.out.println("  " + s));

        try {
            tree.add(null);                   // tree can't compare null to anything
        } catch (Exception e) {
            System.out.println();
            System.out.println("Null rejected: " + e.getClass().getSimpleName());
        }
    }
}
