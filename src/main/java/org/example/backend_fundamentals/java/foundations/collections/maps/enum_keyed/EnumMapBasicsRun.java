package org.example.backend_fundamentals.java.foundations.collections.maps.enum_keyed;

import java.util.EnumMap;
import java.util.Map;

/**
 * Demonstrates {@link EnumMap}: enum-keyed map with a small bit-vector backing.
 *
 * <p>When keys are an enum, prefer {@code EnumMap} over {@code HashMap<MyEnum, V>}.
 * It is dramatically faster (no hashing — keys index directly into an array sized to
 * the enum's declared length) and uses far less memory. Iteration order matches the
 * enum's declaration order.</p>
 *
 * <p>Constraints: requires the {@code Class} object at construction; null keys throw.</p>
 */
public class EnumMapBasicsRun {

    enum Weekday { MON, TUE, WED, THU, FRI, SAT, SUN }

    public static void main(String[] args) {
        // Constructor takes the Class — EnumMap needs to know the enum's size up front
        Map<Weekday, String> schedule = new EnumMap<>(Weekday.class);

        // Insert in deliberately non-declaration order
        schedule.put(Weekday.WED, "yoga");
        schedule.put(Weekday.MON, "gym");
        schedule.put(Weekday.FRI, "rest");
        schedule.put(Weekday.SUN, "long run");

        System.out.println("EnumMap iteration (enum declaration order):");
        schedule.forEach((k, v) -> System.out.println("  " + k + " -> " + v));

        try {
            schedule.put(null, "anything");
        } catch (Exception e) {
            System.out.println();
            System.out.println("Null key rejected: " + e.getClass().getSimpleName());
        }
    }
}
