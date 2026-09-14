package org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service;

import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.enums.Level;

public class ConsoleSink extends Sink{

  public ConsoleSink(
      Level level,
      Formatter formatter) {
    super(level, formatter);
  }

  /**
   * The two demonstration prints can interleave when synchronous callers run concurrently. A production
   * sink should write one complete formatted entry in one atomic operation; see {@link Sink}.
   */
  @Override
  protected void write(String formatted) {
    System.out.println("Writting to console");
    System.out.println(formatted);
  }
}
