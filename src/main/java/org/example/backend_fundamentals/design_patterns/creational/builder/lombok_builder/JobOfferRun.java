package org.example.backend_fundamentals.design_patterns.creational.builder.lombok_builder;

import lombok.extern.slf4j.Slf4j;

/**
 * Demo runner for the Lombok Builder variant. Shows the happy path and the
 * "no setters called" path &mdash; the latter producing a silently-invalid
 * product that the hand-written {@code simple_builder} variant would refuse
 * to compile. Entry point is {@code JobOffer.builder()}, the static method
 * Lombok generates on the product.
 */
@Slf4j
public class JobOfferRun {

  public static void main(String[] args) {
    log.info("=== 1. Happy path: every field set via fluent setters ===");
    JobOffer offer = JobOffer.builder()
        .salary(150_000)
        .city("HYDERABAD")
        .joiningBonus(20_000)
        .relocationBonus(10_000)
        .performanceBonus(15_000)
        .build();
    System.out.println(offer);

    log.info("=== 2. No setters called: Lombok permits this; the product is silently invalid ===");
    JobOffer empty = JobOffer.builder().build();
    System.out.println(empty);
  }
}
