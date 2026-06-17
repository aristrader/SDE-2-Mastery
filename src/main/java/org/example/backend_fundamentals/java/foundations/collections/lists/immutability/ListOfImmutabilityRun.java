package org.example.backend_fundamentals.java.foundations.collections.lists.immutability;

import java.util.List;

/**
 * Demonstrates that {@link List#of(Object[])} returns a fully immutable list.
 *
 * <p>{@code add}, {@code remove}, and {@code set} all throw
 * {@link UnsupportedOperationException}. This is the strongest immutability flavour
 * in this package — there is no backing list to mutate either.</p>
 *
 * <p>Compare with {@code UnmodifiableListViewRun} (read-only view of a mutable backing
 * list) and {@code ArraysAsListTrapRun} (fixed size, but {@code set} works).</p>
 */
public class ListOfImmutabilityRun {

    public static void main(String[] args) {
        List<String> list = List.of("1st element", "2nd element");
        System.out.println("Initial: " + list);

        try {
            list.add("THIS");
        } catch (Exception e) {
            System.out.println("List.of is fully immutable — add() threw " + e.getClass().getSimpleName());
        }
    }
}
