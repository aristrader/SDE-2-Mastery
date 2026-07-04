package org.example.backend_fundamentals.java.collections.lists.immutability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Demonstrates that {@link Collections#unmodifiableList(List)} returns a read-only
 * <em>view</em>, not a copy.
 *
 * <p>The view rejects mutation through itself, but it tracks the backing list — if the
 * backing list is mutated externally, the view reflects the change. A common surprise:
 * callers think the list is "frozen" when only the read path through the view is.</p>
 *
 * <p>For a true frozen snapshot, use {@link List#copyOf(java.util.Collection)} —
 * that takes a defensive copy.</p>
 */
public class UnmodifiableListViewRun {

    public static void main(String[] args) {
        List<String> backing = new ArrayList<>();
        backing.add("Hi");
        backing.add("2nd");

        List<String> view = Collections.unmodifiableList(backing);
        System.out.println("View before mutating backing: " + view);

        backing.add("New added");                                            // backing mutated externally
        System.out.println("View after mutating backing:  " + view);         // view reflects it

        try {
            view.add("THIS");                                                // direct mutation rejected
        } catch (Exception e) {
            System.out.println("View rejects direct mutation — add() threw " + e.getClass().getSimpleName());
        }
    }
}
