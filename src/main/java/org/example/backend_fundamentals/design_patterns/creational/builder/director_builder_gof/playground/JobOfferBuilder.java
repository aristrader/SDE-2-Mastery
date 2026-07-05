package org.example.backend_fundamentals.design_patterns.creational.builder.director_builder_gof.playground;

/**
 * Concrete builder that produces an immutable {@link JobOffer} from
 * {@link OfferConstructionSteps} calls. Required candidate-driven data
 * ({@code salary}, {@code city}) is taken in the constructor; optional
 * template-driven bonuses are set by the Director (or direct calls).
 * Validation runs in {@link #build()}.
 *
 * @see OfferLetterBuilder
 */
public class JobOfferBuilder implements OfferConstructionSteps {

  private final int salary;
  private final String city;
  private int joiningBonus;
  private int relocationBonus;
  private int performanceBonus;

  /**
   * @param salary base salary in INR; validated at {@link #build()}
   * @param city offer location; validated at {@link #build()}
   */
  public JobOfferBuilder(int salary, String city) {
    this.salary = salary;
    this.city = city;
  }

  @Override
  public void joiningBonus(int amount) {
    this.joiningBonus = amount;
  }

  @Override
  public void relocationBonus(int amount) {
    this.relocationBonus = amount;
  }

  @Override
  public void performanceBonus(int amount) {
    this.performanceBonus = amount;
  }

  /**
   * Validates and returns the immutable {@link JobOffer}.
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
    return new JobOffer(salary, city, joiningBonus, relocationBonus, performanceBonus);
  }
}
