---
order: 20
search: false
---

# Design a News Feed System

## Problem

Design a news feed system like Facebook feed, Twitter timeline, or Instagram home feed.

## Clarify scope

Ask:

- Is it web, mobile, or both?
- Is the relationship model friends, follows, or both?
- Is feed order reverse chronological or ranked?
- How many DAU?
- How many friends/followers can a user have?
- Can posts contain images and videos?
- Do we need likes, replies, notifications, and privacy controls?

Reasonable assumptions:

```text
10M DAU
Reverse chronological feed
5000 friends max per user
Text, image, and video posts
No ranking ML in the base design
```

## Functional requirements

- Publish a post.
- Retrieve a user's feed.
- Include text and media references.
- Respect authentication, privacy, mute/block/share settings.
- Show basic metadata: author, profile image, post content, media, likes/replies.

## Non-functional requirements

- Low-latency feed reads.
- Durable posts.
- Scalable fanout.
- High availability.
- Cache-heavy design.
- Eventual consistency is acceptable for feed propagation.

## API sketch

Publish:

```text
POST /v1/me/feed

{
  "content": "Hello",
  "mediaIds": ["image-123"]
}
```

Read:

```text
GET /v1/me/feed?cursor=2026-08-04T10:00:00Z_987&limit=20
```

Use cursor pagination because new posts can arrive while a user scrolls.

## Data model

```text
post(
  post_id bigint primary key,
  author_id bigint,
  content text,
  media_ids json,
  created_at timestamp
)

friend_edge(
  user_id bigint,
  friend_id bigint,
  created_at timestamp,
  primary key(user_id, friend_id)
)

feed_entry(
  user_id bigint,
  post_id bigint,
  author_id bigint,
  created_at timestamp,
  primary key(user_id, created_at, post_id)
)
```

The DB is the source of truth. Cache stores hot feed IDs and hot post/user/action/counter data.

## Architecture

![News feed architecture](../assets/news-feed-architecture.svg)

Component responsibilities:

| Component | Responsibility |
| --- | --- |
| Web servers | Auth, rate limiting, route requests |
| Post service | Validate and persist posts |
| Fanout service | Find recipients and create fanout jobs |
| Graph service/store | Friend/follower relationships |
| Fanout queue | Buffer feed-entry writes |
| Fanout workers | Append post IDs to user feed caches |
| News feed service | Fetch feed IDs and hydrate feed items |
| Caches | Feed IDs, post objects, users, actions, counters |
| CDN | Serve image/video bytes |

## Publish flow

1. User posts content.
2. Web server authenticates and rate-limits.
3. Post service stores post in DB.
4. Post service writes post object to content cache.
5. Fanout service reads friends/followers from graph store.
6. Fanout service filters recipients by privacy/mute/share settings.
7. Fanout service sends fanout jobs to queue.
8. Fanout workers append `postId` to each recipient's news feed cache.
9. Notification service may send push notifications.

## Retrieval flow

1. User requests feed.
2. News feed service reads post IDs from news feed cache.
3. Service fetches post objects from post cache.
4. Service fetches author/profile data from user cache.
5. Service fetches action/counter data.
6. Service returns hydrated JSON.

Hydration means converting:

```text
[postId1, postId2, postId3]
```

into:

```text
[
  { post, author, mediaUrls, viewerActions, counters },
  ...
]
```

## Fanout deep dive

![Fanout models](../assets/news-feed-fanout-models.svg)

### Fanout on write

When Alice posts, push Alice's `postId` into every friend's feed cache immediately.

Good:

- fast feed reads
- feed is ready before the user opens the app

Bad:

- expensive for users with many followers
- wasted work for inactive users

### Fanout on read

When Bob opens the app, pull recent posts from Bob's friends and merge them then.

Good:

- avoids wasted work for inactive users
- avoids celebrity hot-write explosion

Bad:

- slower reads
- expensive if the user follows many active accounts

### Hybrid

Use fanout-on-write for normal users and fanout-on-read for celebrities/high-follower users.

```text
normal author post -> push postId into recipients' feed caches
celebrity author post -> store post normally, merge into followers' feeds when they read
```

This is the default interview answer because it protects read latency for most users and prevents hot accounts from flooding the system.

## Cache layers

| Cache | Key shape | Value |
| --- | --- | --- |
| News feed | `feed:{userId}` | recent ordered `postId`s |
| Content/post | `post:{postId}` | post content and media IDs |
| User | `user:{userId}` | name, profile image, account metadata |
| Social graph | `friends:{userId}` | friend/follower IDs |
| Action | `action:{userId}:{postId}` | liked/replied/saved state |
| Counter | `counter:{postId}` | like/reply/share counts |

Keep feed cache as IDs so updates to post/user/action/counter data do not require rewriting every cached feed entry.

## Deep dives interviewers may ask

### Why two caches: feed cache and post cache?

Feed cache answers: "Which post IDs should this user see?"

Post cache answers: "What is the content of this post ID?"

This avoids duplicating full post objects into thousands of user feeds.

### What if fanout is delayed?

Return the feed currently in cache and let it be slightly stale. For important posts, the client can refresh later or the system can show a "new posts available" indicator.

### What if a user mutes someone?

Apply mute/privacy filters before fanout. If the setting changes after fanout, either filter on read or asynchronously remove stale feed entries. For interviews, filtering on read is the simpler correctness safety net.

### What if cache misses?

Read from the feed-entry store or rebuild with read-time merge. Do not make cache the only source of truth.

## Failure modes

| Failure | Handling |
| --- | --- |
| DB write fails | Do not publish/fanout the post |
| Post written but fanout event lost | Use outbox or durable event log |
| Fanout worker crash | Queue retry; idempotent append |
| Duplicate feed entries | Unique key or set semantics on `(userId, postId)` |
| Celebrity post overload | Pull celebrity posts at read time |
| Feed cache evicted | Rebuild from durable feed-entry store |
| CDN slow | Feed metadata still loads; media can lazy-load |

## Quick recall

**Q. Why is feed read path optimized heavily?**  
A. Users read/refresh feeds far more often than they publish posts.

**Q. What does fanout mean?**  
A. Delivering a new post ID to the feeds of friends/followers.

**Q. Why use hybrid fanout?**  
A. It gives fast reads for normal users while avoiding huge write bursts for celebrity accounts.

**Q. Why keep web servers stateless?**  
A. Any request can go to any server, so horizontal scaling and failover are simpler.

**Q. Where are images/videos stored?**  
A. In CDN/object storage; feed responses carry URLs/metadata, not media bytes.
