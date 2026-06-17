package org.example.backend_fundamentals.java.foundations.collections.lists.basics;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic {@link ArrayList} usage — declare, add, print.
 *
 * <p>{@link ArrayList} is the default mutable list in Java: dynamic size, O(1) random access,
 * O(1) amortised append at the end. The variable is typed as {@link List} (the abstraction),
 * not {@code ArrayList} — DIP in practice.</p>
 */
public class ArrayListBasicsRun {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("Hi");
        list.add("2nd");
        System.out.println(list);
    }
}
