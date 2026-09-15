### Goal

Design a small extensible logging library that can be called concurrently and writes to configurable
output sinks.

### Requirements
- Configuration supports logger type, sinks, sink levels, async buffer size, and timestamp format.

### Clarification for the first implementation

- When the async buffer is full, block until a slot is available rather than silently dropping a message.

### Test scenarios

- Synchronous logging.
- Asynchronous logging.
- Filtering based on a sink's minimum log level.
- Multiple threads logging concurrently.
- Enqueue-order preservation for asynchronous messages.
- Backpressure when the asynchronous buffer reaches capacity.

### Interview follow-ups

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
