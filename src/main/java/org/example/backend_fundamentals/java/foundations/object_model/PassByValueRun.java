package org.example.backend_fundamentals.java.foundations.object_model;

/**
 * Demonstrates Java's pass-by-value-of-the-reference semantics.
 *
 * <p>Java hands the callee a copy of the pointer, not the original variable.
 * Mutating the object through that pointer is visible to the caller;
 * reassigning the pointer itself is not.
 */
public class PassByValueRun {

    static class Dog {
        private String name;

        Dog(String name) { this.name = name; }

        String getName() { return name; }
        void setName(String name) { this.name = name; }

        @Override public String toString() { return "Dog(" + name + ")"; }
    }

    /** Mutates the object AND tries to reassign the local reference. */
    static void rename(Dog d) {
        System.out.println("  inside rename() — received: " + d);

        d.setName("Max");                        // mutation — goes through to caller's object
        System.out.println("  after setName()  — d is: " + d);

        d = new Dog("Completely Different Dog"); // reassignment — only affects local copy
        System.out.println("  after d = new Dog() — d is: " + d + "  (local copy only)");
    }

    public static void main(String[] args) {
        Dog dog = new Dog("Rex");
        System.out.println("before rename() — dog is: " + dog);

        rename(dog);

        System.out.println("after  rename() — dog is: " + dog);
        System.out.println();
        System.out.println("Conclusion:");
        System.out.println("  setName() change IS visible  — same heap object was mutated");
        System.out.println("  new Dog() change is NOT visible — caller's variable still points to original");
    }
}
