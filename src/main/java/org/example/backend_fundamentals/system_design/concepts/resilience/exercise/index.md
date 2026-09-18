---
order: 10
search: false
---

# Exercise

## Exercise: resilience-boundary - Handle an unknown downstream outcome

An API submits a non-idempotent create request to a downstream service. The client times out after the
request may have reached that service.

1. Why is an immediate blind retry unsafe?
2. What identifiers and state should be recorded before the call?
3. What response should the API return while the outcome remains unknown?
4. When should the circuit breaker open, and what should a half-open state do?

## Answer shape

Persist an idempotency key/correlation ID before the call. Treat timeout as unknown, resolve it through a
status lookup or safe reconciliation, and return a pending/degraded response rather than inventing a final
outcome. The circuit opens after a configured failure pattern and probes recovery with limited traffic.

## Quick recall

**Q. What does a timeout prove?**
A. Only that the caller did not receive a response in time; it does not prove the downstream side did nothing.
