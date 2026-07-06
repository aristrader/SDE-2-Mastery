---
order: 40
---

# Rate Limiting

## Multi-Dimensional Rate Limits
Most production systems enforce multiple limits simultaneously rather than relying on a single rule. A request succeeds only if it passes all dimensions.

Common dimensions include:
- **Per-IP:** Limits requests from a single address to prevent simplistic brute-force attacks.
- **Per-Account / Per-User:** Limits requests per authenticated user.
- **Per-API-Key:** Limits automated traffic from registered clients.
- **Global Limit:** A system-wide threshold (e.g., max 10k req/sec) to protect backend infrastructure regardless of who is sending the traffic.

### Login Page Rate Limiting
Login endpoints are prime targets for attacks (credential stuffing, brute forcing). They usually combine:
1. **Per-IP limit:** Stops attackers running scripts from a single machine.
2. **Per-Account limit:** Stops attackers rotating through 1,000 IPs to attack a single target account (since email/username is provided before auth succeeds).
3. **Global limit:** Stops massive distributed botnets from overwhelming the service even if neither IP nor Account limits are breached individually.

## CAPTCHA as Rate Limiting
The primary goal of CAPTCHA is not simply to "prove you are human", but to **make automated abuse expensive**. It slows down request rates drastically by forcing 3-10 seconds of human/ML work per action. 
Modern CAPTCHA (like reCAPTCHA v3) uses a **Risk Scoring Model** (0.0 to 1.0) instead of direct puzzles, evaluating:
- Mouse movement (smoothness, acceleration)
- Click behavior and timing
- Browser fingerprint (fonts, plugins, language)
- IP Reputation (residential vs datacenter/Tor)
- Cookie history

## Backpressure vs Rate Limiting
Controlling Kafka consumer counts or enforcing queues is conceptually similar to rate limiting (protecting downstream systems from load), but is classified differently.
- **Rate Limiting:** Usually an API gateway / edge concept where excess requests are immediately rejected (e.g. `429 Too Many Requests`).
- **Backpressure / Flow Control:** Internal system mechanics (like Kafka consumer throughput limits) where excess load is queued, delayed, or shed internally to prevent component collapse.

## Common Rate Limiting Algorithms

### Sliding Window Log
Stores the exact timestamp of every request. When evaluating a new request, it counts timestamps within the window (`Current Time - Window Size`). 
- **Gotcha:** Extremely memory heavy for high-throughput systems.

### Sliding Window Counter
Divides the window into discrete buckets (e.g., 0-10 sec, 10-20 sec) and stores request counts per bucket. 
- **Benefit:** Highly memory efficient compared to the raw log while still smoothing out boundary bursts.

### Token Bucket
Buckets have a fixed **Capacity** and a constant **Refill Rate** (e.g., 10 tokens/sec, max 100).
- **Gotcha:** Allows burst traffic. If a system is idle overnight, the bucket fills to its max capacity. When traffic spikes, the initial burst is allowed up to the full capacity. Millions of users bursting simultaneously can still overwhelm the backend, which is why a **Global Limit** is strictly necessary alongside user token buckets.

### Leaky Bucket
Incoming requests enter a queue. The system processes them at a strict, constant outgoing rate (the "leak").
- **Trade-off:** Rate Limiting (Token Bucket) rejects excess traffic; Leaky Bucket queues it. The last request in a burst will wait significantly longer for processing.

## Redis Hot Keys in Rate Limiting
Redis is frequently used for distributed rate limiting (via `INCR` and `EXPIRE`) because it is fast, in-memory, and atomic. 
- **Hot Key Problem:** Occurs when a single key (e.g., a global limit counter or a massive tenant's key) receives disproportionate traffic, melting a single Redis node while the rest of the cluster is idle.
- **Solution:** Key Sharding. Split the hot key into multiple keys (e.g., `limit:1`, `limit:2`, `limit:3`). Traffic distributes across nodes, and the application aggregates the counts when reading.

## Quick recall
**Q. Why isn't an IP-based rate limit enough for a login page?**
A. Attackers can easily bypass it by rotating through proxy IPs. A per-account limit based on the submitted email/username is required alongside it.

**Q. What is the fundamental purpose of a CAPTCHA?**
A. To make automated abuse (credential stuffing, scraping) economically unviable by forcing 3-10 seconds of human effort per request, effectively dropping the request rate.

**Q. Token Bucket vs Leaky Bucket — what happens to a sudden traffic burst?**
A. Token Bucket allows the burst to pass immediately (up to bucket capacity). Leaky Bucket queues the burst and processes it at a strict, constant rate.

**Q. What is the Redis "Hot Key" problem and how is it mitigated?**
A. A single key receives massive traffic, overloading one node in the cluster. Mitigated by key sharding (e.g., appending `:1`, `:2` to the key) to distribute load, then aggregating on read.

## Resources / References
- [HelloInterview: Distributed Rate Limiter System Design](https://www.hellointerview.com/learn/system-design/problem-breakdowns/distributed-rate-limiter) - Excellent end-to-end breakdown of the HLD interview for a distributed rate limiter.
- [YouTube: Distributed Rate Limiter](https://www.youtube.com/watch?v=MIJFyUPG4Z4&t=752s) - Video walkthrough of the distributed rate limiter architecture.


