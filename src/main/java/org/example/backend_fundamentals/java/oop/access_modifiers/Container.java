package org.example.backend_fundamentals.java.oop.access_modifiers;

/**
 * Demonstrates why nested classes used as helpers (Builder, Config, Iterator) must be
 * {@code static}.
 *
 * <p>{@link Config} is {@code static}: no hidden reference to {@code Container}.
 * It can be created independently, stored in a cache, and GC'd freely.</p>
 *
 * <p>{@link Cursor} is non-static (inner class): the compiler silently injects
 * {@code private final Container Container.this} into every instance.
 * As long as any {@code Cursor} is reachable, the enclosing {@code Container}
 * — and its entire {@code data} array — cannot be garbage-collected,
 * even if no other code holds a reference to it.</p>
 *
 * <p>The Builder pattern uses a {@code static} nested Builder for exactly this reason:
 * a builder cached or passed around must not silently retain the product being built.</p>
 */
public class Container {

    private final String[] data;

    public Container(String... data) {
        this.data = data;
    }

    /**
     * Static nested class — no hidden {@code Container} reference.
     * Can be instantiated without a {@code Container} instance.
     */
    public static class Config {
        public final int pageSize;
        public Config(int pageSize) { this.pageSize = pageSize; }
    }

    /**
     * Non-static inner class — holds a hidden {@code Container.this} reference.
     * Accessing {@code data} works, but the enclosing {@code Container} is pinned in memory.
     */
    public class Cursor {
        private int index = 0;
        public boolean hasNext() { return index < data.length; }  // accesses Container.this.data
        public String next()     { return data[index++]; }
    }
}
