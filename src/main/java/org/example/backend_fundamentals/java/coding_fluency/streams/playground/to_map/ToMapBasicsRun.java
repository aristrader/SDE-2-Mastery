package org.example.backend_fundamentals.java.coding_fluency.streams.playground.to_map;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Demonstrates basic {@link Collectors#toMap(java.util.function.Function, java.util.function.Function)}
 * usage — building a map by extracting a key and a value from each stream element.
 *
 * <p>{@code toMap(keyFn, valueFn)} requires that <strong>keys are unique</strong>. Duplicate
 * keys throw {@code IllegalStateException}. For inputs that may produce duplicates,
 * use the three-argument overload with a merge function (see {@code ToMapDuplicateKeyTrapRun}).</p>
 *
 * <p>{@link Function#identity()} is the idiomatic way to keep the whole element as the value.</p>
 */
public class ToMapBasicsRun {

    record Employee(String name, String department, int salary) {}

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Alice",  "Engineering", 100_000),
                new Employee("Bob",    "Engineering", 120_000),
                new Employee("Carol",  "Sales",        80_000)
        );

        // toMap with a key extractor + a value extractor
        Map<String, Integer> salaryByName = employees.stream()
                .collect(Collectors.toMap(Employee::name, Employee::salary));
        System.out.println("Salary by name:   " + salaryByName);

        // toMap with Function.identity() to keep the whole element as value
        Map<String, Employee> employeeByName = employees.stream()
                .collect(Collectors.toMap(Employee::name, Function.identity()));
        System.out.println("Employee by name: " + employeeByName);
    }
}
