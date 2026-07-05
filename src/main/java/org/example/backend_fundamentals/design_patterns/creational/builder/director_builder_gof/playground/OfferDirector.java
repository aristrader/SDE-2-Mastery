package org.example.backend_fundamentals.design_patterns.creational.builder.director_builder_gof.playground;

/**
 * Holds reusable, named construction recipes targeting
 * {@link OfferConstructionSteps}. Each recipe drives any builder implementing
 * the interface, so the same recipe can produce a different artefact per
 * builder. The Director never calls {@code build()} &mdash; the caller still
 * owns materialisation.
 */
public class OfferDirector {

  /** Senior offer: generous joining, relocation, and performance bonuses. */
  public void constructSeniorOffer(OfferConstructionSteps constructionSteps) {
    constructionSteps.joiningBonus(10000);
    constructionSteps.relocationBonus(10000);
    constructionSteps.performanceBonus(10000);
  }

  /** Junior offer: modest joining, relocation, and performance bonuses. */
  public void constructJuniorOffer(OfferConstructionSteps constructionSteps) {
    constructionSteps.joiningBonus(1000);
    constructionSteps.relocationBonus(1000);
    constructionSteps.performanceBonus(1000);
  }
}
