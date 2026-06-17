package org.example.backend_fundamentals.java.foundations.collections.sets.enum_keyed;

import java.util.EnumSet;
import java.util.Set;

/**
 * Demonstrates {@link EnumSet}: a set specialised for enum elements, backed by a single
 * bit-vector ({@code long} for &lt;= 64 enum constants, otherwise an array of longs).
 *
 * <p>When elements are an enum, prefer {@code EnumSet} over {@code HashSet&lt;MyEnum&gt;}.
 * Memory footprint is tiny (1 bit per enum constant) and operations are fast (bitwise
 * AND/OR). Iteration order matches the enum's declaration order.</p>
 *
 * <p>{@code EnumSet} is created via static factory methods, never {@code new}:</p>
 * <ul>
 *   <li>{@link EnumSet#of(Enum, Enum[])} — pick specific elements.</li>
 *   <li>{@link EnumSet#allOf(Class)} — full enum.</li>
 *   <li>{@link EnumSet#noneOf(Class)} — empty.</li>
 *   <li>{@link EnumSet#range(Enum, Enum)} — between two constants in declaration order.</li>
 * </ul>
 */
public class EnumSetBasicsRun {

    enum Weekday { MON, TUE, WED, THU, FRI, SAT, SUN }

    public static void main(String[] args) {
        Set<Weekday> weekend = EnumSet.of(Weekday.SUN, Weekday.SAT);
        Set<Weekday> all = EnumSet.allOf(Weekday.class);
        Set<Weekday> empty = EnumSet.noneOf(Weekday.class);
        Set<Weekday> midweek = EnumSet.range(Weekday.TUE, Weekday.THU);

        System.out.println("Weekend:  " + weekend);
        System.out.println("All:      " + all);
        System.out.println("Empty:    " + empty);
        System.out.println("Midweek:  " + midweek);

        // Insert in deliberately non-declaration order, iterate to confirm declaration order
        EnumSet<Weekday> custom = EnumSet.noneOf(Weekday.class);
        custom.add(Weekday.WED);
        custom.add(Weekday.MON);
        custom.add(Weekday.FRI);
        custom.add(Weekday.SUN);

        System.out.println();
        System.out.println("Inserted in WED, MON, FRI, SUN order.");
        System.out.println("Iterates as (declaration order): " + custom);

        try {
            custom.add(null);
        } catch (Exception e) {
            System.out.println();
            System.out.println("Null rejected: " + e.getClass().getSimpleName());
        }
    }
}
