package org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service;

import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.model.LogEntry;

public class JsonFormatter implements Formatter{

  @Override
  public String format(LogEntry logEntry) {
    System.out.println("Formatting the file to json format");
    return logEntry.toString();
  }
}
