package org.example.backend_fundamentals.design_patterns.creational.builder.simple_builder.playground;

import lombok.extern.slf4j.Slf4j;

/**
 * Demo runner for the EJ-style Builder. Walks happy path, defaults-only, and
 * validation-failure scenarios. There is no "forgot to supply salary"
 * scenario because the compiler refuses to construct {@link JobOffer.Builder}
 * without it.
 */
@Slf4j
public class JobOfferRun {

  public static void main(String[] args) {
    log.info("=== 1. Happy path: required + all optionals ===");
    JobOffer offer = new JobOffer.Builder(150_000, "HYDERABAD")
        .joiningBonus(20_000)
        .relocationBonus(10_000)
        .performanceBonus(15_000)
        .build();
    System.out.println(offer);

    log.info("=== 2. Defaults only: required fields, optionals default to 0 ===");
    JobOffer minimal = new JobOffer.Builder(120_000, "BANGALORE").build();
    System.out.println(minimal);

    log.info("=== 3. Validation failure: total bonuses > salary ===");
    try {
      new JobOffer.Builder(50_000, "PUNE")
          .joiningBonus(30_000)
          .relocationBonus(30_000)
          .build();
      log.error("Expected IllegalStateException but build() succeeded");
    } catch (IllegalStateException e) {
      log.info("Correctly rejected by build(): {}", e.getMessage());
    }
  }
}
