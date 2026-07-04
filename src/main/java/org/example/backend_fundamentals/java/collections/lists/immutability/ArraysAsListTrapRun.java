package org.example.backend_fundamentals.java.collections.lists.immutability;

import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates the {@link Arrays#asList(Object[])} fixed-size trap.
 *
 * <p>{@link Arrays#asList} returns a list backed by the given array — {@code set} works
 * (replace in place) but {@code add} and {@code remove} throw
 * {@link UnsupportedOperationException}. The most surprising of the three "immutability"
 * flavours: the list looks mutable until you try to resize it.</p>
 *
 * <p>For a fully mutable list from an array: {@code new ArrayList<>(Arrays.asList(...))}.
 * For a fully immutable list: {@link List#of(Object[])}.</p>
 */
public class ArraysAsListTrapRun {

    public static void main(String[] args) {
        List<String> list = Arrays.asList("ABC", "DEF");
        System.out.println("Initial:  " + list);

        list.set(0, "SET WORKS");                                     // ← set() is allowed
        System.out.println("After set: " + list);

        try {
            list.add("New added");                                    // ← add() throws
        } catch (Exception e) {
            System.out.println("Fixed size — add() threw " + e.getClass().getSimpleName());
        }
    }
}
