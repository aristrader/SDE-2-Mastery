package org.example.backend_fundamentals.java.generics.playground;

import java.util.ArrayList;
import java.util.List;

/**
 * Topic 1 — Generics basics.
 *
 * <p>Covers three exercises:
 * <ol>
 *   <li>{@link Box} — a generic single-value container (mutable).</li>
 *   <li>{@link Pair} — a generic two-value container (immutable).</li>
 *   <li>{@code main} — demonstrates why raw types are dangerous and how generics fix it.</li>
 * </ol>
 */
public class GenericPractice {

    /**
     * A mutable container for a single value of type {@code T}.
     *
     * <p>The field is intentionally non-final because {@link #setValue} is a
     * legitimate mutating operation — this is a value holder, not a record.
     */
    public static class Box<T> {
        private T value;

        public void setValue(T value) {
            this.value = value;
        }

        public T getValue() {
            return this.value;
        }
    }

    /**
     * An immutable container for two values of potentially different types {@code A} and {@code B}.
     *
     * <p>Fields are {@code final} because neither is ever reassigned after construction —
     * {@link #swap()} returns a new {@code Pair} rather than mutating this one.
     */
    public static class Pair<A, B> {
        private final A value1;
        private final B value2;

        public Pair(A value1, B value2) {
            this.value1 = value1;
            this.value2 = value2;
        }

        public A getFirst() {
            return value1;
        }

        public B getSecond() {
            return value2;
        }

        /**
         * Returns a new {@code Pair} with the two values in reversed order.
         * This instance is unchanged.
         */
        public Pair<B, A> swap() {
            return new Pair<>(value2, value1);
        }
    }

    public static void main(String[] args) {
        // Exercise 3 — raw type bug fix.
        // Without <String>, list.add(5) compiles and crashes at runtime with ClassCastException.
        // With <String>, list.add(5) is a compile error — bug caught immediately, no cast needed.
        List<String> list = new ArrayList<>();
        list.add("hello");
        // list.add(5); // compile error — type safety enforced at compile time
        String s = list.get(0); // no cast needed
        System.out.println(s);

        // Box demo
        Box<String> box = new Box<>();
        box.setValue("hello");
        System.out.println(box.getValue());
        box.setValue("updated");
        System.out.println(box.getValue());

        // Pair demo
        Pair<String, Integer> pair = new Pair<>("age", 30);
        System.out.println(pair.getFirst() + " = " + pair.getSecond());
        Pair<Integer, String> swapped = pair.swap();
        System.out.println(swapped.getFirst() + " = " + swapped.getSecond());
    }
}
