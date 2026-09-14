package org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service;


import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.config.LoggerConfig;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.enums.Level;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.model.LogEntry;

/**
 * Common logging flow: create an immutable entry, then delegate delivery to the chosen logger type.
 *
 * <p>Subclasses vary only in {@link #submit(LogEntry)}: synchronous delivery dispatches immediately,
 * while asynchronous delivery queues the entry for a worker.</p>
 */
@RequiredArgsConstructor
@Getter
public abstract class Logger {

  private final AtomicLong id = new AtomicLong(0);
  private final LoggerConfig loggerConfig;

  /**
   * Creates one entry and hands it to the delivery strategy. Callers provide only level and message;
   * they never select a sink.
   */
  public final void log(Level level, String message){
    LogEntry logEntry = new LogEntry(
        this.id.addAndGet(1),
        level,
        message,
        LocalDateTime.now()
    );

    submit(logEntry);
  }

  /**
   * Delivers an already-created entry. This is the synchronous/asynchronous variation point.
   */
  protected abstract void submit(LogEntry logEntry);

  /**
   * Applies every configured sink's filtering, formatting, and write behavior to one entry.
   */
  protected final void dispatch(LogEntry logEntry){
    for(Sink sink: getLoggerConfig().getSinks()){
      sink.append(logEntry);
    }
  }

  public void debug(String message){
    log(Level.DEBUG, message);
  }

  public void info(String message){
    log(Level.INFO, message);
  }

  public void warn(String message){
    log(Level.WARN, message);
  }

  public void error(String message){
    log(Level.ERROR, message);
  }
}
