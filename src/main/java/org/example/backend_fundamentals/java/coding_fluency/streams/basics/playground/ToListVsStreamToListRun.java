package org.example.backend_fundamentals.java.coding_fluency.streams.basics.playground;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Demonstrates the difference between {@link Collectors#toList()} and {@link Stream#toList()}.
 *
 * <p>{@link Collectors#toList()} (Java 8+) returns a <em>mutable</em> {@code ArrayList} —
 * you can add, remove, or replace elements after collection. {@link Stream#toList()}
 * (Java 16+) returns an <em>unmodifiable</em> {@code List} — every mutation method throws.
 * Choose based on whether the caller will mutate the result.</p>
 */
public class ToListVsStreamToListRun {

    public static void main(String[] args) {
        // Collectors.toList() — mutable ArrayList
        List<String> mutable = Stream.of("a", "b", "c")
                .collect(Collectors.toList());
        mutable.add("d");                                            // works
        System.out.println("Collectors.toList() (mutable): " + mutable);

        // Stream.toList() — unmodifiable
        List<String> unmodifiable = Stream.of("a", "b", "c").toList();
        try {
            unmodifiable.add("d");
        } catch (Exception e) {
            System.out.println("Stream.toList() — add() threw " + e.getClass().getSimpleName());
        }
        System.out.println("Stream.toList() (unmodifiable): " + unmodifiable);

        // Collectors.toUnmodifiableList() (Java 10+) — same effect as Stream.toList()
        List<String> unmodifiableViaCollector = Stream.of("a", "b", "c")
                .collect(Collectors.toUnmodifiableList());
        try {
            unmodifiableViaCollector.add("d");
        } catch (Exception e) {
            System.out.println("Collectors.toUnmodifiableList() — add() threw " + e.getClass().getSimpleName());
        }
    }
}
