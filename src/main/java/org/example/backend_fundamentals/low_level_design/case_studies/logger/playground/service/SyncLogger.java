package org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.config.LoggerConfig;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.enums.Level;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.model.LogEntry;

/** Writes each entry on the caller thread. */
public class SyncLogger extends Logger{

  public SyncLogger(
      LoggerConfig loggerConfig) {
    super(loggerConfig);
  }

  @Override
  protected void submit(LogEntry logEntry) {
      dispatch(logEntry);
  }
}
