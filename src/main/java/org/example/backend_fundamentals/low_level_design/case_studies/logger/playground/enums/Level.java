package org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.enums;

import lombok.Getter;

@Getter
public enum Level {

  DEBUG(1),
  INFO(2),
  WARN(3),
  ERROR(4);

  private final int priority;

  Level(int priority){
    this.priority = priority;
  }
}
