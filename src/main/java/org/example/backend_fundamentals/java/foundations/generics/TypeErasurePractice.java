package org.example.backend_fundamentals.java.foundations.generics;

import java.util.ArrayList;
import java.util.List;

public class TypeErasurePractice {

    // Exercise 1 — instanceof trap
    // list instanceof List<Integer> is a COMPILE ERROR — type info is gone at runtime.
    // The correct check is against the raw type only.
    public static boolean isAList(Object obj) {
        return obj instanceof List;   // raw type check — all you can do at runtime
        // obj instanceof List<Integer>  // COMPILE ERROR
    }

    // Exercise 2 — same erasure overload
    // Both erase to process(List) — the JVM sees duplicate signatures.
    // Fix: use different method names.
    // void process(List<Integer> list) { }   // COMPILE ERROR if both present
    // void process(List<String> list) { }
    public static void processIntegers(List<Integer> list) {
        System.out.println("integers: " + list);
    }

    public static void processStrings(List<String> list) {
        System.out.println("strings: " + list);
    }

    // Exercise 3 — .getClass() proves both lists are the same class at runtime
    public static void classDemo() {
        List<Integer> ints = new ArrayList<>();
        List<String> strs = new ArrayList<>();
        System.out.println(ints.getClass());                    // class java.util.ArrayList
        System.out.println(strs.getClass());                    // class java.util.ArrayList
        System.out.println(ints.getClass() == strs.getClass()); // true — erasure made them identical
    }

    // Exercise 4 — unchecked cast warning
    // The compiler warns but cannot error — it has no way to verify the cast is safe at runtime
    // because the generic parameter was erased. If the object is actually a List<Integer>,
    // the cast appears to succeed here but throws ClassCastException later when an element is read.
    @SuppressWarnings("unchecked")
    public static List<String> unsafeCast(Object obj) {
        return (List<String>) obj;  // unchecked warning without @SuppressWarnings
    }

    public static void main(String[] args) {
        // Exercise 1
        System.out.println(isAList(new ArrayList<String>()));  // true
        System.out.println(isAList("hello"));                  // false

        // Exercise 2
        processIntegers(List.of(1, 2, 3));
        processStrings(List.of("a", "b"));

        // Exercise 3
        classDemo();

        // Exercise 4 — the cast succeeds here, but reading an element as String would blow up
        List<Integer> ints = new ArrayList<>();
        ints.add(42);
        List<String> strings = unsafeCast(ints);
        System.out.println(strings);          // prints [42] — no error yet
        // System.out.println(strings.get(0)); // ClassCastException at runtime — compiler inserted cast
    }
}
