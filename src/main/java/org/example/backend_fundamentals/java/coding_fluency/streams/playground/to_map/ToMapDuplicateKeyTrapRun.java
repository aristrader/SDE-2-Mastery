package org.example.backend_fundamentals.java.coding_fluency.streams.playground.to_map;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demonstrates the {@link Collectors#toMap} duplicate-key trap, and the merge-function fix.
 *
 * <p>The two-argument {@code toMap(keyFn, valueFn)} throws {@link IllegalStateException}
 * if two elements produce the same key — a deliberate fail-fast because silent overwrite
 * usually hides a bug. The three-argument overload takes a <em>merge function</em> that
 * decides how to combine values when keys collide: sum them, keep the larger, concatenate
 * them, etc.</p>
 *
 * <p>Always think about whether duplicates are possible in your input before reaching for
 * {@code toMap}. If they are, decide how you want to combine them — and pass the merge
 * function. If you want grouping (one key → many values) instead of merging, that's
 * {@code groupingBy}, not {@code toMap}.</p>
 */
public class ToMapDuplicateKeyTrapRun {

    record Employee(String name, String department, int salary) {}

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Alice",  "Engineering", 100_000),
                new Employee("Bob",    "Engineering", 120_000),  // duplicate department key
                new Employee("Carol",  "Sales",        80_000),
                new Employee("Dave",   "Engineering", 110_000)   // another duplicate
        );

        // ── Without merge function — throws on the first duplicate key ──
        try {
            employees.stream()
                    .collect(Collectors.toMap(Employee::department, Employee::salary));
        } catch (Exception e) {
            System.out.println("toMap without merge: " + e.getClass().getSimpleName());
            System.out.println("  Cause: " + e.getMessage());
        }

        System.out.println();

        // ── With merge function: sum salaries on collision ──
        Map<String, Integer> totalSalaryByDept = employees.stream()
                .collect(Collectors.toMap(
                        Employee::department,
                        Employee::salary,
                        Integer::sum));
        System.out.println("Total salary by dept (merge = sum):   " + totalSalaryByDept);

        // ── With merge function: keep the maximum on collision ──
        Map<String, Integer> maxSalaryByDept = employees.stream()
                .collect(Collectors.toMap(
                        Employee::department,
                        Employee::salary,
                        Math::max));
        System.out.println("Max salary by dept   (merge = max):   " + maxSalaryByDept);

        // ── With merge function: keep the FIRST on collision (rare; usually a bug) ──
        Map<String, Integer> firstSalaryByDept = employees.stream()
                .collect(Collectors.toMap(
                        Employee::department,
                        Employee::salary,
                        (existing, incoming) -> existing));
        System.out.println("First salary by dept (merge = first): " + firstSalaryByDept);
    }
}
