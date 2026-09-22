---
order: 10
search: false
---

# Final exercise — Rate Limiter LLD

## Exercise: rate-limiter-lld - Per-Client In-Memory API Limiter

### Goal

Model an in-memory API rate limiter that independently allows or rejects requests for each client key.

This is the agreed scope after discussing the initial [interviewer prompt](problem_statement/) and
[candidate clarifications](candidate_discussion/).

### Requirements

- Accept an opaque client key and return an allow-or-reject decision for one request. The caller may use
  one attribute or compose multiple attributes into that key.
- Give each client key independent state; activity for one client must not affect another.
- Configure a positive burst capacity and a positive refill rate.
- An allowed request consumes one available unit.
- Replenish availability from elapsed time, without exceeding the configured burst capacity.
- Reject a request when no unit is available; a rejected request must not consume a unit.
- Handle concurrent requests for the same client without allowing more requests than its current
  allowance permits.
- Reject invalid configuration and an invalid client key with clear exceptions.

### Constraints

- Keep the base implementation in memory and local to one process.
- A token represents one request; variable request cost is a follow-up.
- Keep the limiter independent of HTTP; mapping rejection to `429`, remaining allowance, and retry-after
  information are follow-ups.

### Test scenarios

- A new client can make requests until it reaches the configured burst capacity.
- The next request is rejected after that allowance is exhausted.
- Elapsed time replenishes allowance, and a later request can succeed.
- A long idle period never creates more allowance than burst capacity.
- Two client keys do not affect each other's decisions.
- Concurrent requests cannot collectively exceed a client's available allowance.
- Invalid client keys and invalid configuration are rejected.

### Interview follow-ups

- How would you implement a fixed-window or sliding-window policy instead?
- How would different plans, endpoints, or request weights change policy selection?
- How would multiple application instances enforce one shared limit?
- How would you expire inactive client state without affecting active requests?
- How would you return retry-after information and expose allow/reject metrics?
