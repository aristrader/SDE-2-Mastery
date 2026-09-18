---
order: 10
search: false
---

# Exercise

## Exercise: cap-per-operation - Choose the partition response

During a network partition, an application has two operations:

- reserve the final unit of stock;
- show a product description that may be a few minutes old.

For each operation, state whether you prefer a CP or AP response, what the caller receives during the
partition, and how the system recovers after it ends.

## Answer shape

The reservation should not be accepted without the consistency mechanism that prevents double allocation;
return a retryable/pending response. The description can be served from a local replica if bounded staleness
is acceptable, then reconciled. Explain the invariant; do not answer with only an acronym.

## Quick recall

**Q. What is the CAP choice during a partition?**
A. Whether an operation fails rather than risk inconsistency, or returns an available but potentially stale result.
