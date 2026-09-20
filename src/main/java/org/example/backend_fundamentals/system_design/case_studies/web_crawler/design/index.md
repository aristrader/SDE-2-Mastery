---
order: 20
search: false
---

# Design a Web Crawler

## Agreed scope

Build a polite, distributed crawler for search indexing. It accepts seed URLs and links discovered from allowed HTML pages, persists unique URL work, fetches due pages, stores a body or exact content reference, and schedules recrawls. It is not the search index, a browser-rendering system, or a complete media crawler.

| Requirement | Agreed decision |
| --- | --- |
| Discovery | Start with seeds; normalize, filter, and durably admit links extracted from successful pages. |
| Politeness | Respect RFC 9309 robots rules and independently enforce conservative per-host spacing. |
| Freshness | Persist next crawl time and HTTP validators; revalidate due pages. |
| Duplicate control | Canonical URL admission before the frontier; exact content hash after fetch. |
| Durability | Frontier state, leases, history, and bodies survive workers. Fetchers/parsers are stateless. |

Clarify whether the interviewer wants archival, monitoring, non-HTML content, rendering, or search ranking. Those answers materially change the system. For this answer, they stay out of scope rather than becoming unstated gaps.

## Numbers that change decisions

At 1 billion HTML pages/month, the average rate is about 400 fetches/s and a peak assumption is about 800 fetches/s. At 500 KB per retained page, new bodies consume about 500 TB/month and five years consumes about 30 PB. The implication is durable, partitioned body storage and distributed scheduling; it is not permission to send 800 requests/s to one host.

## Records and ownership

| Record | Key fields | Owner / correctness role |
| --- | --- | --- |
| `UrlRecord` | canonical URL, host key, state, priority, next crawl time, last result | Host-key frontier partition owns scheduling. Here `hostKey` is normalized scheme plus authority, matching robots policy scope. |
| `CrawlLease` | URL key, owner, expiry, attempt | Makes a fetch claim recoverable; expiry permits requeue. |
| Host policy | last robots outcome/expiry, `nextAllowedFetchAt`, backoff | Parsed robots rules are derived/rebuildable; same partition serializes host spacing. |
| Content/history | raw-body hash, body pointer, status, validators, fetch time | Metadata commits each URL-to-observed-body relation; object storage deduplicates only byte-identical bodies. |
| URL-seen filter | canonical URL hash | Optimization only; durable `UrlRecord` decides admission. |

## Main paths

![Crawler ownership and discovery loop.](../assets/web-crawler-architecture.svg)

### Discovery and admission

1. Seed service or parser emits a raw link.
2. Normalize it to a canonical URL, filter unsupported or trap-prone candidates, and derive `hostKey`.
3. Check the fast seen filter, then atomically create-or-return the durable `UrlRecord` in that host partition.
4. A new record becomes `PENDING` with a priority and due time. This durable transition accepts the event; duplicate parser delivery returns the existing record.

### Due fetch and recrawl

1. The active-host heap yields a host whose `nextAllowedFetchAt <= now`; its owner grants one expiring lease.
2. A stateless fetcher applies cached/refreshed robots policy, sends a clear User-Agent, and uses bounded HTTP behavior. A DNS failure is a host-backoff outcome.
3. On a cross-host redirect, record it and send the normalized target through filter and admission for the target host; do not bypass its robots policy or spacing. For a recrawl, send available `ETag`/`Last-Modified` validators. A `304` records validation and a later due time; a changed response hashes raw body bytes, stores/reuses that exact body, and records the URL-to-body observation.
4. Commit history/body metadata and complete the lease through one idempotent `(canonicalUrl, leaseAttempt)` transition. If the worker vanishes, a replay is a no-op after commit or lease expiry returns uncommitted work to pending with backoff.

## Deep dive: active-host frontier

![Priority is chosen before logical per-host scheduling; only eligible active hosts reach a fetcher.](../assets/url-frontier-scheduler.svg)

**Problem → naive failure.** Parallel global FIFO gives the same host to many workers and lets low-value URLs outrun important recrawls. A permanent queue for every internet host also wastes memory.

