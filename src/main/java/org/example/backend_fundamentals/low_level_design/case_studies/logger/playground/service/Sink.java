package org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service;


import lombok.Getter;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.enums.Level;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.model.LogEntry;

/**
 * A destination with its own minimum level and formatting strategy.
 *
 * <p>A real sink that writes to a shared, non-thread-safe target must write one complete entry
 * atomically. For example, a file sink can keep a private {@code writeLock} and synchronize the
 * complete write-plus-newline operation on it. If separate sink objects share the same physical file,
 * they must share that target's lock rather than use separate instance locks.</p>
 */
@Getter
public abstract class Sink {
  private final Level logLevel;
  private final Formatter formatter;

  protected Sink(Level level, Formatter formatter){
    this.logLevel = level;
    this.formatter = formatter;
  }

  /** Filters first, then formats and writes only entries accepted by this sink. */
  public final void append(LogEntry logEntry){
    if (canAppend(logEntry)){
      String formatted = formatter.format(logEntry);
      write(formatted);
    }
  }

  private boolean canAppend(LogEntry logEntry){
    return logEntry.getLevel().getPriority() >= this.logLevel.getPriority();
  }

  protected abstract void write(String formatted);
}
