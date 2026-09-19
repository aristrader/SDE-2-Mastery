package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.enums.SplitType;

@Data
public class Expense {
  private static final AtomicInteger ID_SEQUENCE = new AtomicInteger();

  private int id;
  private BigDecimal amount;
  private Integer paidByUserId;
  private List<Split> splits;
  private SplitType splitType;
  private Integer groupId;

  public Expense(BigDecimal amount, Integer paidByUserId,
      List<Split> splits, SplitType splitType, Integer groupId) {

    if(amount == null || paidByUserId == null || splits == null || splits.isEmpty() || splitType == null){
      throw new IllegalStateException("Incorrect values for the expense constuctor");
    }

    this.id = ID_SEQUENCE.incrementAndGet();
    this.amount = amount;
    this.paidByUserId = paidByUserId;
    this.splits = splits;
    this.splitType = splitType;
    this.groupId = groupId;
  }
}
