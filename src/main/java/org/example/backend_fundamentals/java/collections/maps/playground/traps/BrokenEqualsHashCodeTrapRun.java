package org.example.backend_fundamentals.java.collections.maps.playground.traps;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Demonstrates how a broken {@code equals}/{@code hashCode} contract silently breaks
 * {@link HashMap} lookup.
 *
 * <p>{@code HashMap.getValue(key)} works in two steps: hash the key to find the bucket,
 * then walk the bucket comparing entries with {@code equals}. If a class doesn't override
 * {@code equals} and {@code hashCode}, it inherits identity-based versions from
 * {@link Object} — meaning two {@code new BadKey("hello")} instances are considered
 * different keys, even though they look the same. Lookups return {@code null} silently.</p>
 *
 * <p>The fix is to override <em>both</em> methods, based on the same fields. The contract:
 * if {@code a.equals(b)} is {@code true}, then {@code a.hashCode() == b.hashCode()} must
 * also be {@code true}.</p>
 */
public class BrokenEqualsHashCodeTrapRun {

    /** No equals/hashCode override — inherits identity-based behaviour. */
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
        Map<BadKey, String> badMap = new HashMap<>();
        BadKey storedBadKey = new BadKey("hello");
        badMap.put(storedBadKey, "world");

        // Same instance: works (identity matches)
        System.out.println("getValue(storedBadKey):              " + badMap.get(storedBadKey));
        // Different instance with same value: silently null
        System.out.println("getValue(new BadKey(\"hello\")):       " + badMap.get(new BadKey("hello")));
        System.out.println("Map size:                       " + badMap.size());

        System.out.println();
        System.out.println("=== GoodKey — equals + hashCode override ===");
        Map<GoodKey, String> goodMap = new HashMap<>();
        goodMap.put(new GoodKey("hello"), "world");

        // Different instance, same value: works correctly
        System.out.println("getValue(new GoodKey(\"hello\")):     " + goodMap.get(new GoodKey("hello")));
        System.out.println("Map size:                       " + goodMap.size());
    }
}
