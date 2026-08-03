---
order: 20
search: false
---

# Design a Web Crawler

## Problem

Design a crawler that discovers and downloads web pages for search indexing.

The crawler starts from seed URLs, downloads pages, extracts links, filters/deduplicates them, and schedules future crawl work.

## Clarify scope

Ask:

- What is the crawler for: search indexing, archiving, monitoring, or data mining?
- How many pages per day/month?
- Which content types: HTML only, PDFs, images, videos?
- Do we store raw content? For how long?
- Do we recrawl changed pages?
- How fresh does the index need to be?
- Must we respect robots.txt?
- Do we need JavaScript rendering?
- How do we handle duplicate content and spam?

Reasonable assumptions:

```text
Search indexing
1B HTML pages/month
Store HTML for 5 years
Ignore duplicate content
Respect robots.txt
Recrawl important/changed pages
```

## Requirements

Functional:

- Crawl pages from seed URLs.
- Extract and normalize links.
- Deduplicate already-seen URLs.
- Deduplicate duplicate content.
- Store downloaded HTML and metadata.
- Recrawl pages to keep the dataset fresh.

Non-functional:

- Scalable across many crawler workers.
- Polite to websites.
- Robust against malformed HTML, timeouts, traps, and spam.
- Extensible to new content types.
- Durable crawl state so work can resume after failures.

## Back-of-envelope estimation

```text
Pages/month = 1B
Average QPS = 1B / 30 / 24 / 3600 ~= 400 pages/sec
Peak QPS ~= 800 pages/sec
Average page size = 500 KB
Storage/month = 1B * 500 KB ~= 500 TB
5-year storage = 500 TB * 12 * 5 = 30 PB
```

Design implications:

| Number | Implication |
|--------|-------------|
| 400-800 fetches/sec | Workers are easy to scale, but must be host-politeness aware |
| 30 PB retention | Store content in partitioned disk/object storage |
| Billions of URLs | URL seen/frontier cannot be only in memory |
| Many duplicate pages | Hash/fingerprint content before storing |

## High-level architecture

![Web crawler architecture](../assets/web-crawler-architecture.svg)

```text
Seed URLs
  -> URL frontier
  -> Fetcher workers
  -> HTML parser
  -> Content seen?
  -> Content storage
  -> URL extractor
  -> URL filter
  -> URL seen?
  -> URL frontier
```

Component responsibilities:

| Component | Responsibility | Interview caveat |
|-----------|----------------|------------------|
| Seed URL service | Provides starting URLs by domain/topic/region | Seed choice affects coverage |
| URL frontier | Schedules next URLs | Must handle priority, freshness, and politeness |
| Fetcher workers | Download pages | Respect robots, timeout, retry/backoff, content size |
| DNS cache | Maps host to IP | DNS can become a bottleneck |
| Parser | Validates HTML and extracts links | Keep parsing separate from fetching |
| URL filter | Drops unsupported/bad URLs | Blocks traps, spam, invalid content types |
| URL seen store | Tracks normalized URL hashes | Avoid duplicate frontier entries |
| Content seen store | Tracks content fingerprints | Avoid duplicate storage/indexing |
| Content storage | Stores HTML/metadata | PB-scale durable storage |

## URL frontier design

![URL frontier priority and politeness](../assets/url-frontier-scheduler.svg)

The frontier is the main design deep dive.

### Why naive BFS fails

Treating the web like a plain FIFO queue causes:

- **Impoliteness:** many links from one page often point to the same host, so parallel workers can hammer that host.
- **Bad prioritization:** low-value spam can be crawled before important pages.
- **Poor freshness:** updated important pages may wait behind millions of unimportant new URLs.
- **Spider traps:** infinite URL spaces can keep generating new links.

DFS is worse because link depth can be effectively unbounded.

### Two-stage frontier

Use two layers:

```text
Front queues: priority and freshness
Back queues: politeness and host scheduling
```

Front queues classify URLs by score. The score may use PageRank/link score, domain quality, historical update rate, recrawl deadline, and product-specific topic priority.

Back queues enforce host politeness. Each active host has pending URLs and a `nextAllowedFetchAt`. Workers pull only hosts whose delay has expired.

### Answer to "one queue per site?"

You do not create a permanent in-memory queue for every site.

Use this implementation model:

```text
hostKey = normalized host or registered domain
partition = hash(hostKey) % N

Durable store:
  hostKey -> pending URL list
  hostKey -> crawl policy
  hostKey -> nextAllowedFetchAt

Memory:
  active host heap ordered by nextAllowedFetchAt
  small per-host buffers for currently active hosts
```

Only hosts with pending URLs need active state. Large or inactive host queues live in durable partitioned storage. This gives per-host politeness without millions of hot in-memory queues.

### Worker scheduling

