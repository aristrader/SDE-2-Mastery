---
order: 10
search: false
---

# News Feed Exercise

## Exercise: news-feed-hld - Design Feed Publishing And Retrieval

### Goal

Practice the HLD answer for a news feed system with publish, fanout, cache, and retrieval flows.

## Timed mock

Set a 45-minute timer. Do not read the reference design until you have drawn the write and read paths.

| Time | What to produce |
|---|---|
| 0-5 min | Requirements and read/write consistency expectations |
| 5-8 min | DAU, read/write ratio, fanout pressure, and storage estimate |
| 8-13 min | Publish/read APIs and post/feed-entry model |
| 13-25 min | Publish path, graph lookup, fanout workers, and media delivery |
| 25-40 min | Deep dive: celebrity fanout and cache/read-path design |
| 40-45 min | Lag, retry, deletion, and trade-offs |

### Task

Design a reverse-chronological news feed for 10M DAU where users can publish text/media posts and read friends' posts.

Begin by describing the smallest design that merges recent posts from followed authors on every read. Then identify
the reader-latency pressure that moves normal authors to fanout-on-write and the hot-author pressure that requires a
hybrid read-time merge. Do not start with a list of infrastructure products.

Cover:

- publish API
- retrieval API
- post service
- fanout service
- graph lookup
- fanout queue and workers
- news feed cache
- post/user/action/counter caches
- CDN for media
- fanout-on-write vs fanout-on-read vs hybrid

### Acceptance criteria

- Explain why news feed cache stores post IDs, not full objects.
- Explain why post cache and news feed cache are separate.
- Include a hybrid fanout strategy for high-follower users.
- Include rate limiting on posting.
- Commit the post and a fanout outbox event together; make replayed fanout writes idempotent.
- Include stale-feed behavior when fanout lags.
- Mention cache-miss fallback to durable feed-entry storage plus read-time merge for high-follower authors.
- Explain why delete, block, mute, and privacy changes still need a read-time visibility check.
- State what the author sees after acceptance and what a follower sees while fanout is delayed.

## Interviewer follow-ups

1. Why cannot you fan out every celebrity post to all followers synchronously?
2. Why does a feed cache store ordered post IDs rather than full post bodies?
3. What happens when fanout workers lag for ten minutes?
4. How do delete/privacy changes reach already materialized feeds?
5. How does the system avoid losing a committed post if the process dies before publishing to the fanout queue?
6. Why is a queue not itself the durable acceptance boundary for a post?
7. A cache entry says a post is visible but the author has now blocked the viewer. Which source wins and why?
8. How do you avoid duplicated or skipped results when new posts arrive during pagination?

## Self-review

| Signal | Score 0-2 |
|---|---|
| Fanout-on-write, fanout-on-read, and hybrid have clear triggers | |
| The publish and read paths are independently understandable | |
| Cache layers have distinct data and invalidation rules | |
| Fanout is idempotent and handles lag/retries | |
| Celebrity, delete, and stale-feed trade-offs are explicit | |

**Target:** at least `7/10`. Then compare with [Design](/system_design/case_studies/news_feed/design/).
