package org.example.backend_fundamentals.java.coding_fluency.streams.playground.grouping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demonstrates {@link Collectors#groupingBy(java.util.function.Function)} replacing
 * the manual {@code computeIfAbsent} loop.
 *
 * <p>{@code groupingBy} is the most-used non-trivial collector. It takes a classifier
 * function and produces a {@code Map<K, List<V>>} — entries with the same classifier
 * value end up in the same list. Without it, you'd write a manual {@code HashMap} +
 * {@code computeIfAbsent} loop; this demo shows the two side by side.</p>
 */
public class GroupingByBasicsRun {

    record Employee(String name, String department) {}

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Alice",  "Engineering"),
                new Employee("Bob",    "Engineering"),
                new Employee("Carol",  "Sales"),
                new Employee("Dave",   "Engineering"),
                new Employee("Eve",    "Sales"),
                new Employee("Frank",  "Marketing")
        );

        // ── The "before" — manual loop with computeIfAbsent ──
        Map<String, List<Employee>> manual = new HashMap<>();
        for (Employee e : employees) {
            manual.computeIfAbsent(e.department(), k -> new ArrayList<>()).add(e);
        }
        System.out.println("Manual loop (computeIfAbsent):");
        manual.forEach((dept, emps) -> System.out.println("  " + dept + " -> " + emps));

        // ── The "after" — one line with groupingBy ──
        Map<String, List<Employee>> grouped = employees.stream()
                .collect(Collectors.groupingBy(Employee::department));
        System.out.println();
        System.out.println("groupingBy(Employee::department):");
        grouped.forEach((dept, emps) -> System.out.println("  " + dept + " -> " + emps));
    }
}
