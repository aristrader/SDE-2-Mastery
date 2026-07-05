package org.example.backend_fundamentals.java.foundations.generics.playground;

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * Topic 1 — Exercise 2: a generic LIFO stack backed by an {@link ArrayList}.
 *
 * <p>Key decisions:
 * <ul>
 *   <li>Backed by {@code ArrayList} (not array) to avoid unchecked array creation with generics.</li>
 *   <li>{@link #pop()} throws {@link EmptyStackException} rather than returning {@code null} —
 *       null would be ambiguous if {@code null} values are ever pushed onto the stack.</li>
 * </ul>
 */
public class GenericStack<T> {
    private final ArrayList<T> list = new ArrayList<>();

    /** Pushes {@code value} onto the top of the stack. */
    public void push(T value) {
        list.add(value);
    }

    /**
     * Removes and returns the top element.
     *
     * @throws EmptyStackException if the stack is empty
     */
    public T pop() {
        if (!list.isEmpty()) {
            T value = list.get(list.size() - 1);
            list.remove(list.size() - 1);
            return value;
        }
        throw new EmptyStackException();
    }

    /** Returns the top element without removing it, or {@code null} if the stack is empty. */
    public T peek() {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public static void main(String[] a) {
        GenericStack<String> s = new GenericStack<>();
        System.out.println("empty? " + s.isEmpty());  // true
        System.out.println("peek:  " + s.peek());      // null
        s.push("ABC");
        System.out.println("empty? " + s.isEmpty());  // false
        System.out.println("peek:  " + s.peek());      // ABC
        System.out.println("pop:   " + s.pop());       // ABC
        System.out.println("empty? " + s.isEmpty());  // true
        System.out.println("peek:  " + s.peek());      // null
    }
}
