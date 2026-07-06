package org.example.backend_fundamentals.java.oop.nested_classes;

/**
 * Demonstrates static nested class creation, inner class creation, and the outer-instance rule.
 */
public class NestedClassDemo {

  public static void main(String[] args) {

    // =========================
    // 1. STATIC NESTED CLASS
    // =========================
    StaticNested s1 = new StaticNested(5);
    System.out.println("StaticNested a = " + s1.a);

    // Static method call (no object needed)
    StaticNested.printHello();


    // =========================
    // 2. NON-STATIC INNER CLASS
    // =========================
    NestedClassDemo outer = new NestedClassDemo();        // step 1: create outer
    Inner inner = outer.new Inner(10);                    // step 2: create inner

    System.out.println("Inner a = " + inner.a);

    inner.printOuterMessage(); // accessing outer instance data
  }


  // =====================================
  // STATIC NESTED CLASS
  // =====================================
  public static class StaticNested {
    int a;

    StaticNested(int value) {
      this.a = value;
    }

    static void printHello() {
      System.out.println("Hello from StaticNested");
    }
  }


  // =====================================
  // NON-STATIC INNER CLASS
  // =====================================
  public class Inner {
    int a;

    Inner(int value) {
      this.a = value;
    }

    void printOuterMessage() {
      System.out.println("Accessing outer instance method:");
      outerMethod(); // can directly access outer methods
    }
  }


  // Outer class method
  void outerMethod() {
    System.out.println("Hello from Outer class");
  }
}