1. Pick a priority queue with weighted selection.
2. Route the URL to its host partition.
3. If host is eligible, put it in the active host heap.
4. Worker pops a host whose `nextAllowedFetchAt <= now`.
5. Worker fetches one URL from that host.
6. Scheduler updates `nextAllowedFetchAt`.
7. If the host still has URLs, reinsert it; otherwise evict active host state.

This keeps global throughput high while limiting pressure on each website.

## URL normalization and dedup

Normalize before URL-seen checks:

- lowercase scheme/host
- remove fragments
- normalize default ports
- resolve relative paths
- sort/drop known tracking query parameters where safe
- follow canonical signals/redirects when available

Then store a hash of the normalized URL in a URL-seen store or Bloom filter plus durable URL table.

URL seen prevents fetching the same URL again. Content seen catches same content under different URLs.

## Content dedup

Content dedup path:

```text
HTML -> parse/clean -> fingerprint/hash -> content seen check -> store or discard
```

Use exact hashes for exact duplicates. For near duplicates, mention simhash/minhash-style fingerprints only if the interviewer asks about search-quality depth.

## Robots.txt and politeness

Before crawling a host:

1. Fetch `scheme://host/robots.txt`.
2. Parse the group for your crawler's user-agent; fall back to `*`.
3. Apply most-specific `Allow`/`Disallow` path match.
4. Cache robots rules and refresh periodically.
5. Use a clear crawler User-Agent.
6. Enforce a default per-host delay even if robots has no explicit delay.

Robots.txt is not security authorization; it is crawl policy that well-behaved crawlers respect.

## Fetcher design

Keep fetcher details short in an interview:

- HTTP GET with max response size
- DNS cache
- short timeout
- retry with backoff for transient failures
- content-type and status-code handling
- compression support
- redirect limit
- JavaScript/server-side rendering only if dynamic links are in scope

The fetcher should be stateless. Crawl state belongs in the frontier and stores.

## Freshness and recrawling

Not every page should be recrawled equally.

Signals:

- historical update frequency
- page/domain importance
- sitemap hints
- HTTP cache headers like `Last-Modified`/`ETag`
- previous crawl failures

Schedule recrawls by due time and priority:

```text
nextCrawlAt = function(importance, update_frequency, last_crawl_result)
```

Important pages can be recrawled daily/hourly. Low-value stable pages may be recrawled rarely.

## Storage

| Store | Data |
|-------|------|
| Frontier store | pending URLs, priority, host, next crawl time |
| URL seen store | normalized URL hashes |
| Content seen store | content fingerprints |
| Content store | raw/cleaned HTML and metadata |
| Crawl history | status, fetch time, content hash, errors, next crawl time |
| Robots cache | host crawl rules and expiry |

Use memory buffers for hot frontier operations, but persist state so a crash does not lose the crawl.

## Scaling

- Partition URLs by host/domain hash so the same host's politeness state is owned by one scheduler partition.
- Scale fetcher workers horizontally.
- Keep fetchers stateless.
- Use consistent hashing if adding/removing crawler partitions frequently.
- Store large queues and seen sets durably; memory is only a cache/buffer.

## Failure modes

| Failure | Handling |
|---------|----------|
| Fetch timeout | Retry with backoff, then mark failed and reschedule later |
| DNS failure | Cache negative result briefly, retry later |
| Worker crash | Frontier lease/ack model returns URL to queue |
| Parser crash on bad HTML | Isolate parsing and skip malformed content |
| Spider trap | URL length/depth caps, per-host URL caps, pattern filters |
| Host overload/errors | Increase per-host delay, reduce priority, or pause host |
| Duplicate/spam flood | URL filters, canonicalization, host/domain caps, quality scoring |

## What to skip unless asked

- Full HTML parser implementation.
- Complete robots.txt parser grammar.
- Browser rendering architecture.
- Search ranking/index serving.
- PageRank math.
- Exact Bloom filter false-positive math.

Name these only when relevant. The core HLD is frontier scheduling, politeness, dedup, storage, and distributed workers.

## Quick recall

**Q. Why is crawler a good HLD interview problem?**  
A. It tests queues, dedup, distributed scheduling, rate limiting/politeness, storage, and failure handling.

**Q. What is the URL frontier?**  
A. The scheduler/store for URLs waiting to be crawled; it controls priority, freshness, and politeness.

**Q. Why not plain FIFO BFS?**  
A. It can overload one host and wastes crawl budget on low-value or duplicate pages.

**Q. How do we avoid one queue per website?**  
A. Maintain logical per-host state only for active hosts, spill queues to durable storage, and use a heap ordered by `nextAllowedFetchAt`.

**Q. What is the difference between URL dedup and content dedup?**  
A. URL dedup avoids refetching the same normalized URL; content dedup avoids storing the same page content from multiple URLs.

**Q. What is the most important politeness rule?**  
A. Respect robots.txt and enforce per-host delays before scheduling fetches.
