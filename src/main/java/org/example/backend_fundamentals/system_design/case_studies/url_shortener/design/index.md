---
order: 20
search: false
---

# Design a URL Shortener

## Problem

Design a URL shortening service like TinyURL.

Given a long URL:

```text
https://www.systeminterview.com/q=chatsystem&c=loggedin&v=v3&l=long
```

Return a shorter alias:

```text
https://tinyurl.com/y7keocwj
```

When the short URL is opened, redirect to the original long URL.

## Clarify scope

Ask:

- How many URLs are created per day?
- What is the expected read-to-write ratio?
- What characters are allowed in the short code?
- Should short URLs expire?
- Can short URLs be deleted or updated?
- Do we need custom aliases?
- Do we need click analytics?
- Should redirects be permanent (`301`) or temporary (`302`)?

Translate answers into design choices:

| Answer | Design consequence |
|--------|--------------------|
| Expiration required | Add `expires_at`, cleanup job, and `410 Gone` option |
| Custom aliases required | Add alias reservation, uniqueness checks, and abuse moderation |
| Analytics required | Prefer `302` and async click-event pipeline |
| Delete/update required | Add status/versioning and cache invalidation |
| Very high write volume | Pay more attention to ID generator throughput and DB write sharding |

Reasonable interview assumptions:

```text
100M new URLs/day
10:1 read-to-write ratio
Short code uses 0-9, a-z, A-Z
No delete/update/custom alias in MVP
High availability and low-latency redirect path
```

## Functional requirements

- Create a short URL for a given long URL.
- Redirect a short URL to the original long URL.
- Return `404` for invalid short codes.
- Optionally dedupe repeated shortening of the same long URL.
- Optionally record click analytics without slowing redirects.

## Non-functional requirements

- High availability for redirects.
- Low redirect latency.
- Scalable reads and writes.
- Collision-free short-code generation.
- Durable mapping storage.
- Abuse protection on URL creation.

## Interview mental model

Separate the system into three concerns:

| Concern | Main question | Main component |
|---------|---------------|----------------|
| Naming | How do we create a compact unique code? | Distributed ID generator + base62 encoder |
| Lookup | How do we map code back to URL quickly? | Cache + URL mapping DB |
| Side effects | How do we track clicks without slowing redirects? | Async event pipeline |

This prevents a common weak answer: mixing analytics, validation, DB writes, and redirect latency into one request path.

## Back-of-envelope estimation

```text
New URLs/day = 100M
Writes/sec = 100M / 86,400 ~= 1,160
Read:write = 10:1
Reads/sec ~= 11,600
10-year records = 100M * 365 * 10 = 365B
Average long URL = 100 bytes
Raw long-URL storage ~= 36.5 TB before metadata/indexes/replication
```

Short-code capacity:

| Code length | Capacity |
|-------------|----------|
| 1 | `62^1 = 62` |
| 2 | `62^2 = 3,844` |
| 3 | `62^3 = 238,328` |
| 4 | `62^4 = 14,776,336` |
| 5 | `62^5 = 916,132,832` |
| 6 | `62^6 = 56,800,235,584` |
| 7 | `62^7 = 3,521,614,606,208` |

Seven base62 characters are enough for 365B URLs.

Capacity rule:

```text
Find smallest n where alphabet_size^n >= total_records
For base62: 62^7 ~= 3.5T > 365B
```

## API sketch

Create:

```text
POST /api/v1/urls
Content-Type: application/json

{ "longUrl": "https://example.com/a/very/long/path" }

201 Created
{ "shortUrl": "https://tinyurl.com/zn9edcu" }
```

Redirect:

```text
GET /{shortCode}

302 Found
Location: https://example.com/a/very/long/path
```

Use `302` when analytics matters. Use `301` only if reducing shortener load matters more than tracking every click.

## Data model

Main table:

```text
url_mapping(
  id bigint primary key,
  short_code varchar(16) unique not null,
  long_url text not null,
  long_url_hash char(64),
  created_at timestamp not null,
  expires_at timestamp null,
  status varchar(20) not null
)
```

Indexes:

| Index | Why |
|-------|-----|
| `unique(short_code)` | redirect lookup and collision safety |
| `index(long_url_hash)` | optional dedupe lookup |
| `index(expires_at)` | cleanup expired links |

## High-level architecture

![URL shortener architecture](../assets/url-shortener-architecture.svg)

```text
Client
  -> Load balancer
  -> URL service
  -> Cache
  -> URL mapping DB
  -> Distributed ID generator
```

Web/API servers are stateless, so they scale horizontally.

Component responsibilities:

| Component | Responsibility | Interview caveat |
|-----------|----------------|------------------|
| Load balancer | Distribute requests across stateless URL services | No sticky session needed |
| URL service | Validate, encode, lookup, redirect | Keep it stateless |
| Cache | Serve hot redirect mappings | Cache hit should avoid DB |
| URL DB | Durable source of truth | Unique constraint on `short_code` |
| ID generator | Produce globally unique numeric IDs | Existing redirects do not depend on it |
| Analytics queue | Buffer click events | Redirect should not block on analytics |

## Short-code generation

### Option 1: hash + collision resolution

Hash the long URL with something like CRC32/MD5/SHA-1, take the first 7 characters, and check whether that short code is already used.

If there is a collision, retry with a salt or predefined suffix until a free code is found.

Pros:

