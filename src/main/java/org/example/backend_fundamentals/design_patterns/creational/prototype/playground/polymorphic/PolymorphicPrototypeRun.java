package org.example.backend_fundamentals.design_patterns.creational.prototype.playground.polymorphic;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo runner showing polymorphic prototype cloning.
 *
 * <p>The client holds a {@code List<Shape>} and calls {@code shape.clone()} in a loop. It never
 * checks {@code instanceof}, never casts, and never names {@link Circle} or {@link Rectangle}.
 * The JVM dispatches to the correct concrete {@code clone()} at runtime — that is the
 * polymorphism this package demonstrates.
 */
public class PolymorphicPrototypeRun {

  public static void main(String[] args) {
    List<Shape> originals = new ArrayList<>();
    originals.add(new Circle(10, 10, "red", 20));
    originals.add(new Rectangle(5, 5, "blue", 30, 15));

    // Clone entire list — caller only knows Shape
    List<Shape> clones = new ArrayList<>();
    for (Shape shape : originals) {
      clones.add(shape.clone()); // polymorphic dispatch: Circle or Rectangle clone() at runtime
    }

    System.out.println("=== Originals ===");
    originals.forEach(System.out::println);

    System.out.println("\n=== Clones ===");
    clones.forEach(System.out::println);

    System.out.println("\n=== Same object reference? ===");
    for (int i = 0; i < originals.size(); i++) {
      boolean sameRef = originals.get(i) == clones.get(i);
      System.out.println(originals.get(i).getClass().getSimpleName()
          + ": " + (sameRef ? "SAME — broken clone" : "DIFFERENT — correct"));
    }
  }
}
