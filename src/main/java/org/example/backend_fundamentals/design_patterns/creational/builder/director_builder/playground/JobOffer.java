package org.example.backend_fundamentals.design_patterns.creational.builder.director_builder.playground;

import lombok.ToString;

/**
 * Immutable record of a candidate's compensation terms, used in this package
 * as the product paired with {@link OfferDirector}. Same shape as
 * {@code simple_builder/JobOffer.java}; duplicated per the repo's
 * self-contained-package convention. The pattern lesson lives in
 * {@link OfferDirector} and {@code BuilderDirector.md}.
 *
 * @see Builder
 * @see OfferDirector
 */
@ToString
public final class JobOffer {

  private final int salary;
  private final String city;
  private final int joiningBonus;
  private final int relocationBonus;
  private final int performanceBonus;

  private JobOffer(Builder builder) {
    this.salary = builder.salary;
    this.city = builder.city;
    this.joiningBonus = builder.joiningBonus;
    this.relocationBonus = builder.relocationBonus;
    this.performanceBonus = builder.performanceBonus;
  }

  /**
   * Fluent builder for {@link JobOffer}. {@link OfferDirector} recipes operate
   * on instances of this class &mdash; the caller supplies required fields via
   * the constructor; recipes configure the optional bonuses.
   */
  public static class Builder {

    private final int salary;
    private final String city;
    private int joiningBonus;
    private int relocationBonus;
    private int performanceBonus;

    /**
     * @param salary base salary in INR; validated at {@link #build()}
     * @param city offer location; validated at {@link #build()}
     */
    public Builder(int salary, String city) {
      this.salary = salary;
      this.city = city;
    }

    /** Sets the optional joining bonus. */
    public Builder joiningBonus(int joiningBonus) {
      this.joiningBonus = joiningBonus;
      return this;
    }

    /** Sets the optional relocation bonus. */
    public Builder relocationBonus(int relocationBonus) {
      this.relocationBonus = relocationBonus;
      return this;
    }

    /** Sets the optional first-year performance bonus. */
    public Builder performanceBonus(int performanceBonus) {
      this.performanceBonus = performanceBonus;
      return this;
    }

    /**
     * Validates the contract and returns the immutable {@link JobOffer}.
     *
     * @throws IllegalStateException if any rule is violated (salary
     *     {@code > 0}, city non-blank, bonuses {@code >= 0}, total bonuses
     *     {@code <=} salary)
     */
    public JobOffer build() {
      if (salary <= 0) {
        throw new IllegalStateException("salary must be > 0, got: " + salary);
      }
      if (city == null || city.isBlank()) {
        throw new IllegalStateException("city is required and cannot be blank");
      }
      if (joiningBonus < 0 || relocationBonus < 0 || performanceBonus < 0) {
        throw new IllegalStateException(
            "bonuses cannot be negative: joining=" + joiningBonus
                + ", relocation=" + relocationBonus
                + ", performance=" + performanceBonus);
      }
      int totalBonus = joiningBonus + relocationBonus + performanceBonus;
      if (totalBonus > salary) {
        throw new IllegalStateException(
            "total bonuses (" + totalBonus + ") cannot exceed salary (" + salary + ")");
      }
      return new JobOffer(this);
    }
  }
}
