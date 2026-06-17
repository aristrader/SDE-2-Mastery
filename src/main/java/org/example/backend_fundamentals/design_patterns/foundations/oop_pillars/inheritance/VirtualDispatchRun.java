package org.example.backend_fundamentals.design_patterns.foundations.oop_pillars.inheritance;

/**
 * Shows that the JVM always starts method lookup from the actual runtime type
 * of the object (the top of the hierarchy), regardless of whose code is
 * currently executing.
 *
 * <p>Key rule: {@code super.method()} only means "skip my override and start
 * lookup from the parent class." It does NOT change what {@code this} is.
 * So any {@code this.foo()} call inside the parent's code still dispatches
 * back to the child's override if one exists.
 */
public class VirtualDispatchRun {

    static class Parent {
        void greet() {
            System.out.println("  [Parent.greet] running — now calling this.describe()");
            this.describe(); // 'this' is whatever the runtime object is — NOT always Parent
        }

        void describe() {
            System.out.println("  [Parent.describe] I am a Parent");
        }
    }

    static class Child extends Parent {
        @Override
        void describe() {
            System.out.println("  [Child.describe] I am a Child");
        }
    }

    static class GrandChild extends Child {
        @Override
        void describe() {
            System.out.println("  [GrandChild.describe] I am a GrandChild");
        }

        void runSuperGreet() {
            System.out.println("  [GrandChild] calling super.greet() ...");
            super.greet(); // runs Parent.greet(), but 'this' is still GrandChild
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Case 1: Parent reference, Parent object ===");
        Parent p = new Parent();
        p.greet();
        // greet() calls this.describe() → Parent.describe() — straightforward

        System.out.println();
        System.out.println("=== Case 2: Parent reference, Child object ===");
        Parent c = new Child();
        c.greet();
        // greet() runs from Parent (declared type of reference)
        // but this.describe() dispatches to Child.describe() — runtime type wins

        System.out.println();
        System.out.println("=== Case 3: GrandChild calls super.greet() ===");
        GrandChild gc = new GrandChild();
        gc.runSuperGreet();
        // super.greet() runs Parent.greet()
        // but 'this' is still the GrandChild instance
        // so this.describe() dispatches to GrandChild.describe() — top of hierarchy wins

        System.out.println();
        System.out.println("Rule: lookup ALWAYS starts from the runtime type (the top).");
        System.out.println("      super only skips one layer; it never freezes 'this'.");
    }
}
