package org.example.backend_fundamentals.design_patterns.creational.builder.director_builder_gof.playground;

import lombok.ToString;

/**
 * Immutable record of a candidate's compensation terms, produced by
 * {@link JobOfferBuilder} in the GoF Director-Builder demo.
 *
 * <p>Has <i>no nested Builder</i> &mdash; the builder is a top-level class
 * so the file-tree shows {@code JobOfferBuilder} and
 * {@code OfferLetterBuilder} side by side. Constructor is package-private so
 * {@link JobOfferBuilder} can call it from {@code build()} in the same
 * package.
 *
 * @see JobOfferBuilder
 * @see OfferDirector
 */
@ToString
public final class JobOffer {

  private final int salary;
  private final String city;
  private final int joiningBonus;
  private final int relocationBonus;
  private final int performanceBonus;

  JobOffer(int salary, String city, int joiningBonus, int relocationBonus, int performanceBonus) {
    this.salary = salary;
    this.city = city;
    this.joiningBonus = joiningBonus;
    this.relocationBonus = relocationBonus;
    this.performanceBonus = performanceBonus;
  }
}
