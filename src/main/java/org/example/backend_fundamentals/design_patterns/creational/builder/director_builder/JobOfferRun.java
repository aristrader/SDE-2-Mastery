package org.example.backend_fundamentals.design_patterns.creational.builder.director_builder;

import lombok.extern.slf4j.Slf4j;

/**
 * Demo runner for the simple Director-paired Builder. Walks the three named
 * recipes (standard, senior, relocate), then a fourth scenario that applies
 * the senior recipe and overrides one field <i>after</i> the recipe runs
 * &mdash; demonstrating that the caller, not the Director, owns
 * {@link JobOffer.Builder#build()}.
 */
@Slf4j
public class JobOfferRun {

  public static void main(String[] args) {
    OfferDirector director = new OfferDirector();

    log.info("=== 1. Standard offer ===");
    JobOffer.Builder b1 = new JobOffer.Builder(100_000, "PUNE");
    director.constructStandardOffer(b1);
    System.out.println(b1.build());

    log.info("=== 2. Senior offer ===");
    JobOffer.Builder b2 = new JobOffer.Builder(200_000, "BANGALORE");
    director.constructSeniorOffer(b2);
    System.out.println(b2.build());

    log.info("=== 3. Relocate offer ===");
    JobOffer.Builder b3 = new JobOffer.Builder(150_000, "HYDERABAD");
    director.constructRelocateOffer(b3);
    System.out.println(b3.build());

    log.info("=== 4. Senior offer with post-recipe override (perf bonus 15k -> 20k) ===");
    JobOffer.Builder b4 = new JobOffer.Builder(200_000, "BANGALORE");
    director.constructSeniorOffer(b4);
    b4.performanceBonus(20_000);
    System.out.println(b4.build());
  }
}
