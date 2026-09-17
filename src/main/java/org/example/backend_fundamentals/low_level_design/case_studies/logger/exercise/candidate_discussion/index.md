---
search: false
---

# Candidate discussion — Logger

The interviewer begins only with “Design a logging library.” Do not immediately draw classes. Narrow the
problem until delivery, output, and concurrency have explicit contracts.

## Clarify the problem

**Candidate:** “Is this an in-process library used by one application, or a service that receives logs from
other applications?”

**Agreed answer:** It is an in-process library. Network ingestion and a centralized log platform are out of
scope.

**Why it matters:** The first version needs an object model and local output targets, not brokers,
authentication, schemas, or distributed delivery guarantees.

**Candidate:** “What does the caller supply, and which levels are needed?”

**Agreed answer:** The caller supplies only a level and message. Every entry also records a timestamp.
Support `DEBUG`, `INFO`, `WARN`, and `ERROR` in severity order.

**Why it matters:** `Level` is a finite ordered enum. `LogEntry` is the immutable data passed through both
delivery modes. Do not add thread names or `FATAL` merely because another logger design includes them.

**Candidate:** “Can one logger fan out to several targets? Do filter level and output format vary by target?”

**Agreed answer:** A logger can use one or more sinks. Each sink has its own minimum level and formatter;
the caller never chooses either.

**Why it matters:** Filtering and formatting belong to `Sink`. `Formatter` is composed with a sink instead
of creating classes such as `JsonConsoleSink` and `TextFileSink` for every combination.

**Candidate:** “Do we need synchronous and asynchronous delivery? If the buffer fills, may a log be lost?”

**Agreed answer:** Support both modes. The asynchronous mode uses a configurable bounded buffer and blocks
the caller for space rather than silently dropping an entry it is trying to submit.

**Why it matters:** `SyncLogger` dispatches immediately. `AsyncLogger` uses one `ArrayBlockingQueue` and
one worker. `put()` captures the agreed backpressure policy; `offer()` or `add()` would describe different
contracts.

**Candidate:** “What does thread-safe mean here: safe logger state only, or also an atomic record write to
each real target? What order is required across concurrent callers?”

**Agreed answer:** Shared logger and queue state must be safe. One worker preserves queue-acceptance order;
there is no caller-invocation order guarantee across racing threads. A real shared file or remote target
must write one formatted record atomically.

**Why it matters:** Queue FIFO does not decide which concurrent caller reaches `put()` first. The current
console-only playground demonstrates the design; a production sink must protect its actual shared output
target around one complete record write.

**Candidate:** “Are sink failures, retries, file rotation, hot configuration changes, or named logger
hierarchies required now?”

**Agreed answer:** No. Configuration is static for this exercise. Those are follow-ups, as are real file,
database, Kafka, and HTTP sinks.

**Why it matters:** The core model has extension seams through `Sink` and `Formatter`, but it does not claim
that the playground already implements a production failure or reload policy.

## Final scope

- An immutable entry carries timestamp, level, and message.
- A logger fans out to configured sinks; each sink applies its own minimum level and formatter.
- Sync delivery writes on the caller thread. Async delivery uses a bounded FIFO queue and one worker.
- `put()` blocks when the buffer is full. A normal close places a marker after accepted entries and drains it.
- Real target atomicity, failure isolation, retries, rotation, named loggers, and runtime configuration are
  extensions, not completed claims.

The final [exercise](../) is the source of truth for requirements and test scenarios.
