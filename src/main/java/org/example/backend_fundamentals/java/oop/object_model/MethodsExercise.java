package org.example.backend_fundamentals.java.oop.object_model;

public class MethodsExercise {
    public static void main(String[] args) {
        testPassByValue();
        testDowncasting();
        System.out.println("Methods exercises passed!");
    }

    static class Person {
        String name;
        public Person(String name) { this.name = name; }
    }

    private static void modifyPerson(Person p) {
        // TODO: This method is supposed to change the caller's person's name to "Alice".
        // Fix the code so it works, keeping in mind Java is pass-by-value.
        p = new Person("Alice"); 
    }

    public static void testPassByValue() {
        Person p = new Person("Bob");
        modifyPerson(p);
        
        // Uncomment the assertion below and make sure it passes.
        // assert p.name.equals("Alice") : "Pass by value trap! You reassigned the reference.";
    }

    static class Animal {}
    static class Dog extends Animal {
        void bark() { System.out.println("Woof"); }
    }

    public static void testDowncasting() {
        Animal animal = new Animal();
        
        // TODO: Safely attempt to cast 'animal' to a Dog without throwing ClassCastException.
        // Use instanceof to protect the cast.
        // Dog dog = (Dog) animal;
        // dog.bark();
    }
}
