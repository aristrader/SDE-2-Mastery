package org.example.backend_fundamentals.design_patterns.creational.abstract_factory.playground;

/**
 * Abstract product in the Abstract Factory family. Concrete factories return
 * instances typed against this interface so callers depend on the abstract
 * type, never on a concrete chair class.
 */
public interface Chair {

  String getMaterial();

  String getColor();
}
