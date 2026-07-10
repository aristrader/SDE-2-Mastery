package org.example.backend_fundamentals.java.coding_fluency.streams.playground.grouping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demonstrates {@link Collectors#groupingBy} with downstream collectors.
 *
 * <p>The two-arg form of {@code groupingBy} takes a second collector that runs against
 * each group instead of just collecting them into a list. This is how you produce
 * counts per group, sums per group, mapped lists per group, averages per group, etc.,
 * all in one stream pass.</p>
 *
 * <p>Common downstream collectors:</p>
 * <ul>
 *   <li>{@link Collectors#counting()} — count entries per group</li>
 *   <li>{@link Collectors#summingInt} / {@code summingLong} / {@code summingDouble} — sum a numeric field</li>
 *   <li>{@link Collectors#averagingInt} / {@code averagingLong} / {@code averagingDouble} — average</li>
 *   <li>{@link Collectors#mapping} — extract / transform per element before collecting downstream</li>
 *   <li>{@link Collectors#toSet} — dedupe per group</li>
 * </ul>
 */
public class GroupingByDownstreamRun {

    record Employee(String name, String department, int salary) {}

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Alice",  "Engineering", 100_000),
                new Employee("Bob",    "Engineering", 120_000),
                new Employee("Carol",  "Sales",        80_000),
                new Employee("Dave",   "Engineering", 110_000),
                new Employee("Eve",    "Sales",        90_000),
                new Employee("Frank",  "Marketing",    95_000)
        );

        // Count per department
        Map<String, Long> countByDept = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::department,
                        Collectors.counting()));
        System.out.println("Count per dept:    " + countByDept);

        // Total salary per department
        Map<String, Integer> totalSalary = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::department,
                        Collectors.summingInt(Employee::salary)));
        System.out.println("Total salary:      " + totalSalary);

        // Average salary per department
        Map<String, Double> avgSalary = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::department,
                        Collectors.averagingInt(Employee::salary)));
        System.out.println("Avg salary:        " + avgSalary);

        // Names per department (extract a field per element with mapping(...))
        Map<String, List<String>> namesByDept = employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::department,
                        Collectors.mapping(Employee::name, Collectors.toList())));
        System.out.println("Names per dept:    " + namesByDept);
    }
}
