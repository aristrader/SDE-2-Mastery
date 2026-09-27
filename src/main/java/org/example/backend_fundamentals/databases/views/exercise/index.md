---
order: 10
search: false
---

# Exercise

## Exercise: revenue-dashboard - Choose a reporting boundary

`orders` and `payments` receive writes throughout the day. Finance needs a dashboard showing daily paid revenue by
country. It can be up to 15 minutes old, but checkout must not slow down.

1. Explain why a normal view is useful but does not remove the repeated aggregation cost.
2. Choose a materialized view or an application-managed projection. State the refresh/freshness contract.
3. The refresh job fails twice. What should the dashboard show, and what should operations do?
4. If reads must continue during PostgreSQL refresh, name the extra requirement for `REFRESH ... CONCURRENTLY`.

## Quick recall

**Q. What is the first clarification?**

A. Whether the dashboard needs current committed data or an explicitly bounded stale snapshot.
