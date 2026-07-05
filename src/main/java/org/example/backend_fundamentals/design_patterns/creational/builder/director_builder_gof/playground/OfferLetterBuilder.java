package org.example.backend_fundamentals.design_patterns.creational.builder.director_builder_gof.playground;

/**
 * Concrete builder that produces a printable, candidate-facing offer letter
 * from {@link OfferConstructionSteps} calls. Implements the same step
 * interface as {@link JobOfferBuilder} so the same {@link OfferDirector}
 * recipe drives both, but {@link #build()} returns a {@link String} rather
 * than a {@link JobOffer} &mdash; the artefact is unstructured text, and
 * carries no cross-field invariants of its own.
 */
public class OfferLetterBuilder implements OfferConstructionSteps {

  private final StringBuilder letter = new StringBuilder();

  /**
   * Pre-populates the letter header with candidate-specific data.
   *
   * @param candidateName name addressed in the salutation
   * @param salary base salary in INR
   * @param city offer location
   */
  public OfferLetterBuilder(String candidateName, int salary, String city) {
    letter.append("Dear ").append(candidateName).append(",\n\n");
    letter.append("We are pleased to offer you a role in ").append(city)
        .append(" at a base salary of INR ").append(salary).append(".\n");
    letter.append("Your compensation includes:\n");
  }

  @Override
  public void joiningBonus(int amount) {
    letter.append("  - Joining bonus: INR ").append(amount).append("\n");
  }

  @Override
  public void relocationBonus(int amount) {
    letter.append("  - Relocation bonus: INR ").append(amount).append("\n");
  }

  @Override
  public void performanceBonus(int amount) {
    letter.append("  - First-year performance bonus: INR ").append(amount).append("\n");
  }

  /** Appends the closing line and returns the assembled letter. */
  public String build() {
    letter.append("\nWelcome aboard!\n");
    return letter.toString();
  }
}
