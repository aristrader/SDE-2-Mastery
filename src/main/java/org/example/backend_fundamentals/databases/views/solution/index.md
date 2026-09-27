---
order: 20
search: false
---

# Solution

## Solution: revenue-dashboard - Choose a reporting boundary

A normal view centralizes the `orders`/`payments` join and definition of “paid,” but each dashboard read can still
execute that aggregation against live tables. Here, use a materialized view refreshed every 15 minutes because the
product explicitly accepts bounded staleness and checkout should not share the reporting workload.

Store and display the last successful refresh time. If a refresh fails, keep serving that last good snapshot, alert and
retry the job, and mark the dashboard delayed once it exceeds the agreed 15-minute freshness budget. This is recovery,
not silent correctness: a finance user can see that the number is old.

For PostgreSQL, use `REFRESH MATERIALIZED VIEW CONCURRENTLY` when readers must continue during the refresh. It needs a
qualifying unique index on the materialized view. If the calculation must be nearly live or incrementally updated, an
application-owned projection is a different design with its own replay and reconciliation responsibilities.
