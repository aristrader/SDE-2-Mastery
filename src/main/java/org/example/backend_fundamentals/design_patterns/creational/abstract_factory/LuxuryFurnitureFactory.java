package org.example.backend_fundamentals.design_patterns.creational.abstract_factory;

/** Concrete factory producing the luxury furniture family. Stateless. */
public class LuxuryFurnitureFactory implements FurnitureSetFactory {

  @Override
  public Chair createChair() {
    return new LuxuryChair();
  }

  @Override
  public Sofa createSofa() {
    return new LuxurySofa();
  }
}