- fixed code length
- no central ID generator
- less predictable than sequential IDs

Cons:

- collisions are possible
- write path needs existence checks
- DB checks can be expensive at high traffic; Bloom filters can reduce unnecessary DB hits but add false positives

### Option 2: base62 of unique ID

Generate a globally unique numeric ID and convert it to base62:

```text
11157 decimal -> [2, 55, 59] -> 2TX
```

Pros:

- no collision if ID generator is correct
- simple insert path
- easy to explain

Cons:

- needs a distributed ID generator
- sequential IDs can make short URLs guessable

Use this in the primary design. The reusable notes live at `system_design/concepts/distributed_id_generation/`.

Decision rule:

| Requirement | Prefer |
|-------------|--------|
| Simpler creation path and no collisions | Base62 over unique ID |
| Fixed-length opaque code without sequential guessability | Hash + collision resolution, or random code with uniqueness check |
| Need non-guessable codes with base62 ID | Add randomization/encryption/id permutation before base62, or use random aliases |

## Create flow

![URL shortener flows](../assets/url-shortener-flows.svg)

1. Client submits `longUrl`.
2. Service validates scheme, length, and optional blocklist rules.
3. Service checks `long_url_hash` if dedupe is required.
4. If existing mapping is found, return existing short URL.
5. Otherwise, get a new ID from the distributed ID generator.
6. Convert ID to base62.
7. Insert row into `url_mapping`.
8. Return short URL.

Make the DB unique constraint on `short_code` the final safety net even when the ID generator is expected to be unique.

Create-path correctness checks:

- reject invalid URL schemes
- normalize URL carefully if dedupe matters
- enforce max URL length
- rate-limit by IP/user/API key
- make `short_code` unique in the DB
- return the same mapping for repeated creates only if that is a stated product requirement

## Redirect flow

1. Client requests `GET /{shortCode}`.
2. Load balancer routes to any stateless web server.
3. Service checks cache for `shortCode`.
4. On hit, return redirect.
5. On miss, query DB by `short_code`.
6. If missing or disabled, return `404` or `410`.
7. If found, populate cache and return redirect.
8. Emit click analytics asynchronously.

Do not put analytics writes on the redirect critical path. A queue/stream lets redirect success stay independent of analytics lag.

Redirect response choice:

| Choice | Effect |
|--------|--------|
| `301` | Browser may cache permanently; lower load on shortener; weaker per-click analytics |
| `302` | Browser calls shortener each time; better analytics and abuse tracking; higher service load |

Default to `302` unless the interviewer says analytics is not important.

## Deep dives

### Cache strategy

Cache `shortCode -> longUrl` because reads dominate writes. Use TTLs and an eviction policy.

For invalid short codes, either do not cache misses or cache negative results with a very short TTL. Long negative caching can turn temporary data lag into persistent wrong `404`s.

### Sharding

Point lookups make sharding manageable:

- shard by `short_code` hash for redirect distribution
- shard by `id` if write locality and ID ranges matter
- replicate shards for availability

Avoid range-sharding by sequential ID without thinking through hot writes; newest IDs may all hit the same shard.

Shard-key options:

| Shard key | Good | Risk |
|-----------|------|------|
| Hash of `short_code` | Even redirect distribution | Range scans by time are harder |
| Hash of `id` | Simple with generated IDs | Sequential IDs need hashing before shard choice |
| Tenant/user | Useful for ownership dashboards | Large tenants can become hot |

### Abuse and security

- Rate-limit URL creation by IP/user/API key.
- Block dangerous schemes like `javascript:`.
- Add malware/phishing checks if product scope requires it.
- Consider making generated IDs non-obvious if enumeration is a concern.
- Allow admin/user disable flow for abusive URLs in a production version.

### Analytics

Click analytics can track:

- click count by short code
- timestamp
- referrer
- coarse geography
- device/browser

Write analytics asynchronously:

```text
Redirect service -> click event queue -> analytics workers -> analytics store
```

## Failure modes

| Failure | Handling |
|---------|----------|
| Cache down | Fall back to DB; expect higher latency/load |
| DB read shard down | Use replica/failover; cache can absorb some hot links |
| ID generator down | Creation fails or degrades; redirects should still work |
| Analytics pipeline down | Drop/defer analytics; redirects continue |
| Abuse spike on create endpoint | Rate limiter rejects excessive creation requests |

## Interview wrap-up

End with this summary:

```text
The critical path is redirect: shortCode -> cache/DB -> 302.
The correctness path is creation: unique ID -> base62 -> unique DB row.
Analytics and abuse checks exist, but analytics is asynchronous and abuse protection sits mostly on creation.
```

## Quick recall

**Q. What should you clarify first?**  
A. Traffic, read/write ratio, code length/alphabet, expiration/delete/update, analytics, custom aliases.

**Q. Why does base62 need a distributed ID generator?**  
A. Base62 only encodes a number; uniqueness comes from the generated numeric ID.

**Q. Why is redirect path cache-heavy?**  
A. Short URLs are read many more times than they are created.

**Q. `301` vs `302`?**  
A. `301` reduces future shortener traffic through browser caching; `302` preserves analytics because each click returns to the shortener.

**Q. What is the simplest DB lookup?**  
A. `short_code -> long_url` point lookup with a unique index.

**Q. What should not block redirects?**  
A. Analytics writes, malware rechecks, and other non-critical side effects.
