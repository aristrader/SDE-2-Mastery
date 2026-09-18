---
order: 20
search: false
---

# Design a URL Shortener

## Agreed scope

| Question | Answer | Decision |
| --- | --- | --- |
| Core operations? | Create generated alias; redirect by code | Separate create correctness from redirect latency. |
| Scale? | 100M creates/day; 10:1 redirects | Cache redirect mappings and partition durable storage. |
| Are mappings mutable? | No | Cache entries are safe until expiry/eviction. |
| Is analytics required? | Yes, but not in redirect latency | Publish click events after the response. |
| Custom aliases, updates, malware scan? | Follow-ups | Do not complicate the base answer. |

## Requirements

- Create a collision-safe short code for a valid long URL.
- Redirect an active, unexpired code with low latency.
- Keep mappings durable and redirects highly available.
- Return a stated error for unknown or disabled/expired mappings.
- Prevent abusive create traffic without making analytics a redirect dependency.

## Capacity decisions

```text
100M creates/day ≈ 1,160 creates/sec
10:1 read/write ≈ 11,600 redirects/sec
10 years ≈ 365B mappings
62^7 ≈ 3.5T generated codes
```

Seven base62 characters cover the retained mappings. The traffic makes cache-first redirects and
hash-partitioned durable mappings necessary; it does not require an elaborate analytics system on the hot path.

## APIs and data

```text
POST /v1/links { longUrl } -> 201 { shortUrl, code }
GET /{code} -> 301/302 with Location header
```

```text
url_mapping(
  short_code primary key,
  id unique,
  long_url,
  status,
  expires_at,
  created_at
)
```

`short_code` is the point-lookup and partition key. A normalized URL hash is optional only if product behavior
requires repeated creates to return the same alias. Index `expires_at` for asynchronous cleanup; index the
normalized URL hash only when dedupe is enabled.

## Architecture

![URL shortener architecture](../assets/url-shortener-architecture.svg)

The mapping store is authoritative. Cache, the ID generator, and the analytics queue have deliberately
different contracts: cache accelerates reads, the generator enables only new links, and analytics is optional
for redirect success.

## Create path: durable uniqueness

![URL shortener create and redirect flow](../assets/url-shortener-flows.svg)

1. Validate scheme, length, and caller quota.
2. Allocate a unique numeric ID from a distributed generator or a safely leased range.
3. Base62-encode the ID and insert the mapping.
4. Let the unique code constraint reject any impossible generator defect before returning the alias.

Base62 is an encoding, not a uniqueness mechanism. Random codes are a valid alternative when enumeration is a
concern, but they require insert-and-retry collision handling.

## Redirect path: fast but recoverable

1. Read `code -> destination/status/expiry` from cache. A short negative-cache TTL may protect the mapping
   store from repeated unknown-code probes, but a long one can preserve a temporary miss.
2. On a miss, perform one indexed read from the code's mapping partition.
3. Backfill cache only for active mappings, then return the redirect.
4. Publish a click event after the response; unknown mappings return `404`, while disabled/expired mappings can
   use `410` if that distinction is required.

On cache failure, the service falls back to the mapping store. On a mapping-partition failure, serve cached hot
links when policy permits, otherwise fail over to a replica or return a retryable response. Cache must not make
an expired or disabled mapping authoritative forever.

## Two trade-offs worth saying aloud

| Decision | Chosen answer | Cost / boundary |
| --- | --- | --- |
| Code allocation | Unique ID + base62 | Simple writes; use permutation/random codes if enumeration matters. |
| Redirect behavior | Temporary/permanent status matches target semantics | Cache policy controls repeat lookup and analytics visibility separately. |

`301` represents a permanent target and `302` a temporary one. Neither alone promises or forbids repeat
lookups: state the cache policy separately when analytics or per-click controls matter. [RFC 9110](https://www.rfc-editor.org/rfc/rfc9110.html)

## Failure and abuse handling

| Condition | Result |
| --- | --- |
| ID generator unavailable | New creates retry/fail; redirects continue. |
| Analytics queue unavailable | Redirect continues; retry, buffer, or drop event by policy. |
| Viral cache miss | Coalesce fills or limit retries to protect the mapping partition. |
| Create abuse | Enforce IP/user/API-key quota before ID allocation. |
| Moderation/expiry | Status or expiry check blocks redirect and prevents cache backfill. |

## Explicit follow-ups

- Custom aliases require atomic reservation, ownership/quota checks, and moderation.
- Destination updates or deletion require status/versioning plus cache invalidation; immutable generated links
  avoid that invalidation path in the base design.
- Dedupe requires a carefully normalized long-URL hash and a defined product rule for whether two callers share
  one alias.
- Malware/phishing scanning can be asynchronous after creation, but the product must define whether a pending
  scan blocks first redirect.
- Analytics workers aggregate queued events into a separate store; they are never queried by the redirect path.

## Quick recall

**Q. Why keep a database uniqueness constraint after using an ID generator?**

A. It is the final durable guard against a generator or caller defect.

**Q. What is the redirect correctness source?**

A. The durable mapping record, not cache or analytics.

**Q. Why partition by code hash?**

A. The dominant operation is a point lookup, and hashing spreads traffic and mappings.
