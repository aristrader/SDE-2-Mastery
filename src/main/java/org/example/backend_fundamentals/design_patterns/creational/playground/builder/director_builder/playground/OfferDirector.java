package org.example.backend_fundamentals.design_patterns.creational.builder.director_builder;

/**
 * Holds reusable, named construction recipes for {@link JobOffer} &mdash;
 * the <i>simple</i> Director flavour where each recipe takes the concrete
 * {@link JobOffer.Builder} type. The Director never calls {@code build()};
 * the caller still owns materialisation, can layer additional setters after
 * the recipe, and lets the builder's validation run.
 *
 * <p>See {@code BuilderDirector.md} for when this pays off vs inlining
 * recipes at the call site.
 *
 * @see JobOffer
 */
public class OfferDirector {

  /** Standard offer: modest joining bonus only. */
  public void constructStandardOffer(JobOffer.Builder builder) {
    builder.joiningBonus(5_000)
        .relocationBonus(0)
        .performanceBonus(0);
  }

  /** Senior offer: higher joining bonus plus a first-year performance bonus. */
  public void constructSeniorOffer(JobOffer.Builder builder) {
    builder.joiningBonus(10_000)
        .relocationBonus(0)
        .performanceBonus(15_000);
  }

  /** Relocation offer: modest joining bonus, sizeable relocation, smaller performance. */
  public void constructRelocateOffer(JobOffer.Builder builder) {
    builder.joiningBonus(5_000)
        .relocationBonus(20_000)
        .performanceBonus(10_000);
  }
}
