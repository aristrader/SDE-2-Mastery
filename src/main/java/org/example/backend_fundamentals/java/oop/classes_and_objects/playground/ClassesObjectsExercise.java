package org.example.backend_fundamentals.java.oop.classes_and_objects.playground;

import java.util.ArrayList;
import java.util.List;

public class ClassesObjectsExercise {

    // ---------------------------------------------------------
    // EXERCISE 1: Constructors & Constructor Chaining
    // ---------------------------------------------------------
    static class User {
        String username;
        int age;
        boolean isActive;

        // TODO: Create a no-argument constructor that chains to the 
        // 3-argument constructor, passing "guest", 18, and false.

        // TODO: Create a 3-argument constructor to initialize all fields.
    }

    // ---------------------------------------------------------
    // EXERCISE 2: Static vs Instance
    // ---------------------------------------------------------
    static class Counter {
        int instanceCount = 0;
        static int globalCount = 0;

        Counter() {
            // TODO: Increment both instanceCount and globalCount
        }
    }

    // ---------------------------------------------------------
    // EXERCISE 3: Final vs Immutability
    // ---------------------------------------------------------
    public static void testFinalReference() {
        final List<String> items = new ArrayList<>();
        
        // TODO: Add an item to the 'items' list to prove that final 
        // objects can still be mutated.
        
        // Uncommenting the below line should cause a compile error.
        // items = new ArrayList<>(); 
    }

    // ---------------------------------------------------------
    // EXERCISE 4: Static Nested Class
    // ---------------------------------------------------------
    static class DatabaseConnection {
        private String url;

        // TODO: Create a static nested class named 'Builder'
        // It should have a method url(String url) that sets the builder's url,
        // and a build() method that returns a new DatabaseConnection instance.
    }

    public static void main(String[] args) {
        // Test Exercise 2
        Counter c1 = new Counter();
        Counter c2 = new Counter();
        
        System.out.println("c1 instance count: " + c1.instanceCount); // Should be 1
        System.out.println("Global count: " + Counter.globalCount);   // Should be 2

        // Test Exercise 3
        testFinalReference();

        // Test Exercise 4
        // DatabaseConnection.Builder builder = new DatabaseConnection.Builder();
        // DatabaseConnection conn = builder.url("jdbc:mysql://localhost:3306").build();
    }
}
