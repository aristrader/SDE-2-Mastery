package org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.config;

import java.util.ArrayList;
import java.util.List;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service.Sink;

/** Immutable logger setup shared by synchronous and asynchronous delivery strategies. */
public class LoggerConfig {

  private final int asyncBufferSize;
  private final List<Sink> sinks;

  public LoggerConfig(List<Sink> sinks, int asyncBufferSize) {
    this.sinks = List.copyOf(sinks);
    this.asyncBufferSize = asyncBufferSize;
  }

  public List<Sink> getSinks(){
    return new ArrayList<>(sinks);
  }

  public int getAsyncBufferSize() {
    return asyncBufferSize;
  }
}
