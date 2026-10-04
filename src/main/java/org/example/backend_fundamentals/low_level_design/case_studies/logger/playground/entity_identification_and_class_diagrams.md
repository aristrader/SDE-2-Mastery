# Logger — entity identification and class diagrams

## Goal

Design a small extensible logging library that can be called concurrently and writes to configurable
output sinks.

This is the agreed scope after discussing the initial [interviewer prompt](../exercise/problem_statement/) and
[candidate clarifications](../exercise/candidate_discussion/).

## Requirements

- A log message contains a timestamp, level, and message; callers provide only level and message.
- Support `DEBUG`, `INFO`, `WARN`, and `ERROR` levels.
- A logger has one or more sinks; callers never choose a sink.
- Each sink has a minimum level and ignores lower-level messages.
- Implement a console/STDOUT sink; allow file, database, Kafka, or HTTP sinks to be added later.
- Format messages before writing, with room for alternate formatter strategies.
- Support synchronous and asynchronous logger configurations.
- The asynchronous logger uses a configurable bounded buffer, preserves enqueue order, and does not drop
  messages that it has accepted.
- Logging is safe when called concurrently from multiple threads.
- A real sink that writes to a shared target must write one complete formatted record atomically; the
  console-only demonstration does not implement physical file output.
- Configuration supports logger type, sinks, sink levels, async buffer size, and timestamp format.

## Clarification for the first implementation

- When the async buffer is full, block until a slot is available rather than silently dropping a message.

## Test scenarios

- Synchronous logging.
- Asynchronous logging.
- Filtering based on a sink's minimum log level.
- Multiple threads logging concurrently.
- Enqueue-order preservation for asynchronous messages.
- Backpressure when the asynchronous buffer reaches capacity.

## Current implementation boundary

The playground demonstrates the object model, filtering, bounded queue, worker, and normal drain-on-close
path. Its `FileSink` and `JsonFormatter` are extension seams, not real file I/O or JSON serialization.
`LoggerConfig` currently holds sinks and async buffer size; explicit logger-type and timestamp-format
configuration remain documented follow-ups. Treat sink-failure isolation, external worker interruption,
and concurrent `close()` callers as follow-up behavior unless implemented deliberately.

## Interview follow-ups

- What alternative should be offered when an async buffer is full: reject, timeout, or drop?
- How would you flush pending logs during application shutdown?
- How would you support runtime level changes safely?
- How would multiple async workers change ordering guarantees?
- How would you handle sink failures and retries?
- How would you support multiple named loggers with separate configurations?
- How would you add file rotation?
- How would you support parameterized logging such as `User {} logged in`?

# Entities
- LogEntry
- Level (enum - debug, info, warn, error)
- Sink
- Formatter

# Class Diagrams

```text
LogEntry
- id: Long
- level: Level
- message: String
- timeStamp: LocalDateTime

Level (ENUM)
- DEBUG (1)
- INFO (2)
- WARN (3)
- ERROR (4)
- priority: int

Formatter (interface)
- format(LogEntry): String

TextFormatter implements Formatter
JsonFormatter implements Formatter

Sink (abstract class)
- logLevel: Level
- formatter: Formatter
- append(LogEntry): void
- write(String): void (protected)

FileSink extends Sink
ConsoleSink extends Sink

LoggerConfig
- sinks: List<Sink>
- getSinks(): List<Sink>

Logger
- loggerConfig: LoggerConfig
- id: AtomicLong
- log(level, message): void (public, final)
- submit(LogEntry): void (protected, abstract)
- dispatch(LogEntry): void (protected, final)
+ debug(message): void
+ info(message): void
+ warn(message): void
+ error(message): void

SyncLogger extends Logger
- submit(LogEntry): void

AsyncLogger extends Logger
- queue: BlockingQueue<LogEntry>
- worker: Thread
- lifecycleLock: Object
- acceptingLogs: boolean
- submit(LogEntry): void
- consume(): void
+ close(): void

Async flow
caller threads -> queue.put(entry) -> one worker queue.take() -> dispatch(entry)

Shutdown flow
close() -> reject future entries -> queue shutdown marker -> worker drains -> worker.join()
```
