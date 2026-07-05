package org.example.backend_fundamentals.java.foundations.primitives;

public class PrimitivesExercise {
    public static void main(String[] args) {
        testIntegerCache();
        testAutoboxingTrap();
        System.out.println("Primitives exercises passed!");
    }

    public static void testIntegerCache() {
        Integer a = 100;
        Integer b = 100;
        Integer x = 200;
        Integer y = 200;

        // TODO: Which of these should use == and which should use .equals()?
        // Fix the assertions to pass successfully without throwing an Error.
        // assert a == b;
        // assert x == y; 
    }

    public static void testAutoboxingTrap() {
        Integer wrapper = null;
        
        // TODO: Fix this code so it doesn't throw a NullPointerException.
        // Hint: Avoid auto-unboxing a null reference.
        // int primitive = wrapper;
        // assert primitive == 0;
    }
}
