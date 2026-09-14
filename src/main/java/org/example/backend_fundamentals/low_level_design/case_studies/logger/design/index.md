---
order: 20
search: false
---

# Logger Design Notes

Use the playground design draft for entity identification and class diagrams. This page records the
decisions that matter when explaining the implementation in an interview.

## Core responsibilities

| Type | Responsibility |
|---|---|
| `Logger` | Creates a `LogEntry` and delegates delivery. Callers never choose a sink. |
| `SyncLogger` | Dispatches on the caller thread. |
| `AsyncLogger` | Admits entries to a bounded queue and lets one worker dispatch them. |
| `Sink` | Applies its minimum-level filter, formatter, and target-specific write. |
| `Formatter` | Converts a structured entry to text, JSON, or another representation. |

The key variation point is `Logger.submit(entry)`: synchronous and asynchronous delivery share the same
entry, filtering, formatting, and sink logic.

## Current implementation scope

The console sink and text formatter demonstrate the completed flow. `FileSink` and `JsonFormatter` currently
prove the extension seam rather than perform real file I/O or JSON serialization. `LogEntry` captures a
timestamp, but configurable timestamp formatting remains a small follow-up to add in the formatter/config
layer without changing logger delivery.

## Async buffer policy

The async logger uses a bounded `ArrayBlockingQueue<LogEntry>`. When the queue is full, it blocks the
caller until the worker creates space. This is intentional backpressure: it bounds memory use and does not
silently lose a log that the caller expected to enqueue.

| Method | Queue-full behavior | Fit for this logger |
|---|---|---|
| `add(entry)` | Immediately throws `IllegalStateException`. | No: rejects the log. |
| `offer(entry)` | Immediately returns `false`. | No: caller must choose a drop/reject policy. |
| `put(entry)` | Waits until capacity is available. | Yes: required blocking policy. |
| `offer(entry, timeout, unit)` | Waits only until the timeout. | Possible future policy. |

`put()` can throw `InterruptedException` if the waiting caller is cancelled or the application is shutting
down. The entry was never accepted by the queue, so restore the interrupt signal and fail the log call:

```java
catch (InterruptedException exception) {
  Thread.currentThread().interrupt();
  throw new RuntimeException("Interrupted while queuing log entry", exception);
}
```

## Producer-consumer flow

```text
Caller threads
    │ log(level, message)
    ▼
AsyncLogger.submit(entry)
    │ queue.put(entry) — blocks only when full
    ▼
ArrayBlockingQueue<LogEntry>
    │ queue.take() — worker waits when empty
    ▼
Single worker → dispatch(entry) → each configured sink
```

`new Thread(this::consume)` only creates the worker object. `worker.start()` runs `consume()` on a new
thread; directly calling `consume()` would run it on the caller thread and would not be asynchronous.

For the concurrent demonstration, the main thread starts two producer threads, then calls
`firstProducer.join()` and `secondProducer.join()`. Those joins wait until both producers finish submitting
logs before shutdown begins. `worker.join()` inside `close()` instead waits until output is drained.

## Shutdown without losing accepted logs

The worker cannot simply be interrupted to stop it: an interrupt can make it exit while accepted entries
remain in the queue. The implementation puts one identity-only shutdown marker at the tail instead:

```text
[entry 1, entry 2] → close() → [entry 1, entry 2, shutdown marker]
```

The worker dispatches entries before the marker, sees the marker, and returns. The lifecycle lock makes
queue admission and marker placement one decision:

1. `submit()` checks that the logger accepts logs and enqueues the entry while holding the lock.
2. `close()` obtains the same lock, marks the logger closed, and enqueues the marker.
3. Later callers see the closed state and receive `IllegalStateException`; none can enqueue after the marker.
4. `close()` calls `worker.join()`, so its caller knows accepted logs have been processed when it returns.

## Ordering and concurrent callers

One worker dispatches entries in FIFO **queue-acceptance order**. Concurrent callers have no guaranteed
relative ordering before they reach `queue.put()`.

An `AtomicLong` guarantees unique IDs, but an ID allocated before queue admission can appear after a higher
ID in output: another caller may reach `put()` first. If sequence IDs must exactly represent acceptance
order, allocate the ID in the same critical section as queue admission, or serialize ID allocation and
submission together.

Adding more workers improves throughput but allows different entries to finish writing out of order. Keep
one worker when global output order matters.

## Sink write boundary

`Sink.append()` owns level filtering and formatting; a concrete sink owns the actual target write. The
current console/file classes are demonstration sinks. Their two `println` calls can interleave for
concurrent synchronous callers, so a production sink should write one complete entry atomically.

For a non-thread-safe file writer, lock the whole entry write:

```java
private final Object writeLock = new Object();

protected void write(String formatted) {
  synchronized (writeLock) {
    writer.write(formatted);
    writer.newLine();
  }
}
```

The lock must match the real shared resource. A per-instance lock is sufficient for one sink instance and
one file. If multiple sink instances share one file, they need one shared file-target lock.

## Interview follow-ups

- A full buffer can reject (`add`), return failure (`offer`), block (`put`), or time out (`offer` with a
  timeout). The requirement chooses blocking.
- Multiple named loggers can each own a `LoggerConfig` and delivery strategy.
- Runtime level changes require safely replacing or synchronizing the mutable configuration.
- File rotation, retries, remote sinks, and parameterized messages are extensions; none require changing
  `Logger`'s caller-facing API.

## Quick recall

- **Why `put()` instead of `offer()`?** The stated policy blocks on a full bounded buffer rather than drops.
- **Why one worker?** A FIFO queue plus one consumer preserves accepted-entry output order.
- **What does `join()` do?** It makes the current thread wait until the target thread has ended.
- **Why a shutdown marker?** It drains entries already accepted before ending the worker.
- **Why use the same lifecycle lock in `submit()` and `close()`?** It prevents a log from entering after the
  shutdown marker.
- **Where should a sink lock live?** At the actual shared output target, around one complete entry write.
