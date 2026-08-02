---
order: 20
search: false
---

# Design

## Worked HLD flow: rate limiter

Use this as a timed interview script.

### Clarify

- Is this server-side or client-side? Assume server-side.
- What dimensions? Assume user, IP, API key, endpoint, and global.
- Distributed? Assume many app servers.
- Strict or approximate? Assume accurate enough, with low latency.
- What response? `429 Too Many Requests` with retry headers.

### Estimate

- Peak RPS determines gateway and Redis capacity.
- Number of identities determines key count.
- Number of rules determines rule-cache size.
- Hot global keys may need sharding.

### High-level design

```text
Client -> Gateway/middleware -> Rate limiter -> Redis
                              -> API service if allowed
                              -> 429 if rejected
```

### Deep dive

Focus on:

- token bucket vs sliding window
- Redis key design and TTL
- atomic check-and-update
- hot keys
- fail-open vs fail-closed
- monitoring rejected requests and limiter latency

### Wrap up

Summarize:

```text
This design protects APIs with gateway-side rules, Redis-backed atomic counters, token bucket for burst-friendly traffic, stricter sliding windows for abuse-sensitive endpoints, 429 responses for clients, and monitoring for false positives, hot keys, and Redis latency.
```

## Quick recall

**Q. Why is this a good HLD practice example?**  
A. It forces requirements, estimates, algorithms, distributed state, failure behavior, and monitoring into one design.
