---
order: 10
search: false
---

# Final exercise — Logger LLD

## Exercise: logger-lld - Configurable Sync/Async Logger

### Goal

Design a small extensible logging library that can be called concurrently and writes to configurable
output sinks.

This is the agreed scope after discussing the initial [interviewer prompt](problem_statement/) and
[candidate clarifications](candidate_discussion/).

### Requirements

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
