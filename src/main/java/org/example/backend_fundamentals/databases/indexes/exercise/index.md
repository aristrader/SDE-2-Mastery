---
order: 10
search: false
---

# Exercise

## Exercise: design-order-query-index - Index a Real Query

`orders` is large and write-heavy. The support dashboard repeatedly runs:

```sql
SELECT id, total
FROM orders
WHERE customer_id = :customer
  AND status = 'PAID'
  AND created_at >= :since
ORDER BY created_at DESC
LIMIT 50;
```

Propose one first index for this query. Explain:

1. why its column order follows the predicate and sort shape;
2. why three single-column indexes are not the default answer;
3. whether a covering/index-only plan is useful and what it costs; and
4. what you would check in `EXPLAIN` before and after the change.

Then change the query to `WHERE status = 'PAID' AND created_at >= :since`. Explain why your original index
may no longer be the right access path rather than promising that every composite index will be used.

## Exercise: investigate-slow-lookup - Diagnose Before Adding an Index

An engineer says, “we already indexed `email`, but this login lookup scans the table”:

```sql
SELECT id, password_hash
FROM users
WHERE LOWER(email) = LOWER(:email);
```

Give the smallest safe diagnosis plan. Include the query/index mismatch, one database-specific repair
option, the write/storage trade-off, and how to confirm the repair in production without guessing from a
single developer-machine result.

## Quick recall

**Q. What should an index answer begin with?**
A. The measured query and access pattern, followed by the read benefit and the write/storage cost.
