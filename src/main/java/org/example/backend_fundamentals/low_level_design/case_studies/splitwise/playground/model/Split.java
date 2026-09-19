package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class Split {
  private Integer userId;
  private BigDecimal amountOwed;
  private BigDecimal inputValue;

  public Split(Integer userId, BigDecimal amountOwed, BigDecimal inputValue){
    if (userId == null) {
      throw new IllegalArgumentException("A split must have a user ID");
    }

    this.userId = userId;
    this.amountOwed = amountOwed;
    this.inputValue = inputValue;
  }
}
