---
order: 20
search: false
---

# Design a News Feed System

## Agreed problem

Design a reverse-chronological home feed like Facebook, Twitter, or Instagram. The answer must explain how a post
becomes visible to followers without making either common feed reads or hot-author publishes unboundedly expensive.

This tab is the compact design reference. Read the parent page first for the baseline, the pressure that motivates each
component, and the user-visible failure behavior.

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
- High availability for reads; cache loss must have a durable fallback.
- A post is durable when accepted; eventual consistency is acceptable only for feed propagation.
- A later delete, block, mute, or privacy change must be hidden before asynchronous cleanup completes.

## Design decisions

| Decision | Why |
| --- | --- |
| A post is accepted after `post` and `post_outbox` commit together | A process crash cannot leave a durable post with no recoverable fanout event. |
| Normal authors use asynchronous fanout-on-write | A reader gets a prepared, viewer-partitioned candidate list. |
| High-follower authors use bounded fanout-on-read | One publish does not create a follower-count-sized write storm. |
| `feed_entry` is durable and cache holds only recent IDs | Cache speeds up reads but does not become the sole source of a feed. |
| The read path filters current visibility | Fanout uses an earlier relationship snapshot; delete and privacy cannot wait for repair. |

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

Use an opaque cursor containing the last returned `(sort_key, post_id)`. New posts arriving at the front shift page
offsets, so page-number pagination can duplicate or skip an item while a user scrolls.

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

post_outbox(
  event_id uuid primary key,
  post_id bigint,
  event_type varchar(32),
  created_at timestamp
)
```

`post` is authoritative content. `feed_entry` is a lightweight, per-viewer candidate pointer; it is not a copied post
body. Cache stores hot feed IDs and hot post/user/action/counter data. `post_outbox` is written with `post` so a post
that commits cannot be silently missed by fanout; replayed events require an idempotent `(user_id, post_id)` entry
write.

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
| Fanout workers | Idempotently materialize durable feed entries, then update recent-ID cache |
| Feed-entry store | Durable materialized IDs for normal-author posts |
| News feed service | Fetch/merge feed IDs, filter visibility, and hydrate feed items |
| Caches | Feed IDs, post objects, users, actions, counters |
| CDN | Serve image/video bytes |

## Publish flow

1. The client sends `POST /v1/me/posts` with an idempotency key; the edge authenticates and rate-limits it.
2. The post service commits `post` and `post_outbox(event_id)` in one transaction, then returns success.
3. An outbox relay publishes committed events. Duplicate delivery is expected after a retry.
4. Fanout evaluates graph edges and the current visibility policy.
5. Normal authors produce viewer-partitioned jobs. Each worker upserts `(viewer_id, post_id, sort_key)` and updates
   the recent-ID cache.
6. A high-follower author remains in the author's recent-post source; no entry is written for every follower.
7. Notifications can be sent after acceptance, but they are only a hint and never proof of feed visibility.

## Retrieval flow

1. The feed service reads recent post IDs from the viewer's cache or, on a miss/deep scroll, the durable feed-entry
   range.
2. It pulls a bounded recent window for followed high-follower authors, merges it with materialized IDs, deduplicates,
   and orders candidates by `(sort_key, post_id)`.
3. It filters delete, block, mute, and privacy changes against authoritative state before returning an item.
4. It hydrates the visible page with batched post, author/profile, action/counter, and media-metadata reads.
5. It returns media URLs rather than bytes and produces the next cursor from the last visible item.

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

This is the default interview answer because it protects read latency for most users and prevents hot accounts from
flooding the system. The threshold is a capacity policy, not a magic number: it exists to cap per-post write work and
queue lag.

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
The cache is not the only feed copy: its fallback is durable `feed_entry` plus read-time merge for high-follower posts.

## Deep dives interviewers may ask

### Why two caches: feed cache and post cache?

Feed cache answers: "Which post IDs should this user see?"

Post cache answers: "What is the content of this post ID?"

This avoids duplicating full post objects into thousands of user feeds.

### What if fanout is delayed or replayed?

Return the currently available feed and let it be briefly stale. The author already has a durable post and outbox
event. Workers later drain the backlog; a replay is safe because feed-entry materialization is idempotent.

### What if a user mutes someone?

Apply mute/privacy filters before fanout. If the setting changes after fanout, filter on read against authoritative
visibility state and asynchronously remove stale feed entries. The read-time filter is the correctness safety net.

### What if cache misses?

Read from the feed-entry store or rebuild with read-time merge. Do not make cache the only source of truth.

### What if a post is deleted after fanout?

Mark it unavailable in authoritative post state. The read path suppresses it immediately, while asynchronous repair
removes old feed entries and cache IDs. Cleanup improves cost; the read-time check provides correctness.

## Failure modes

| Failure | Handling |
| --- | --- |
| DB write fails | Do not publish/fanout the post |
| Post written but fanout event not yet published | Relay the committed outbox event |
| Fanout worker crash | Queue retry; idempotent durable feed-entry write, then cache repair |
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
