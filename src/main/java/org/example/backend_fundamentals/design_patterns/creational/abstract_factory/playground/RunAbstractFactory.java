package org.example.backend_fundamentals.design_patterns.creational.abstract_factory.playground;

/**
 * Demo runner for Abstract Factory. Drives both furniture families through
 * the same {@link #furnishRoom(FurnitureSetFactory)} helper to make the
 * pattern's headline visible: identical client code, different product
 * families, no concrete product class named at the call site.
 */
public class RunAbstractFactory {

  public static void main(String[] args) {
    System.out.println("=== Cheap family ===");
    furnishRoom(new CheapFurnitureFactory());

    System.out.println("=== Luxury family ===");
    furnishRoom(new LuxuryFurnitureFactory());
  }

  /**
   * Client-side helper that consumes whatever family the factory belongs to.
   * Lives here, not on {@link FurnitureSetFactory}, because consumption is
   * the caller's responsibility &mdash; not the factory's.
   */
  private static void furnishRoom(FurnitureSetFactory factory) {
    Chair chair = factory.createChair();
    Sofa sofa = factory.createSofa();
    System.out.println("Chair: " + chair);
    System.out.println("Sofa:  " + sofa);
  }
}
