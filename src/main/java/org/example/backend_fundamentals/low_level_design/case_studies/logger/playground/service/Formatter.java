package org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service;

import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.model.LogEntry;

/** Converts a structured {@link LogEntry} into the representation required by a sink. */
public interface Formatter {
  String format(LogEntry logEntry);
}
