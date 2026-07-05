package org.example.backend_fundamentals.design_patterns.creational.builder.lombok_builder.playground;

import lombok.Builder;
import lombok.ToString;

/**
 * Lombok-generated variant of the Builder pattern. {@code @Builder} produces
 * a package-private all-args constructor, a static nested
 * {@code JobOfferBuilder} with fluent setters and {@code build()}, and a
 * static {@code JobOffer.builder()} entry point.
 *
 * <p>What you give up versus the hand-written variant in {@code simple_builder}:
 * required-field enforcement, in-{@code build()} validation, and naming
 * control. See {@code BuilderLombok.md} for the trade-offs and recovery
 * routes.
 */
@Builder
@ToString
public final class JobOffer {

  private final int salary;
  private final String city;
  private final int joiningBonus;
  private final int relocationBonus;
  private final int performanceBonus;
}
