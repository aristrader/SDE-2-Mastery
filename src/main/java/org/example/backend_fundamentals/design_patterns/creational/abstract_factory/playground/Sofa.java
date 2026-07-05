package org.example.backend_fundamentals.design_patterns.creational.abstract_factory.playground;

/**
 * Abstract product in the Abstract Factory family. Paired with {@link Chair}
 * &mdash; every concrete factory produces a {@code Chair} and a {@code Sofa}
 * that belong to the same family.
 */
public interface Sofa {

  String getMaterial();

  String getColor();
}
