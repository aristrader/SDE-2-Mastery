---
order: 140
---

# Logger LLD

Workspace for designing a concurrent, configurable logger with log levels, per-sink filtering, formatting,
and synchronous or asynchronous output.

Start with the vague [interviewer prompt](exercise/problem_statement/), lead the
[candidate discussion](exercise/candidate_discussion/), then solve the agreed [final exercise](exercise/).
Start with console output and an explicit bounded-buffer backpressure policy. Additional sink types, log
rotation, and distributed collection are follow-ups.

## Quick recall

- A sink's configured minimum level decides which messages it receives.
- Preserve asynchronous enqueue order; extra workers require an explicit ordering trade-off.
- Keep level filtering, formatting, and writing separate when their behavior can vary.
