package org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service;

import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.config.LoggerConfig;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.enums.Level;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.model.LogEntry;

/**
 * Queues log entries for one background worker instead of writing on the caller thread.
 *
 * <p>The bounded queue provides FIFO delivery and backpressure: {@code put()} blocks when full rather
 * than silently dropping an entry. One worker preserves queue acceptance order.</p>
 */
public class AsyncLogger extends Logger{

  private final BlockingQueue<LogEntry> queue;
  private final Thread worker;
  /** Identity marker placed after accepted entries so the worker drains them before stopping. */
  private static final LogEntry SHUTDOWN_SIGNAL = new LogEntry(-1L, Level.DEBUG, "", LocalDateTime.MIN);

  /**
   * Coordinates queue admission with shutdown. A log must not be admitted after the shutdown marker.
   */
  private final Object lifecycleLock = new Object();
  private boolean acceptingLogs = true;

  public AsyncLogger(
      LoggerConfig loggerConfig) {
    super(loggerConfig);
    this.queue = new ArrayBlockingQueue<>(getLoggerConfig().getAsyncBufferSize());
    this.worker = new Thread(this::consume);
    worker.start();
  }

  @Override
  protected void submit(LogEntry logEntry) {
    synchronized (lifecycleLock) {
      if (!acceptingLogs) {
        throw new IllegalStateException("Logger is closed");
      }

      try {
        queue.put(logEntry);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Interrupted while queuing log entry", e);
      }
    }

  }

  /**
   * Waits for entries, dispatches them one at a time, and stops only after the shutdown marker.
   */
  private void consume(){
    while (true) {
      try {
        LogEntry logEntry = queue.take();

        if(logEntry == SHUTDOWN_SIGNAL){
          return;
        }

        dispatch(logEntry);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  /**
   * Rejects future logs, queues a shutdown marker after accepted entries, then waits for the worker to
   * drain and terminate.
   */
  public void close() {
    try {
      synchronized (lifecycleLock) {
        if (!acceptingLogs) {
          return;
        }

        acceptingLogs = false;
        queue.put(SHUTDOWN_SIGNAL);
      }

      worker.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted while shutting down logger", e);
    }
  }

}
