---
order: 10
search: false
---

# Web Crawler Exercise

## Exercise: web-crawler-hld - Design a polite frontier

### Prompt

Design a crawler for search indexing. It must crawl 1 billion HTML pages each month, retain fetched bodies for five years, avoid duplicate content, respect robots rules, and recrawl important changed pages. Search ranking, browser rendering, and non-HTML media are out of scope.

### Timed mock

Set 45 minutes. Do not begin with components; make each box answer a pressure from the prompt.

| Time | Deliverable |
| --- | --- |
| 0–5 min | Clarify scope, define success, and state explicit exclusions. |
| 5–9 min | Give one fetch/storage estimate and connect it to durable partitioned state. |
| 9–16 min | Explain FIFO plus `seenUrl` baseline, then why host bursts, traps, freshness, and crashes break it. |
| 16–26 min | Draw discovery admission and due-fetch/recrawl paths, naming the durable acceptance and lease-ack boundaries. |
| 26–38 min | Deep-dive active-host scheduling, URL/content dedup, and robots/failure policy. |
| 38–45 min | Reject the global queue, walk failure recovery, and label follow-ups. |

### Your answer must show

- A canonical URL record, host key, `nextAllowedFetchAt`, and expiring crawl lease; say which is durable.
- A discovery path from extracted raw link through normalization, filter, atomic admission, and pending frontier state.
- A normal due-fetch path, including a conditional recrawl that receives `304 Not Modified`.
- Why a global FIFO/rate limiter is insufficient and why active logical host queues do not require permanent in-memory queues for every site.
- Separate URL admission deduplication from exact content deduplication, including the Bloom-filter false-positive trade-off if you use one.
- Concrete results for a worker crash, timeout/429/503, robots unreachable, malformed HTML, and spider trap.

### Self-grilling

1. What exact durable action accepts a newly discovered URL?
2. Why is “one queue for every host” the wrong implementation interpretation of host politeness?
3. If a worker commits the body but dies before acknowledging the lease, why is a later retry safe?
4. What does a `304` change, and what does it deliberately not store?
5. Why is `Crawl-delay` not the same thing as the RFC robots rules?
6. Which policy protects the crawler from missing URLs, and which intentionally trades a little coverage for memory efficiency?
7. How would you transfer a hot host to a new frontier partition without two schedulers fetching it at once?

### Acceptance checklist

| Signal | Score 0–2 |
| --- | --- |
| The scope, baseline, and pressure are explained before the architecture. | |
| Every component is introduced for a named reason and durable ownership is clear. | |
| The normal discovery and recrawl paths have acceptance/acknowledgement boundaries. | |
| Each deep dive gives problem → naive failure → mechanism → trade-off → recovery. | |
| Failure handling says what happens after retry ambiguity and what operations observe. | |

**Target:** `9/10`. Then compare your answer to [Design](/system_design/case_studies/web_crawler/design/), and identify one assumption that would change the design most.
