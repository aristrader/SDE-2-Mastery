package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

@Data
public class Settlement {
  private static final AtomicInteger ID_SEQUENCE = new AtomicInteger();

  private int id;
  private Integer debtorId;
  private Integer creditorId;
  private BigDecimal amount;
  private Integer groupId;

  public Settlement(Integer debtorId, Integer creditorId, BigDecimal amount, Integer groupId){

    if(debtorId == null || creditorId == null || amount == null) {
      throw new IllegalArgumentException("please supply valid values for the settlement");
    }

    this.id = ID_SEQUENCE.incrementAndGet();
    this.debtorId = debtorId;
    this.creditorId = creditorId;
    this.amount = amount;
    this.groupId = groupId;
  }
}
