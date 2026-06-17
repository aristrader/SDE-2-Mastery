# Backpressure

## In one sentence

**Backpressure is the system telling the producer "slow down, I can't keep up" — instead of silently falling over.**

## The problem it solves

Imagine a fast producer and a slow consumer:

- **Producer:** the HTTP handler, receiving 1000 requests/sec, each wanting to archive something to S3.
- **Consumer:** S3 uploads, which maybe finish at 200/sec.

If you do nothing, the gap (800/sec) has to go *somewhere*. Three possible outcomes:

| Strategy | What happens |
|---|---|
| Unbounded queue | Tasks pile up in memory forever → heap fills → `OutOfMemoryError` → crash |
| Drop silently | You lose archive data and don't know it |
| **Backpressure** | Producer is *forced* to slow down to match the consumer's speed |

Backpressure is option 3. It makes the slowness **visible and felt** by whoever is producing work, instead of hiding it in a growing buffer or a silent drop.

## How `ApiRequestResponseArchiver` does backpressure

Look at `submitArchiveTask`:

```java
if (this.writeQueue.offer(archiveTask)) {
  // queue had room → fast path, return immediately
} else {
  log.info("Queue offer failed ...");
  this.writeQueue.put(archiveTask);   // ← THIS is backpressure
}
```

The queue has a **fixed size** (`myservice.api.archive.s3.queue.size`). When it's full:

1. `offer()` returns `false` (can't fit).
2. `put()` is called instead. `put()` **blocks** — the calling thread sits and waits until a worker pulls something off the queue and frees a slot.
3. Only then does `put()` return and the HTTP handler continue.

The HTTP thread is now **deliberately slowed down** to the exact speed that S3 uploads can drain the queue. That's backpressure.

## Why this is good

- **No memory blow-up.** Queue size is capped.
- **No silent data loss.** Every task eventually gets processed.
- **Slowness propagates honestly.** If S3 is slow, the API gets slow. That's a signal — dashboards light up, alerts fire, you know there's a problem. Compare with "S3 is slow but API is fast and we're silently dropping audit logs" — much worse.

## Why this is also uncomfortable

Backpressure means **the slowness travels upstream**. In the archiver:

- S3 slow → archive queue fills
- Queue full → `put()` blocks the HTTP thread
- HTTP thread blocked → API latency goes up
- API slow → client retries → more load → worse

So backpressure is a **design choice with teeth**. The code is saying "I'd rather my API slow down than lose archive data." The alternative would be to drop archive tasks when the queue is full (call `offer()` and if it fails, log and give up). That keeps the API fast but loses data.

The current code picked "slow down the API" — which is often the right call for audit/compliance data where losing records is worse than a latency spike.

## Why this matters for the `ThreadPoolExecutor` refactor

`ThreadPoolExecutor` has several built-in rejection policies for when the queue is full:

| Built-in policy | Behavior | Matches current archiver behavior? |
|---|---|---|
| `AbortPolicy` (default) | Throw `RejectedExecutionException` | No — would break callers |
| `DiscardPolicy` | Silently drop the task | No — loses archive data |
| `DiscardOldestPolicy` | Drop the oldest queued task | No — loses archive data |
| `CallerRunsPolicy` | Run task on calling thread (HTTP thread!) | No — HTTP thread would do the slow S3 upload |

None of them match "log + blocking put". So the refactor needs a small custom `RejectedExecutionHandler` that does exactly that. Two lines of behavior, preserved exactly.

## Backpressure appears everywhere

Once you know the word, you see it everywhere:

- **`StageExecutor` timeouts** — a form of backpressure: "I can't wait longer than X, so fail fast."
- **Feign client connection pools** — bounded pools that block when exhausted.
- **Database connection pools (HikariCP)** — blocks when all connections are busy.
- **Kafka consumers with `max.poll.records`** — bounded fetch size.

All the same idea: **cap how much work is in flight, and make overflow visible**.
