package org.example.backend_fundamentals.design_patterns.creational.builder.director_builder_gof;

/**
 * Shared step interface that {@link OfferDirector} recipes drive, and that
 * every concrete builder in this package implements. By typing recipe
 * parameters against this interface rather than a concrete builder, the same
 * recipe drives any implementing builder &mdash; producing a different
 * artefact per builder.
 *
 * <p>Deliberately omits {@code build()} (each builder returns a different
 * type) and the candidate-driven fields salary / city / candidateName (those
 * enter via each builder's own constructor, not as template steps). See
 * {@code BuilderDirectorGof.md} for the reasoning.
 */
public interface OfferConstructionSteps {

  void joiningBonus(int amount);

  void relocationBonus(int amount);

  void performanceBonus(int amount);
}
