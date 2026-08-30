---
order: 10
search: false
---

# Web Crawler Exercise

## Exercise: web-crawler-hld - Design The Frontier

### Goal

Walk through a web crawler HLD with special focus on URL frontier scheduling.

## Timed mock

Set a 45-minute timer. Draw the frontier and fetch flow before opening the reference design.

| Time | What to produce |
|---|---|
| 0-5 min | Scope, crawl freshness, politeness, and storage requirements |
| 5-8 min | Fetch QPS, storage, bandwidth, and dedup scale |
| 8-13 min | URL/content records and crawler APIs or interfaces |
| 13-25 min | Discovery, normalization, durable frontier, fetchers, parser, and storage |
| 25-40 min | Deep dive: per-host scheduling/politeness and distributed frontier ownership |
| 40-45 min | Traps, retries, recrawl, and trade-offs |

### Prompt

Design a crawler that downloads 1B HTML pages/month for search indexing, stores content for 5 years, ignores duplicate content, and respects robots.txt.

### Required sections

- Clarifying questions
- Functional and non-functional requirements
- Back-of-envelope estimation
- High-level architecture
- URL frontier design
- Politeness strategy
- URL/content dedup strategy
- Recrawl freshness strategy
- Scaling and failure modes

### Self-grilling questions

- Why is naive BFS insufficient?
- Why is DFS usually worse?
- How do you enforce politeness without a permanent queue for every website?
- What lives in memory vs durable storage in the frontier?
- What is the difference between URL seen and content seen?
- How do robots.txt and per-host delays interact?
- What happens if a host has millions of generated trap URLs?
- Which parts of the system must be stateless?

### Acceptance criteria

- You explain front queues vs back queues.
- You give a concrete answer for "we cannot have a queue for every site."
- You discuss robots.txt, User-Agent, per-host delay, timeout, and retry/backoff.
- You keep HTML downloader internals brief.
- You mention recrawl scheduling, not only one-time discovery.
- You identify what the book covered well and what you added from broader crawler design.

## Self-review

| Signal | Score 0-2 |
|---|---|
| URL discovery, canonicalization, seen-URL, and content dedup are separate | |
| Host scheduling explains priority, `nextAllowedAt`, and politeness | |
| Frontier state is durable while fetchers are stateless | |
| Partition ownership prevents cross-worker coordination per host | |
| Retry, robots failure, trap URLs, and recrawl have explicit behavior | |

**Target:** at least `7/10`. Then compare with [Design](/system_design/case_studies/web_crawler/design/).
