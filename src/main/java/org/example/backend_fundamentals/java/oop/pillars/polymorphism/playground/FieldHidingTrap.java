package org.example.backend_fundamentals.java.oop.pillars.polymorphism.playground;

/**
 * Polymorphism doesn't apply to fields — fields are hidden, not overridden.
 * <p>
 * A {@code Child} instance carries both {@code label} fields in memory; the
 * reference type at the call site decides which one you read. The matching
 * getter is an instance method, so it dispatches polymorphically — which is
 * the strongest reason fields should be {@code private} and accessed through
 * getters instead of read directly.
 */
public class FieldHidingTrap {

  static class Parent {
    String label = "parent";

    String getLabel() { return label; }
  }

  static class Child extends Parent {
    String label = "child";

    @Override
    String getLabel() { return label; }
  }

  public static void main(String[] args) {
    Parent p = new Child();
    System.out.println("p.label            = " + p.label);             // "parent" — field, declared type wins
    System.out.println("((Child) p).label  = " + ((Child) p).label);   // "child"
    System.out.println("p.getLabel()       = " + p.getLabel());        // "child" — instance method dispatches
  }
}
