---
search: false
---

# Candidate discussion — Logger

Start with a narrow proposal:

> “I’ll build an in-memory configurable logger with a console sink first. I’ll support synchronous and
> asynchronous modes, and make the bounded-buffer behavior explicit.”

## Questions to ask

1. Which log levels and fields must every log entry contain?
2. Can one logger write to multiple sinks, with each sink filtering by its own minimum level?
3. Must the first version support asynchronous logging and concurrent callers?
4. When the asynchronous buffer is full, should the caller block, time out, reject, or drop the entry?
5. Are file output, retries, log rotation, persistence, and remote collection in scope now?

## Agreed scope for this exercise

- A caller provides a level and message; entries also contain timestamp information.
- Console output is sufficient, but the design supports additional sinks and formatters later.
- Sync and async configurations are required; the async logger has a bounded buffer.
- The agreed backpressure policy is to block while the buffer is full rather than drop an accepted entry.
- File rotation, retry policies, remote sinks, and named logger hierarchies are follow-ups.

The final [exercise](../) records the precise requirements and test scenarios.
