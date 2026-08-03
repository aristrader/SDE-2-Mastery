---
order: 10
search: false
---

# URL Shortener Exercise

## Exercise: url-shortener-hld - Design The System

### Goal

Walk through the URL shortener HLD in 30-45 minutes.

### Prompt

Design a URL shortening service that creates compact aliases and redirects short URLs to original URLs at scale.

### Required sections

- Clarifying questions
- Functional and non-functional requirements
- Back-of-envelope estimation
- APIs
- Data model
- High-level architecture
- Short-code generation strategy
- Redirect flow
- Scaling and failure modes

### Self-grilling questions

- What breaks if you use the first 7 characters of MD5 without collision handling?
- Why does base62 not solve uniqueness by itself?
- What happens to click analytics if you return `301`?
- Which parts of the design are read-path critical and which are create-path critical?
- What keeps the service working if analytics workers are down?
- What exact component depends on the distributed ID generator?
- How would you stop one user from creating millions of spam links?

### Acceptance criteria

- You justify 7-character base62 capacity from the estimate.
- You explain why base62 needs a distributed ID generator.
- You compare hash+collision vs base62 ID generation.
- You choose `301` or `302` based on analytics needs.
- You keep analytics off the redirect critical path.
- You mention caching, DB sharding/replication, and create-endpoint rate limiting.
