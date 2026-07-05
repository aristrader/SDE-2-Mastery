package org.example.backend_fundamentals.design_patterns.creational.abstract_factory.playground;

/** Concrete factory producing the cheap furniture family. Stateless. */
public class CheapFurnitureFactory implements FurnitureSetFactory {

  @Override
  public Chair createChair() {
    return new CheapChair();
  }

  @Override
  public Sofa createSofa() {
    return new CheapSofa();
  }
}
