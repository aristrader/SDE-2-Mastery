package org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service;

import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.enums.Level;

public class FileSink extends Sink{

  public FileSink(
      Level level,
      Formatter formatter) {
    super(level, formatter);
  }

  @Override
  protected void write(String formatted) {
    System.out.println("Writting file to the file sink");
    System.out.println(formatted);
  }
}
