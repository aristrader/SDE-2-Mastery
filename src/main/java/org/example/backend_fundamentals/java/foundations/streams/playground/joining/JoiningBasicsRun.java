package org.example.backend_fundamentals.java.foundations.streams.playground.joining;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Demonstrates {@link Collectors#joining()} — concatenating a stream of strings with
 * an optional delimiter, prefix, and suffix.
 *
 * <p>{@code joining} replaces the manual {@code StringBuilder} loop with first-flag handling
 * for separators. Three overloads:</p>
 *
 * <ul>
 *   <li>{@code joining()} — no delimiter, just concatenation.</li>
 *   <li>{@code joining(delimiter)} — items separated by {@code delimiter}.</li>
 *   <li>{@code joining(delimiter, prefix, suffix)} — also wrapped in {@code prefix} / {@code suffix}.</li>
 * </ul>
 *
 * <p>The third overload always emits the prefix and suffix even on an empty stream
 * ({@code "[]"} for {@code joining(", ", "[", "]")} with zero elements).</p>
 */
public class JoiningBasicsRun {

    public static void main(String[] args) {
        List<String> fruits = List.of("apple", "banana", "cherry");

        // Plain concatenation
        String concat = fruits.stream().collect(Collectors.joining());
        System.out.println("joining():                  " + concat);

        // With delimiter
        String csv = fruits.stream().collect(Collectors.joining(", "));
        System.out.println("joining(\", \"):              " + csv);

        // With delimiter, prefix, suffix
        String wrapped = fruits.stream().collect(Collectors.joining(", ", "[", "]"));
        System.out.println("joining(\", \", \"[\", \"]\"):    " + wrapped);

        // Empty stream — prefix + suffix still emitted
        String empty = Stream.<String>empty().collect(Collectors.joining(", ", "[", "]"));
        System.out.println("joining over empty stream:  " + empty);

        // For comparison — manual StringBuilder version
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String s : fruits) {
            if (!first) sb.append(", ");
            sb.append(s);
            first = false;
        }
        sb.append("]");
        System.out.println();
        System.out.println("Manual StringBuilder:       " + sb);
    }
}
