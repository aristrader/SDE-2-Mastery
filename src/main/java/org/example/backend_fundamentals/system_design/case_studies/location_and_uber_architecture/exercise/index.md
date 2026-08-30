---
order: 10
search: false
---

# Exercise

## Exercise: uber-hld - Design a ride-sharing service

## Timed mock

Set a 45-minute timer. Do not open the design tab until the self-review.

| Time | What to produce |
|---|---|
| 0-5 min | Clarifying questions, scope, and functional/non-functional requirements |
| 5-8 min | Location-update and ride-request QPS estimates |
| 8-13 min | Core APIs, entities, and ride-state lifecycle |
| 13-25 min | Location ingestion, spatial index, matching, trip state, and notification path |
| 25-40 min | Deep dive: fresh nearby-driver lookup and correct driver assignment |
| 40-45 min | Failure handling, regional partitioning, and trade-offs |

## Blind prompt

Design the core backend for a ride-sharing service. Riders need a fare estimate, can request a ride, and can track the assigned driver. Drivers go online/offline, publish location, and accept or decline a ride offer. Optimize for low matching latency, fresh location, and never assigning one driver to two rides.

Exclude payments, promotions, fraud, and customer support unless a follow-up asks for them.

## Your answer must cover

- An expiring, server-authoritative fare quote before the rider creates a ride.
- Latest driver location in a high-throughput spatial index, not the primary ride database.
- Candidate funnel: pickup cell and neighbouring cells, eligibility filter, exact distance/ETA, then ranking.
- Atomic short-lived offer coordination and a durable conditional assignment.
- Push notification plus a durable trip-state read path for missed notifications.
- A regional queue only for matching bursts or recovery, not for freshness-sensitive GPS updates.

## Interviewer follow-ups

1. A driver sends an update every five seconds. Why not write every update to PostgreSQL?
2. Two matchers choose the same driver. What decides the final winner after a Redis lease expires?
3. What happens when a driver does not respond to the offer within ten seconds?
4. A concert creates a sudden request spike in one downtown cell. What scales, and what must remain local?
5. How do you avoid stale/offline drivers appearing in `nearby drivers`?

## Self-review

Score each item `0`, `1`, or `2` after comparing with the design page. `0` = missing, `1` = named but vague, `2` = clear flow, trade-off, and recovery.

| Signal | Score |
|---|---|
| Requirements and estimates changed the design | |
| Location index is fresh, local, and handles cell boundaries | |
| Lease and durable assignment are clearly separate | |
| Timeouts, retries, and late acceptance are safe | |
| Regional queueing has a reason, deadline, and idempotency | |

**Target:** at least `7/10`. Record any weak item as `Visit Again` in the Part row, then read [Design](/system_design/case_studies/location_and_uber_architecture/design/).
