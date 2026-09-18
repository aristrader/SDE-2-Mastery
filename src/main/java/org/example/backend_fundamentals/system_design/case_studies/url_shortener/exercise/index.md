---
order: 10
search: false
---

# URL Shortener Exercise

## Exercise: url-shortener-hld - Design a Read-Heavy Redirect Service

### Goal

Give a 35–40 minute HLD answer for a TinyURL-style system that creates generated aliases and redirects them at
scale.

### Timed mock

| Time | Focus |
| --- | --- |
| 0–5 min | Scope, traffic, expiry/custom-alias, and click-visibility questions |
| 5–10 min | Capacity and base62 length |
| 10–25 min | Create/redirect paths, storage, cache, and async analytics |
| 25–35 min | ID allocation, cache failures, hot keys, and partitioning |
| 35–40 min | Abuse controls, trade-offs, and extensions |

### Prompt

Design a service that converts long URLs into compact aliases. A visitor opening an alias should be redirected
to its saved destination with low latency.

### Clarify before designing

- Are aliases generated only, or can users reserve custom names?
- Are mappings immutable, expiring, or editable?
- What traffic and retention assumptions apply?
- Must every click remain observable, or can clients cache stable redirects?
- Are analytics, moderation, and malware scanning synchronous requirements?

### Your answer must cover

- Capacity estimate and base62 code-length reasoning.
- Create path: validation, unique ID, base62 encoding, durable unique mapping.
- Redirect path: cache hit, cache miss, expiry/status check, and redirect response.
- Why analytics is asynchronous and why the mapping store remains authoritative.
- Hash partitioning, cache failure, ID-generator failure, hot-key behavior, and create abuse.
- The separate choices of redirect semantics and response cache policy.

### Acceptance criteria

- Define a source of uniqueness before base62 conversion and retain a database uniqueness guard.
- Give the redirect response only after resolving an active mapping from cache or the durable store.
- Keep analytics out of the redirect success path.
- State the cache-miss, cache-down, and mapping-partition failure outcomes.
- Explain why `301`/`302` do not by themselves decide click visibility.
- Keep custom aliases and destination updates as explicit follow-ups unless required.

### Interview follow-ups

- How would random aliases change collision handling?
- How would custom aliases change the create path?
- What should happen to an expired mapping already in cache?
- How would a viral link avoid stampeding the mapping partition?
- When would a permanent redirect be acceptable?
- What breaks if a hash prefix is used without collision handling?
- Which component is affected when the ID generator is unavailable?
