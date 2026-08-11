---
order: 20
search: false
---

# Design Top-K Heavy Hitters

## Requirements

Return the top 100 most-viewed videos globally for fixed hourly, daily, monthly, and all-time windows, with `K <= 1,000`. A view should appear in the next ranking within one minute. Ranking reads should take tens of milliseconds. The source video platform already writes a durable `VideoViewed` event stream; the ranking system consumes it. Results are exact for the base answer.

The key design choice is to calculate rankings continuously, not when a client calls the API.

## Derivation

The simple starting point is one consumer updating a relational counter per video and a `count` index serving all-time top K. It proves the API but cannot sustain high-rate indexed writes. Adding hourly buckets supports time windows, but a monthly request now scans, groups, and sorts a large range. Cache alone only hides that query until an expiry creates a slow miss.

The final design fixes each bottleneck in order: partition by video ID, aggregate events before writing, materialize the fixed windows clients are allowed to request, and prepublish a small ranked snapshot to cache. This is the explanation to give before drawing the components below.

## Components

| Component | Responsibility |
| --- | --- |
| Event stream | Durable, replayable `VideoViewed(eventId, videoId, eventTime)` log, partitioned by `videoId`. |
| Stream aggregators | Deduplicate as required, assign events to event-time windows, and keep per-video counts in checkpointed state. |
| Aggregate store | Holds durable materialized counts and immutable ranking snapshots. It is recoverable derived state, not the raw-event authority. |
| Candidate merger | Merges local top candidates into one exact global top-K list for each window. |
| Snapshot publisher | Writes the new ranked list to the aggregate store and Redis/cache on the freshness schedule. |
| Ranking API | Stateless read service that validates `window`, `limit`, and dimensions, then reads the snapshot/cache. |

## Event path

```text
Video service -> VideoViewed topic (key = videoId)
              -> stream task that owns videoId
              -> event-time window count + checkpoint
              -> local top M candidates
              -> global candidate merger
              -> TopKSnapshot + Redis
```

The stream processor retains state like:

```text
(metric=views, windowStart=10:00, region=global, videoId=42) -> 183421
```

Maintain counts for each supported fixed query shape rather than deriving a month by scanning hourly data at read time. For every event, update the current hour, day, month, and all-time aggregate for that video. For each fixed window, the merger produces:

```text
(metric=views, windowStart=10:00, region=global) ->
  [(1, video_42, 183421), (2, video_99, 175820), ...]
```

The API returns that second record. It does not query every count, group, sort, or invoke a stream job.

## Exact distributed top K

Each item has one aggregation owner because the source stream key is `videoId`. A shard can retain a min-heap of its best `M` values and periodically emit that compact candidate set. The global merger applies another min-heap of size `K` to the union.

With single-owner item counts, local top `K` is mathematically sufficient for exact global top `K`; a local rank `K+1` item already has at least K items above it. In practice, use `M > K` to give room for dimensions, partial aggregate merges, and operational simplicity.

## Time handling

Start with tumbling windows, such as `[10:00, 11:00)`. A watermark plus allowed lateness prevents a slightly delayed event from being placed in the wrong hour. Example policy: amend a window until one minute after it ends; route later events to a late-event stream and monitor them.

For a required 60-minute sliding window refreshed every minute:

```text
currentCount = previousCount + counts[new minute] - counts[expired minute]
```

Store minute buckets long enough to subtract the expired one. This is a valid deep dive, not the base design.

## Read path and failure behavior

```text
Client -> load balancer -> Ranking API -> Redis snapshot -> response
                                      -> durable snapshot only when cache is unavailable
```

Publishing a snapshot before the previous one expires avoids read-through cache misses. If publishing breaks, return the previous snapshot and include/record its age rather than allowing every request to run a heavy aggregate query.

The raw event stream remains retained. A task failure restores its checkpoint and replays the tail; a bad aggregation release rebuilds all derived state from the raw stream. Duplicate events are handled according to the upstream delivery guarantee with event-ID dedupe or atomic state-plus-offset checkpointing.

## Optional approximation

A Count-Min Sketch replaces huge exact count state with hashed counters. It estimates the count for an item but cannot list item IDs, so it requires a separate bounded candidate heap. Sketch collisions overestimate counts. Use it for tolerant trending features, never silently for exact reporting.

## Quick recall

**Q. Where is the source of truth?**
A. The retained raw event log. Counts, snapshots, and cache entries can be rebuilt from it.

**Q. Why keep a durable snapshot as well as Redis?**
A. Redis is the fast serving layer; the durable snapshot rehydrates it after cache loss and supports a controlled stale fallback.

**Q. What changes for a sliding window?**
A. Keep fine-grained buckets and add the newest bucket while subtracting the bucket that expires.
