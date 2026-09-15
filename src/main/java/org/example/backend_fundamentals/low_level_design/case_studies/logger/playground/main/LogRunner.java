package org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.main;

import java.util.ArrayList;
import java.util.List;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.config.LoggerConfig;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.enums.Level;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service.AsyncLogger;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service.ConsoleSink;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service.FileSink;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service.JsonFormatter;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service.Sink;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service.SyncLogger;
import org.example.backend_fundamentals.low_level_design.case_studies.logger.playground.service.TextFormatter;

public class LogRunner {
  public static void main(String args[]) throws InterruptedException {

    List<Sink> sinks = new ArrayList<>();

    ConsoleSink consoleSink = new ConsoleSink(
        Level.DEBUG,
        new TextFormatter()
    );

    FileSink fileSink = new FileSink(
        Level.INFO,
        new JsonFormatter()
    );

    sinks.add(consoleSink);
    sinks.add(fileSink);

    LoggerConfig loggerConfig = new LoggerConfig(sinks, 100);

    SyncLogger syncLogger = new SyncLogger(loggerConfig);

//    syncLogger.debug("Hi this is debug log");
//    syncLogger.info("Hi this is info log");
//    syncLogger.warn("Hi this is warn log");
//    syncLogger.error("Hi this is error log");

    AsyncLogger asyncLogger = new AsyncLogger(loggerConfig);

//    asyncLogger.debug("Hi this is debug log");
//    asyncLogger.info("Hi this is info log");
//    asyncLogger.warn("Hi this is warn log");
//    asyncLogger.error("Hi this is error log");
//
//    asyncLogger.close();
//
//    try {
//      asyncLogger.info("This should be rejected");
//    } catch (IllegalStateException e) {
//      System.out.println(e.getMessage());
//    }

    Thread firstProducer = new Thread(() -> {
      for (int index = 1; index <= 3; index++) {
        asyncLogger.info("First producer log " + index);
      }
    });

    Thread secondProducer = new Thread(() -> {
      for (int index = 1; index <= 3; index++) {
        asyncLogger.info("Second producer log " + index);
      }
    });

    firstProducer.start();
    secondProducer.start();

    firstProducer.join();
    secondProducer.join();

    asyncLogger.close();
  }
}
