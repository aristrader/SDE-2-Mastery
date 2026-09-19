package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model.Split;

public class EqualSplitStrategy implements SplitStrategy{

  @Override
  public List<Split> calculate(BigDecimal amount, List<Split> splits) {

    if(amount == null || amount.compareTo(BigDecimal.valueOf(0))<=0 || splits == null || splits.isEmpty()){
      throw new IllegalArgumentException("Amount and splits must be valid");
    }

    BigDecimal share = amount.divide(BigDecimal.valueOf(splits.size()), 2, RoundingMode.DOWN);
    splits.forEach(split -> split.setAmountOwed(share));
    splits.get(splits.size()-1).setAmountOwed(amount.subtract(share.multiply(
        BigDecimal.valueOf(splits.size()-1))));

    return splits;
  }
}
