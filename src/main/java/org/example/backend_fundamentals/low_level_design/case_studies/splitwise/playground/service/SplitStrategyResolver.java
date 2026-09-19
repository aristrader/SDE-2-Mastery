package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.service;

import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.enums.SplitType;

public class SplitStrategyResolver {

  private static final EqualSplitStrategy equalSplitStrategy = new EqualSplitStrategy();
  private static final ExactSplitStrategy exactSplitStrategy = new ExactSplitStrategy();
  private static final PercentageSplitStrategy percentageSplitStrategy = new PercentageSplitStrategy();

  SplitStrategy resolve(SplitType splitType) {

    if(splitType == null) {
      throw new IllegalArgumentException("Split type is required");
    }

    return switch (splitType) {
      case EQUAL -> equalSplitStrategy;
      case EXACT -> exactSplitStrategy;
      case PERCENTAGE -> percentageSplitStrategy;
      default -> throw new IllegalArgumentException("No splitStrategy exists for the passed splitType");
    };
  }
}
