package org.example.backend_fundamentals.low_level_design.case_studies.splitwise.playground.model;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

@Data
public class User {
  private static final AtomicInteger ID_SEQUENCE = new AtomicInteger();

  private int id;
  private String name;
  private Long phoneNo;
  private String email;

  public User(String name, Long phoneNo, String email){

    if(name == null || (phoneNo == null && email == null)){
      throw new IllegalArgumentException("Please supply valid values for user");
    }

    this.id = ID_SEQUENCE.incrementAndGet();
    this.name = name;
  this.phoneNo = phoneNo;
  this.email = email;
  }
}
