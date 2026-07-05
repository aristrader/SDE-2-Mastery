package org.example.backend_fundamentals.java.collections.sets.playground.traps;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Demonstrates how a broken {@code equals}/{@code hashCode} contract breaks
 * {@link HashSet} dedup.
 *
 * <p>{@link HashSet} is backed internally by a {@link java.util.HashMap} where elements
 * are keys. A broken contract on the element type causes the set to fail at its primary
 * job: rejecting duplicates. Two {@code new BadKey("hello")} instances are different
 * objects (identity-based defaults), so the set stores both — silently — and ends up
 * with multiple "logically equal" elements. Looking up a fresh equal instance also
 * returns false from {@code contains}.</p>
 *
 * <p>The {@code GoodKey} side overrides both methods based on the same field, so the
 * second {@code add} of an equal element returns {@code false} and the size stays at 1.</p>
 */
public class BrokenEqualsHashCodeSetTrapRun {

    /** No equals/hashCode override — inherits Object's identity-based defaults. */
    static class BadKey {
        final String value;
        BadKey(String value) { this.value = value; }
        @Override public String toString() { return "BadKey(" + value + ")"; }
    }

    /** equals + hashCode based on the same field. */
    static class GoodKey {
        final String value;
        GoodKey(String value) { this.value = value; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GoodKey)) return false;
            return Objects.equals(value, ((GoodKey) o).value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override public String toString() { return "GoodKey(" + value + ")"; }
    }

    public static void main(String[] args) {
        System.out.println("=== BadKey — no equals/hashCode override ===");
        Set<BadKey> badSet = new HashSet<>();
        badSet.add(new BadKey("hello"));
        badSet.add(new BadKey("hello"));                                       // should dedupe — doesn't
        badSet.add(new BadKey("hello"));
        System.out.println("Size after adding 'hello' three times: " + badSet.size());     // 3
        System.out.println("contains(new BadKey(\"hello\")):         " + badSet.contains(new BadKey("hello"))); // false

        System.out.println();
        System.out.println("=== GoodKey — equals + hashCode override ===");
        Set<GoodKey> goodSet = new HashSet<>();
        goodSet.add(new GoodKey("hello"));
        goodSet.add(new GoodKey("hello"));
        goodSet.add(new GoodKey("hello"));
        System.out.println("Size after adding 'hello' three times: " + goodSet.size());    // 1
        System.out.println("contains(new GoodKey(\"hello\")):        " + goodSet.contains(new GoodKey("hello"))); // true
    }
}
