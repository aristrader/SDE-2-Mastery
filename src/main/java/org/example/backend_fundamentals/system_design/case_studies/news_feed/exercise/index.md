---
order: 10
search: false
---

# News Feed Exercise

## Exercise: news-feed-hld - Design Feed Publishing And Retrieval

### Goal

Practice the HLD answer for a news feed system with publish, fanout, cache, and retrieval flows.

### Task

Design a reverse-chronological news feed for 10M DAU where users can publish text/media posts and read friends' posts.

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
- Include idempotent fanout writes.
- Include stale-feed behavior when fanout lags.
- Mention cache miss fallback to durable feed-entry storage or read-time merge.
