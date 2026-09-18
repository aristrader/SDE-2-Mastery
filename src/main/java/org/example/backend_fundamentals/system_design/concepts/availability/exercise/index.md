---
order: 10
search: false
---

# Exercise

## Exercise: availability-slo - Defend a user-visible SLO

An order API has a 99.9% monthly availability target. It runs on two application instances behind a load
balancer and one primary database with a replica.

1. Define the request-level SLI: numerator, denominator, window, and what counts as success.
2. Explain which failure the two application instances survive and which one they do not.
3. Describe what must happen when the primary database fails, including the client retry behavior.
4. Name one correlated failure that makes the apparent redundancy ineffective.

## Answer shape

A strong answer does not promise five nines from two instances. It distinguishes process failure from a
zone/database failure, gives a promotion and routing path, and uses an idempotency key so a retry after an
unknown result does not create a duplicate order.

## Quick recall

**Q. Why is a health check alone insufficient?**
A. It detects an unhealthy route; it does not make data failover or retry semantics correct.
