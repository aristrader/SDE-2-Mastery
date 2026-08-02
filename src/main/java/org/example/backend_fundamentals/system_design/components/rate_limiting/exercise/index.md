---
order: 10
search: false
---

# Exercise

## Exercise: distributed-api-rate-limiter - Design a Distributed API Rate Limiter

Design a rate limiter for a public HTTP API used by web, mobile, and partner clients.

Requirements:

- Support per-user, per-IP, per-API-key, per-endpoint, and global limits.
- Work across many API servers.
- Add minimal latency to each request.
- Return useful throttling responses to clients.
- Support different rules for free and paid tenants.
- Handle Redis or rate-limit-store failure explicitly.

Scale assumptions:

- 50K average RPS.
- 200K peak RPS during traffic spikes.
- 20M registered users.
- 100K partner API keys.
- Some endpoints call paid third-party vendors.

Tasks:

1. Clarify which limits you would enforce first and why.
2. Draw the high-level architecture.
3. Choose an algorithm for normal traffic and explain why.
4. Choose an algorithm for login/abuse-heavy endpoints and explain why.
5. Define Redis keys and TTLs for at least three rules.
6. Explain how you avoid race conditions.
7. Explain how you handle hot keys.
8. Decide fail-open vs fail-closed for:
   - public product search
   - login
   - payment creation
   - KYC vendor verification
9. Specify the HTTP response and headers for throttled requests.
10. List the metrics and alerts you would add.

## Acceptance criteria

A good answer includes:

- gateway/middleware placement
- Redis or equivalent shared fast store
- atomic check-and-update
- token bucket or sliding-window tradeoff
- clear `429` behavior
- fail-open/fail-closed reasoning
- monitoring of rejection rates, hot keys, and limiter latency

## Quick recall

**Q. What is the minimum distributed design?**  
A. API gateway or middleware plus shared Redis counters.

**Q. Which algorithm is a good default for burst-friendly API limits?**  
A. Token bucket.

**Q. Which endpoints deserve stricter rules?**  
A. Login, payment, account creation, SMS/email, and paid vendor calls.

**Q. What is the most common race condition?**  
A. Read-check-write counter logic split across multiple operations.
