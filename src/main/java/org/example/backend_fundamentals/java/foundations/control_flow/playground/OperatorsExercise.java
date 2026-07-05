package org.example.backend_fundamentals.java.foundations.control_flow;

public class OperatorsExercise {
    public static void main(String[] args) {
        testShortCircuit();
        System.out.println("Operators exercises passed!");
    }

    public static void testShortCircuit() {
        String s = null;
        
        // TODO: This code throws a NullPointerException because of the bitwise '&'.
        // Change it to use the proper logical short-circuit operator.
        /*
        if (s != null & s.length() > 0) {
            System.out.println("Valid string");
        }
        */
    }
}
