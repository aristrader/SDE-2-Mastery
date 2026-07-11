package org.example.backend_fundamentals.java.foundations.string.pool.playground;

public class StringExercise {
    public static void main(String[] args) {
        testStringPool();
        System.out.println("String exercises passed!");
    }

    public static void testStringPool() {
        String s1 = "Hello";
        String s2 = "Hello";
        String s3 = new String("Hello");

        // TODO: Fix these assertions based on String pool rules.
        // Use == only when references are identical. Use .equals() for content.
        // assert s1 == s2;
        // assert s1 == s3;
    }
}
