package org.example.backend_fundamentals.java.oop.pillars.inheritance.playground;

/**
 * The two faces of {@code super.method()}.
 * <p>
 * 1. It calls the immediate parent's method, useful when you want to <em>extend</em>
 *    rather than replace parent behaviour (e.g., a child's {@code toString()} that
 *    wraps the parent's representation).
 * <p>
 * 2. It is <em>not</em> polymorphic. The compiler emits {@code invokespecial} with
 *    the parent class hardcoded — there is no vtable walk. So when you write
 *    {@code super.describe()} below, control jumps directly to {@code Parent.describe()}.
 *    But calls <em>inside</em> that parent code (like {@code name()}) are still
 *    {@code invokevirtual} and dispatch back to the child's overrides — which is
 *    almost always surprising the first time you see it.
 * <p>
 * Also: there is no {@code super.super.method()} in Java. You can only reach one
 * level up the chain.
 */
public class SuperMethodDelegation {

  static class Parent {
    public String describe() { return "I am " + name(); }
    public String name()     { return "Parent"; }
  }

  static class Child extends Parent {
    @Override
    public String name() { return "Child"; }

    @Override
    public String describe() {
      // super.describe() runs Parent.describe() (invokespecial — non-polymorphic).
      // Inside Parent.describe(), the call to name() is invokevirtual — it finds
      // Child.name() because `this` is actually a Child.
      return "[" + super.describe() + "]";
    }
  }

  public static void main(String[] args) {
    Child c = new Child();
    System.out.println("c.describe() = " + c.describe());
    // Naive expectation: "[I am Parent]" (super means parent, right?)
    // Actual:            "[I am Child]"
    //   super.describe()  → Parent.describe() (fixed to Parent class)
    //     inside it, name() → Child.name()    (still polymorphic)
  }
}
