package org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.enums.Level;

/** Immutable log data captured before delivery to one or more sinks. */
@Getter
@AllArgsConstructor
@ToString
public class LogEntry {
  private final Long id;
  private final Level level;
  private final String message;
  private final LocalDateTime timeStamp;
}
