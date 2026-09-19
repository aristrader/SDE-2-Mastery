package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.service;

import java.math.BigDecimal;
import java.util.List;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model.Split;

public class ExactSplitStrategy implements SplitStrategy{

  @Override
  public List<Split> calculate(BigDecimal amount, List<Split> splits) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || splits == null || splits.isEmpty()) {
      throw new IllegalArgumentException("Amount and splits must be valid");
    }

    for (Split split : splits) {
      if (split == null || split.getInputValue() == null
          || split.getInputValue().compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Each exact split amount must be positive");
      }
    }

    BigDecimal inputTotal = splits.stream()
        .map(Split::getInputValue)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (inputTotal.compareTo(amount) != 0) {
      throw new IllegalArgumentException("Exact split amounts must sum up to the total amount");
    }

    splits.forEach(split -> split.setAmountOwed(split.getInputValue()));

    return splits;
  }
}
