---
order: 10
search: false
---

# Top-K Heavy Hitters Exercise

## Exercise: trending-videos-hld - Design a Top-K Ranking Service

### Goal

Practice an SDE-2 system-design answer for the top 100 trending videos from a high-volume view-event stream.

### Task

Assume an existing Kafka topic emits `VideoViewed(eventId, videoId, eventTime)` at 500K events/second. Design the ranking service. Clarify the window type and exactness first, then draw the write and read paths.

### Acceptance criteria

- Consume the existing event stream rather than redesigning the video-view endpoint.
- Partition aggregation by video ID and explain why it avoids distributed counter updates for one video.
- Explain fixed-window aggregation, a freshness target, late events, and checkpoint/replay recovery.
- Produce a global top 100 from local candidates and serve a precomputed snapshot from cache.
- State why a database scan on cache miss is dangerous.
- Keep Count-Min Sketch and sliding windows as explicit follow-ups, not the starting design.
