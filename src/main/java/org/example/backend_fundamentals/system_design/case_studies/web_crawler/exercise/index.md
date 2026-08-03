---
order: 10
search: false
---

# Web Crawler Exercise

## Exercise: web-crawler-hld - Design The Frontier

### Goal

Walk through a web crawler HLD with special focus on URL frontier scheduling.

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
