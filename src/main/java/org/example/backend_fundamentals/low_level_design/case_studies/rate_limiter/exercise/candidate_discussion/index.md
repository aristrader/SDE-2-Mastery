---
search: false
---

# Candidate discussion — Rate Limiter

Start by proposing a narrow scope:

> “I will model an in-memory limiter that decides whether one API request is allowed for a client key.
> Each client has an independent allowance, and the first version is safe for concurrent callers.”

Then confirm the decisions that change the model.

## Questions to ask

1. What identifies the subject to limit: user, API key, IP address, endpoint, or a combination?
2. Is there one global policy or different limits for plans and endpoints?
3. Should the policy allow a short burst, or strictly cap all requests in each time period?
4. Does a rejected request consume any allowance?
5. Must requests for the same client be safe when they arrive concurrently?
6. Is this limiter local to one application instance, or must several instances share a limit?
7. Are retry-after information, dynamic policies, metrics, and persistence part of the first version?

## Agreed scope for this exercise

- The limiter receives a client key and decides whether that one request is allowed.
- Every client key has its own configured burst capacity and steady refill rate.
- An allowed request consumes one unit; a rejected request does not.
- Capacity replenishes over elapsed time but never exceeds the configured burst capacity.
- Concurrent requests for one client must not exceed that client's allowance.
- Keep the first implementation in memory and local to one process.
- Distributed coordination, plan-specific policies, persistence, HTTP response mapping, and metrics are
  follow-ups.

The final [exercise](../) records the resulting requirements and test scenarios.
