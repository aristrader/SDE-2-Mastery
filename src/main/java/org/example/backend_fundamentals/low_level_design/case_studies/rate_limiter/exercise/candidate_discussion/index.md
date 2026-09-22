---
search: false
---

# Candidate discussion — Rate Limiter

Start by proposing a narrow scope:

> “I will model an in-memory limiter that decides whether one API request is allowed for a client key.
> Each client has an independent allowance, and the first version is safe for concurrent callers.”

Then confirm the decisions that change the model.

## Questions to ask

1. Is this a reusable library for several APIs, or a limiter dedicated to one API?
2. What identifies the subject to limit: user, API key, IP address, endpoint, or another attribute?
3. Can the limit key combine attributes, such as user plus endpoint, or is one identifier always enough?
4. Is there one global policy or different limits for plans, endpoints, and use cases?
5. What are the actual burst capacity and refill rate for the first policy?
6. Should the policy allow a short burst, or strictly cap all requests in each time period? Is a bucket
   strategy already chosen, or should I select one from this behavior?
7. Does a rejected request consume any allowance?
8. Should the library return only an allow-or-reject decision, or also remaining allowance and retry-after
   information? Is HTTP `429` mapping outside the library?
9. Must requests for the same client be safe when they arrive concurrently?
10. Is this limiter local to one application instance, or must several instances share a limit?
11. Are dynamic policies, metrics, inactive-client cleanup, and persistence part of the first version?

## Agreed scope for this exercise

- The limiter receives a client key and decides whether that one request is allowed.
- Every client key has its own configured burst capacity and steady refill rate.
- The caller supplies the client key. It may represent a single attribute or a composite key such as
  `user + endpoint`; key construction is outside the limiter.
- An allowed request consumes one unit; a rejected request does not.
- Capacity replenishes over elapsed time but never exceeds the configured burst capacity.
- Concurrent requests for one client must not exceed that client's allowance.
- Keep the first implementation in memory and local to one process.
- Return an allow-or-reject decision in the first version; HTTP `429`, retry-after, remaining allowance,
  distributed coordination, plan-specific policies, persistence, metrics, and cleanup are follow-ups.

The final [exercise](../) records the resulting requirements and test scenarios.
