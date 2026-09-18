---
order: 30
---

# URL Shortener

## Interview scope

Design a TinyURL-style service that creates a short alias for a long URL and redirects a visitor to the saved
destination. The base answer covers generated aliases, durable mappings, cache-first redirects, async click
analytics, and expiry/status checks. Custom aliases, changing a destination, malware scanning, and dashboards
are follow-ups rather than assumed requirements.

| Clarification | Agreed answer | Design consequence |
| --- | --- | --- |
| What must be fast? | Redirects | Keep lookup cache-first; analytics never blocks a redirect. |
| What must be unique? | Every generated code | Allocate a unique ID before base62 encoding; DB uniqueness remains the final guard. |
| How much traffic? | 100M new links/day, 10:1 reads:writes | Scale cache and point lookups independently from creation. |
| Can a link change? | No in the base design | A mapping is immutable, making cached entries safe until expiry/eviction. |
| Is every click observable? | Only if cache policy allows it | Redirect status and cache policy are separate decisions. |

## Numbers that change the design

| Estimate | Result | Decision it changes |
| --- | ---: | --- |
| Creates | ~1,160/sec | A single database node or counter is not the durable plan. |
| Redirects | ~11,600/sec | Cache the hot `code -> destination` point lookup. |
| Ten-year mappings | 365B | Partition durable storage by a hash of the code. |
| Base62 capacity at seven characters | ~3.5T | Seven generated characters cover the retained mappings. |

The raw long-URL bytes at 100 bytes each are roughly 36.5 TB before metadata, indexes, replication, and
analytics. The useful interview conclusion is not a precise disk number: it is that mappings require
partitioned, replicated storage while redirect traffic must mostly avoid it.

Use the capacity rule `62^n >= retained mappings` for any requested code length. Fixed-length random codes,
hash prefixes, and base62 IDs all need this capacity check; only their collision and predictability properties
are different.

## Start with the two paths

**Create is the correctness path.** Validate the submitted URL, allocate a globally unique numeric ID, encode
it as base62, persist the mapping behind a unique database constraint, then return the alias. Base62 makes an
ID compact; it does not create uniqueness itself.

**Redirect is the latency path.** Resolve the code from cache, fall back to the mapping store on a miss,
validate status/expiry, return the redirect, and publish analytics asynchronously. The mapping store decides
whether a link exists; cache and analytics only optimize the experience.

## Interview delivery

1. Agree generated aliases, immutable mappings, expiry behavior, and whether click visibility is required.
2. Estimate create rate, redirect rate, retained mappings, and code capacity.
3. Draw the create path before introducing cache or analytics.
4. Trace cache hit, cache miss, and missing/expired link on the redirect path.
5. Deep dive into ID allocation, cache/partition behavior, and redirect/cache-policy trade-offs.
6. Close with cache/database/ID-generator failure behavior and abuse controls.

## Architecture follows the paths

![URL shortener architecture](./assets/url-shortener-architecture.svg)

The URL service is stateless. The mapping store is the source of truth; the cache holds immutable hot
mappings; the ID generator is used only for creation; and the analytics pipeline is deliberately outside the
redirect response path.

## Create and redirect flow

![URL shortener create and redirect flow](./assets/url-shortener-flows.svg)

### Create

1. Validate an allowed URL scheme, length, and caller quota.
2. If dedupe is a product requirement, look up a normalized URL hash; otherwise every request creates a link.
3. Obtain a unique numeric ID, encode it in base62, and insert the mapping.
4. The unique `short_code` constraint is the final collision guard; return the alias only after the durable write.

### Redirect

1. Resolve `GET /{code}` from cache.
2. On a miss, read the partitioned mapping store by code, then backfill cache if active and unexpired.
3. Return `404` for unknown codes and `410` for intentionally expired/disabled mappings when that distinction
   is a product requirement.
4. Return the redirect before publishing the click event to an asynchronous queue.

## Deep dive: code allocation

| Option | Use when | Trade-off |
| --- | --- | --- |
| Unique ID + base62 | Default interview answer | Simple collision-free write path; sequential IDs can be enumerable. |
| Random base62 + unique insert/retry | Codes should be less predictable | Collision retry is part of the create path. |
| Permuted ID or hash prefix + retry | Opaque or deterministic-looking codes are requested | Retain a collision check; permutation hides sequence but is not authorization. |

Use a distributed ID generator or leased ID ranges so creation does not depend on one process. If the generator
is unavailable, new links fail or retry; existing redirects continue because they never need a new ID.

## Deep dive: cache, partitions, and hot links

Cache `shortCode -> destination/status/expiry` with a bounded TTL. On cache miss, the service performs one
indexed point lookup and backfills the cache. A short negative-cache TTL can protect the store from repeated
unknown-code probes, but a long one can incorrectly preserve a temporary miss. Hash-partition by code so both
reads and stored mappings spread evenly; avoid range-sharding on a sequential ID, which can concentrate new
writes.

Popularity is skewed. A hot link can overload one cache shard or one destination, so use cache replication or
request coalescing when measured hot-key traffic demands it. Do not introduce a CDN in the base answer if the
system requires per-click abuse decisions or analytics.

## Deep dive: redirect semantics

`301` means the target is permanent and `302` means it is temporary. Neither status alone defines whether a
browser or intermediary retains the result; response cache policy matters too. For a stable, non-analytic link,
a permanent redirect can reduce repeat service traffic. For a link needing per-click decisions, use a temporary
redirect with an explicit cache policy that preserves the shortener lookup. [RFC 9110](https://www.rfc-editor.org/rfc/rfc9110.html)

## Failure and recovery

| Failure | Observable result and recovery |
| --- | --- |
| Cache unavailable | Redirect service falls back to the mapping store; latency/load rise but mappings remain correct. |
| Mapping partition unavailable | Serve cached hot mappings; otherwise return a retryable failure or fail over to a replica. |
| ID generator unavailable | Creation fails/retries; existing redirects remain unaffected. |
| Analytics queue unavailable | Redirect still succeeds; buffer, retry, or intentionally drop analytics by stated policy. |
| Cache fill stampede | Coalesce concurrent misses or cap retries; do not let one viral link overwhelm the store. |
| Create abuse | Enforce IP/user/API-key quotas before allocation and store a disable status for moderation. |

## Follow-ups that change the design

| Follow-up | What changes |
| --- | --- |
| Custom alias | Atomically reserve the requested code, enforce ownership/quota, and moderate abusive names. |
| Destination update/delete | Version/status the mapping and invalidate or bypass stale cache entries. |
| Expiry | Store `expiresAt`, prevent cache backfill after expiry, and clean up asynchronously. |
| Dedupe | Normalize carefully and index a long-URL hash; this is product behavior, not a default assumption. |
| Security | Block unsafe schemes, rate-limit creation, and asynchronously scan destinations if the product requires it. |
| Analytics dashboard | Aggregate queued clicks in a separate analytical store; never query it on the redirect path. |

## Quick recall

**Q. What makes base62 safe?**

A. Nothing by itself; the input numeric ID must already be unique and the database still enforces uniqueness.

**Q. What decides redirect correctness during a cache miss?**

A. The durable mapping store, including active/expiry state.

**Q. Why is analytics asynchronous?**

A. A queue failure must not delay or fail the redirect response.

**Q. Why hash-partition by code?**

A. Redirects are point lookups, and a hash spreads both mappings and read traffic.

**Q. What changes if custom aliases are in scope?**

A. Alias reservation and uniqueness become a user-visible, contention-prone create operation.
