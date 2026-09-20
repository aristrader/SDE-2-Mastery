package org.example.backend_fundamentals.java.foundations.wrapper_semantics.playground;

import java.util.HashMap;
import java.util.Map;

public class WrapperSemanticsExercise {
    public static void main(String[] args) {
        testIntegerCache();
        testAutoboxingTrap();
        System.out.println("Wrapper semantics exercises passed!");
    }

    public static void testIntegerCache() {
        Integer a = 50;
        Integer b = 50;
        Integer x = 500;
        Integer y = 500;

        assert a == b : "-128..127 boxing identity is guaranteed";
        assert x.equals(y) : "equals compares wrapper values";
        System.out.println("500 identity is not a value contract: " + (x == y));
    }

    public static void testAutoboxingTrap() {
        Map<String, Integer> counts = new HashMap<>();
        try {
            int ignored = counts.get("missing_key");
            throw new AssertionError("Expected null unboxing to fail: " + ignored);
        } catch (NullPointerException expected) {
            System.out.println("Missing value unboxing throws NPE");
        }
        int count = counts.getOrDefault("missing_key", 0);
        assert count == 0;
    }
}
