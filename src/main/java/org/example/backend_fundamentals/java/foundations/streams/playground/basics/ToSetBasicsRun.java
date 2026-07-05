package org.example.backend_fundamentals.java.foundations.streams.playground.basics;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Demonstrates {@link Collectors#toSet()} and {@link Collectors#toUnmodifiableSet()}.
 *
 * <p>{@code toSet()} returns a mutable {@code HashSet} that automatically deduplicates
 * during collection. {@code toUnmodifiableSet()} (Java 10+) returns an immutable set with
 * the same dedup behaviour — but the result rejects all post-collection mutation.</p>
 *
 * <p>Like {@code Collectors.toList()}, {@code toSet()} doesn't guarantee any specific
 * implementation type — only "some mutable set." Don't rely on it being a {@code HashSet}
 * for ordering or null handling.</p>
 */
public class ToSetBasicsRun {

    public static void main(String[] args) {
        // toSet() — mutable, dedupes during collection
        Set<String> mutable = Stream.of("a", "b", "c", "a", "b")
                .collect(Collectors.toSet());
        System.out.println("Collectors.toSet() (after dedup): " + mutable);

        mutable.add("d");                                            // works
        System.out.println("After add('d'):                  " + mutable);

        // toUnmodifiableSet() — also dedupes, but the result rejects mutation
        Set<String> unmodifiable = Stream.of("a", "b", "c", "a")
                .collect(Collectors.toUnmodifiableSet());
        System.out.println("Collectors.toUnmodifiableSet():  " + unmodifiable);

        try {
            unmodifiable.add("d");
        } catch (Exception e) {
            System.out.println("toUnmodifiableSet — add() threw " + e.getClass().getSimpleName());
        }
    }
}
