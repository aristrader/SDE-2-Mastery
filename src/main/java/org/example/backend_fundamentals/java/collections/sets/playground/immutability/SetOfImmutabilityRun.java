package org.example.backend_fundamentals.java.collections.sets.playground.immutability;

import java.util.Set;

/**
 * Demonstrates that {@link Set#of(Object[])} returns a fully immutable set with two
 * additional fail-fast checks at construction.
 *
 * <p>Like {@code List.of} and {@code Map.of}, {@code Set.of}:</p>
 * <ul>
 *   <li>Throws {@link UnsupportedOperationException} on {@code add}, {@code remove}, {@code clear}.</li>
 *   <li>Rejects null elements at construction (immutable collections forbid null).</li>
 * </ul>
 *
 * <p>{@code Set.of} also rejects <strong>duplicate</strong> elements at construction with
 * {@link IllegalArgumentException} — unlike {@link java.util.HashSet} which silently
 * deduplicates. This is fail-fast: if your literal set has duplicates, that's almost
 * certainly a bug; better to find out at construction than to silently shrink.</p>
 */
public class SetOfImmutabilityRun {

    public static void main(String[] args) {
        Set<String> set = Set.of("a", "b", "c");
        System.out.println("Initial: " + set);

        try {
            set.add("d");
        } catch (Exception e) {
            System.out.println("Set.of is immutable — add() threw " + e.getClass().getSimpleName());
        }

        try {
            Set.of("a", null);                                  // null at construction
        } catch (Exception e) {
            System.out.println("Set.of rejects null at construction —      " + e.getClass().getSimpleName());
        }

        try {
            Set.of("a", "a");                                   // duplicates at construction
        } catch (Exception e) {
            System.out.println("Set.of rejects duplicates at construction — " + e.getClass().getSimpleName());
        }
    }
}