**Mechanism.** Weighted priority/freshness queues choose a candidate, then `hash(hostKey)` sends it to one owner. That owner durably stores each host backlog and keeps active hosts in an in-memory heap ordered by `nextAllowedFetchAt`; only the due heap head can receive a lease. It advances the host time and reinserts the host when work remains.

**Trade-off → recovery.** This abandons exact BFS and adds ownership/rebalance work, but it enforces host-level safety and bounds hot memory. An expired lease returns work to the same owner; repeated 429/503/DNS failures increase delay and pause the host rather than multiplying retries. Consistent hashing reduces moves during a controlled owner transfer.

## Deep dive: two independent dedup layers

**Problem → naive failure.** Multiple pages discover one destination; different destinations can return one body. Fetch-time-only dedup wastes the first budget, while URL-only dedup wastes storage on mirrors.

**Mechanism.** Normalization and atomic canonical-key admission prevent duplicate pending entries. An optional Bloom filter makes likely repeats cheap but may falsely suppress a new URL, deliberately trading a little coverage for memory; it is not authoritative. An exact hash of raw body bytes after fetch lets content history point byte-identical bodies to one stored object while preserving each URL-to-body observation.

**Trade-off → recovery.** Conservative normalization misses some variants; aggressive rules can merge distinct resources. Version rules and retain discovery/history data so affected records can be recrawled. Parser replay and lease retry converge through the canonical key and content hash.

## Deep dive: policy and failure boundaries

**Problem → naive failure.** Fetch failures, robots failures, and host backpressure are different situations. A blanket fast retry can overload a failing host; treating an outage as missing policy can violate a host's wishes.

**Mechanism.** Use `/robots.txt` parseable rules for a clear crawler user agent and cache derived parsed policy with expiry. RFC 9309 requires following a successfully fetched policy, allows access after an unavailable policy resource such as 4xx, and requires complete disallow when it is unreachable such as server/network failure. Per-host spacing is a local crawler policy; `Crawl-delay` is not in the RFC standard. [RFC 9309](https://www.rfc-editor.org/rfc/rfc9309.html)

**Trade-off → recovery.** Fail-closed policy loses temporary coverage but is deliberately polite. Persist policy status/reason and retry refreshes with host backoff. Isolate malformed HTML, cap response size, back off timeout/DNS/transient server errors, and record a final failed crawl rather than losing it.

## Failure matrix

| Boundary | Policy and recovery | Observable outcome |
| --- | --- | --- |
| Worker dies after claim | Lease expires; same durable record returns to pending. | Crawl is delayed, not silently lost. |
| Worker dies after response | Commit body/history before ack; otherwise refetch is safe. | At-least-once HTTP work, one converged durable result. |
| 304 response | Store validation time and reschedule; no duplicate body. | Freshness advances cheaply. |
| 429/503/timeout | Capped backoff raises host delay; eventually pause and alert. | That host becomes slower; others continue. |
| Robots unreachable | Assume disallow until refresh/recovery policy permits otherwise. | Coverage pauses for that host. |
| Trap/spam flood | Pattern, depth/length, and per-host caps; record rejection reason. | One host cannot consume the frontier. |

## Rejected alternative and follow-ups

A global priority queue plus global rate limiter is simpler, but it cannot guarantee per-host spacing or bounded host backlog. We choose host-key ownership instead. Follow-ups: browser rendering, PDFs/media, sitemaps, near-duplicate clustering, focused-crawl scoring, legal deletion, and search indexing/query serving.

## Interview close

Say the baseline first, then use the estimate to justify durable partitioned state. Trace durable admission and lease-to-history commit before naming the frontier, dedup, and policy deep dives. Finish by explaining the global-queue rejection and the exact user/operations outcome of a failed crawl.

## Quick recall

**Q. What decides whether a discovered URL was accepted?**

A. The durable canonical `UrlRecord`/pending-entry transition, not a parser buffer.

**Q. What does the frontier schedule?**

A. Eligible hosts, then one URL under a lease, so priority, freshness, and host spacing coexist.

**Q. What survives a fetcher crash?**

A. The URL record, host policy, lease expiry, history, and body metadata; the worker itself is replaceable.
