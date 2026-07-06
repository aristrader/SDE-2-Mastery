package org.example.backend_fundamentals.java.nested_classes;

/**
 * ### Java Static Nested Class vs Inner Class
 *
 * #### 1. Static Nested Class
 *
 * ```java
 * static class A {}
 * ```
 *
 * * Does **not** depend on outer class instance
 * * Can be created directly:
 *
 *   ```java
 *   A obj = new A();
 *   ```
 * * Can have static members
 * * Acts like a normal class, just grouped inside another
 *
 * **Use when:** no need to access outer class instance
 *
 * ---
 *
 * #### 2. Non-Static Inner Class
 *
 * ```java
 * class B {}
 * ```
 *
 * * **Depends on outer class instance**
 * * Must be created like:
 *
 *   ```java
 *   Outer outer = new Outer();
 *   Outer.B obj = outer.new B();
 *   ```
 * * Can access outer class methods/variables directly
 *
 * **Use when:** tightly coupled with outer object
 *
 * ---
 *
 * #### 3. Static Context Rule
 *
 * * `main()` is static
 * * Cannot directly use non-static inner class inside it
 * * Must create outer object first
 *
 * ---
 *
 * #### 4. Key Difference
 *
 * | Feature                 | Static Nested | Inner Class |
 * | ----------------------- | ------------- | ----------- |
 * | Needs outer instance    | No            | Yes         |
 * | Works in static methods | Yes           | No          |
 * | Access outer members    | Only static   | All members |
 *
 * ---
 *
 * #### 5. One-line Memory Trick
 *
 * * Static nested → **independent**
 * * Inner class → **attached to outer object**
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