package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.service;

import java.math.BigDecimal;
import java.util.List;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model.Split;

public class PercentageSplitStrategy implements SplitStrategy{

  @Override
  public List<Split> calculate(BigDecimal amount, List<Split> splits) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || splits == null || splits.isEmpty()) {
      throw new IllegalArgumentException("Amount and splits must be valid");
    }

    for (Split split : splits) {
      if (split == null || split.getInputValue() == null
          || split.getInputValue().compareTo(BigDecimal.ZERO) <= 0
          || split.getInputValue().compareTo(BigDecimal.valueOf(100)) > 0) {
        throw new IllegalArgumentException("Each split percentage must be greater than 0 and at most 100");
      }
    }

    BigDecimal inputTotal = splits.stream()
        .map(Split::getInputValue)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (inputTotal.compareTo(BigDecimal.valueOf(100)) != 0) {
      throw new IllegalArgumentException("Split percentages must sum up to 100");
    }

    splits.forEach(split -> split.setAmountOwed(amount.multiply(split.getInputValue()).divide(BigDecimal.valueOf(100))));

    return splits;
  }
}
