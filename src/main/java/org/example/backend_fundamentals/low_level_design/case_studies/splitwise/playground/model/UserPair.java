package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model;

import lombok.Value;

@Value
public class UserPair {
  int creditorId;
  int debtorId;

  public UserPair(int creditorId, int debtorId){
    this.creditorId = creditorId;
    this.debtorId = debtorId;
  }
}
