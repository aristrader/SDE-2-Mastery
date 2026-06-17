package org.example.backend_fundamentals.java.foundations.generics;

import java.util.Comparator;
import java.util.List;

/**
 * Topic 2 — Bounds ({@code extends}).
 *
 * <p>Bounds constrain what type {@code T} can be, allowing you to call methods
 * from the bound type. Without bounds, {@code T} is just {@code Object}.
 *
 * <p>Exercises:
 * <ol>
 *   <li>{@link #max} / {@link #findMax} / {@link #findMaxStream} — requires {@code Comparable} to compare.</li>
 *   <li>{@link #sum} / {@link #sumStream} — requires {@code Number} to call {@code doubleValue()}.</li>
 *   <li>Exercise 3 (spot the issue) — demonstrated in {@code main}: {@code String} fails
 *       the {@code extends Number} bound at compile time.</li>
 * </ol>
 */
public class BoundsPractice {

    /**
     * Returns the larger of two values.
     *
     * <p>Both {@code Number} and {@code Comparable<T>} are required — {@code Number} for
     * consistency with the numeric methods below, {@code Comparable<T>} to call {@code compareTo()}.
     */
    public static <T extends Number & Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) > 0 ? a : b;
    }

    /**
     * Returns the largest element in the list using a loop.
     * Reuses {@link #max} to avoid duplicating comparison logic.
     */
    public static <T extends Number & Comparable<T>> T findMax(List<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        T max = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            max = max(max, list.get(i));
        }
        return max;
    }

    /**
     * Returns the largest element using {@code Stream.max()}.
     *
     * <p>{@code Comparator.naturalOrder()} works because {@code T extends Comparable<T>}.
     * {@code orElse(null)} is used instead of {@code get()} — safer and suppresses the
     * linter warning on Optional.
     */
    public static <T extends Number & Comparable<T>> T findMaxStream(List<T> list) {
        if (list.isEmpty()) {
            return null;
        }
        return list.stream().max(Comparator.naturalOrder()).orElse(null);
    }

    /**
     * Returns the sum of all elements as a {@code double} using a loop.
     *
     * <p>{@code T extends Number} is required to call {@code doubleValue()}.
     * The {@code +} operator does not work on generic types — arithmetic requires
     * conversion to a primitive first.
     */
    public static <T extends Number> double sum(List<T> list) {
        double sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum = sum + list.get(i).doubleValue();
        }
        return sum;
    }

    /**
     * Returns the sum using {@code mapToDouble}.
     *
     * <p>{@code stream()} gives a {@code Stream<T>} (object stream — no numeric ops).
     * {@code mapToDouble(Number::doubleValue)} converts it to a primitive {@code DoubleStream},
     * which unlocks {@code .sum()}.
     */
    public static <T extends Number> double sumStream(List<T> list) {
        return list.stream().mapToDouble(Number::doubleValue).sum();
    }

    // -------------------------------------------------------------------------
    // Exercise 3 — Spot the issue (solution)
    //
    // WRONG version:
    //   public <T> T findMax(List<T> list) {
    //       T max = list.get(0);
    //       for (T item : list) {
    //           if (item > max) { ... }  // 1. > doesn't work on objects — primitives only
    //       }                            // 2. no bound — T is just Object, compareTo() unavailable
    //   }
    //
    // Fix 1: bound T with Comparable<T> so compareTo() is available
    // Fix 2: replace > with compareTo() > 0
    //
    // CORRECT version: findMax() above — <T extends Comparable<T>> + compareTo()
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        List<Integer> ints = List.of(1, 2, 30);

        // Exercise 3 — String fails the Number bound at compile time:
        // List<String> strings = List.of("a", "b");
        // findMax(strings); // compile error — String does not extend Number

        System.out.println("findMax (loop):   " + findMax(ints));       // 30
        System.out.println("findMax (stream): " + findMaxStream(ints)); // 30
        System.out.println("sum (loop):       " + sum(ints));           // 33.0
        System.out.println("sum (stream):     " + sumStream(ints));     // 33.0
    }
}
