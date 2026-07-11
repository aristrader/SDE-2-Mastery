package org.example.backend_fundamentals.java.oop.pillars.polymorphism.playground;

/**
 * Polymorphism that doesn't happen — static methods are statically dispatched.
 * <p>
 * Despite {@code Parent p = new Child()}, calling {@code p.greet()} prints
 * "Parent". The compiler emits {@code invokestatic Parent.greet()} based on
 * the declared type; the runtime object is never consulted. Smoking-gun proof:
 * a {@code null} reference also calls "Parent" with no NPE — the reference is
 * never dereferenced.
 */
public class StaticHidingTrap {

  static class Parent {
    static String greet() { return "Parent"; }
  }

  static class Child extends Parent {
    // @Override would not compile here — there is no override relationship for static methods.
    static String greet() { return "Child"; }
  }

  public static void main(String[] args) {
    Parent p = new Child();
    System.out.println("Parent ref to Child instance: p.greet() = " + p.greet());
    // Expected: "Parent" — declared type wins.

    System.out.println("Parent ref to Child instance cast to child: p.greet() = " + ((Child) p).greet());

    Parent nullRef = null;
    System.out.println("Null Parent ref:              nullRef.greet() = " + nullRef.greet());
    // Expected: "Parent" — no NPE, the reference is never dereferenced.

    // Direct calls work as expected because they name the class explicitly:
    System.out.println("Direct Parent.greet() = " + Parent.greet());
    System.out.println("Direct Child.greet()  = " + Child.greet());
  }
}
