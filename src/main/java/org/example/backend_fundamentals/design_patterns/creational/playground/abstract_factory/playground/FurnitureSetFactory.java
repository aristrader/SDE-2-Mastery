package org.example.backend_fundamentals.design_patterns.creational.abstract_factory;

/**
 * Abstract factory for the furniture family. Each concrete implementation
 * produces a coordinated set of {@link Chair} and {@link Sofa} that belong
 * to the same family; callers depend on this interface only and remain
 * ignorant of which concrete family they hold.
 *
 * <p>Deliberately limited to product-creating methods. Consumption logic
 * (printing, layout, billing) lives on the caller side; per-family behaviour
 * (warranties, after-sales service plans) is added by introducing additional
 * products produced by the same factory, not by adding methods here.
 *
 * <p>See {@code AbstractFactory.md} for the full walkthrough and the design
 * decisions behind these constraints.
 */
public interface FurnitureSetFactory {

  Chair createChair();

  Sofa createSofa();
}
