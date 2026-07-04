package org.example.backend_fundamentals.java.collections.maps.immutability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Demonstrates that {@link Map#of(Object, Object)} returns a <em>shallowly</em> immutable map.
 *
 * <p>The map structure is frozen — {@code put}, {@code remove}, {@code clear}, {@code merge},
 * {@code compute}, etc. all throw {@link UnsupportedOperationException}. {@code Map.of} also
 * rejects null keys and null values at construction time (fail-fast).</p>
 *
 * <p><strong>The shallow caveat:</strong> {@code Map.of} freezes which value reference each
 * key points to, but it does <em>not</em> freeze the value objects themselves. If a value is
 * a mutable type (e.g., a {@link java.util.List}), anyone with a reference to it can still
 * mutate its contents through the map. The fix for deep immutability is to wrap the value
 * with {@link List#copyOf(java.util.Collection)} (or equivalent) before putting it in.</p>
 *
 * <p>Limit: the {@code Map.of} overloads only support up to 10 entries. For more, use
 * {@link Map#ofEntries(Map.Entry[])}.</p>
 */
public class MapOfImmutabilityRun {

    public static void main(String[] args) {
        Map<String, Integer> map = Map.of("a", 1, "b", 2, "c", 3);
        System.out.println("Initial: " + map);
        System.out.println("Get 'a': " + map.get("a"));

        try {
            map.put("d", 4);
        } catch (Exception e) {
            System.out.println("Map.of is immutable — put() threw " + e.getClass().getSimpleName());
        }

        // merge() looks like read-then-maybe-write, but on Map.of every mutation path throws —
        // including compute(), computeIfAbsent(), computeIfPresent(), replace(), replaceAll(), etc.
        try {
            map.merge("a", 1, Integer::sum);
        } catch (Exception e) {
            System.out.println("Map.of is immutable — merge() threw " + e.getClass().getSimpleName());
        }

        try {
            Map.of("k", null);                                  // null value at construction
        } catch (Exception e) {
            System.out.println("Map.of rejects null values at construction — " + e.getClass().getSimpleName());
        }

        try {
            Map.of(null, 1);                                    // null key at construction
        } catch (Exception e) {
            System.out.println("Map.of rejects null keys at construction — " + e.getClass().getSimpleName());
        }

        // ── Shallow immutability — Map.of freezes the *mapping*, not the value object ──
        System.out.println();
        System.out.println("--- Shallow immutability trap ---");

        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3));
        Map<String, List<Integer>> shallow = Map.of("nums", nums);
        System.out.println("Initial:                       " + shallow);

        try {
            shallow.put("other", new ArrayList<>());            // mutating the MAP — throws
        } catch (Exception e) {
            System.out.println("Map structure is frozen —      put() threw " + e.getClass().getSimpleName());
        }

        // BUT: the value's own state is NOT frozen — you got a reference, you can mutate it.
        shallow.get("nums").add(99);
        System.out.println("After mutating value list:     " + shallow);

        // ── Deep immutability — wrap the value in an immutable copy too ──
        System.out.println();
        System.out.println("--- Deep immutability fix ---");

        List<Integer> freshNums = new ArrayList<>(List.of(10, 20, 30));
        Map<String, List<Integer>> deep = Map.of("nums", List.copyOf(freshNums));
        System.out.println("Initial:                       " + deep);

        try {
            deep.get("nums").add(99);                           // value list is also immutable now
        } catch (Exception e) {
            System.out.println("Value list is also frozen —    add() threw " + e.getClass().getSimpleName());
        }

        // Mutating the source list does NOT affect the deep map — copyOf took a snapshot
        freshNums.add(99);
        System.out.println("After mutating source list:    " + deep);
    }
}
