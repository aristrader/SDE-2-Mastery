---
order: 20
search: false
---

# Solution

## Solution: design-order-query-index - Index a Real Query

Start with one candidate B-tree:

```sql
CREATE INDEX idx_orders_customer_status_created
ON orders (customer_id, status, created_at DESC);
```

`customer_id = ?` and `status = 'PAID'` are leading equality constraints. They narrow the B-tree to one
customer/status run; `created_at` then gives the range and requested order, allowing the engine to stop
after the newest 50 qualifying entries. The descending declaration is engine-specific and should be kept
only when the target engine/plan benefits from it.

Three one-column indexes make the write path maintain three structures and may leave the optimizer to
combine partial results or choose only one. They do not express the ordered three-column access path. A
covering version can include `id` and `total` when the plan shows row fetches dominate, but it makes every
index entry wider. In InnoDB the primary key is already stored with secondary entries; other projected
columns still cost space. In PostgreSQL, consider `INCLUDE (id, total)` after confirming an index-only scan
is realistic for the table's visibility state.

Before and after, inspect `EXPLAIN` for the chosen index, rows scanned/returned, remaining filter, sort,
and estimated cost. Use `EXPLAIN ANALYZE` only where executing the query is safe, then compare representative
production-like parameter values rather than one unusually selective customer.

For `status = 'PAID' AND created_at >= :since`, the original index has no useful leading
`customer_id` condition. A planner might skip-scan it in some engines/data distributions, but do not design
around that possibility. Measure whether a dedicated `(status, created_at)` index, a partial index for a
rare status, or a sequential scan is actually cheaper; a common `PAID` status may not be selective enough
to justify another write cost.

## Solution: investigate-slow-lookup - Diagnose Before Adding an Index

The plain `email` B-tree is not automatically equivalent to `LOWER(email)`: the query applies an expression
before comparison. First inspect the plan and the actual column type/collation, then make the comparison and
index agree. Depending on the engine and product rule, use a case-insensitive column/type, normalize email
to one canonical form at the write boundary, or create an expression index such as:

```sql
CREATE INDEX idx_users_lower_email ON users (LOWER(email));
```

An expression index is maintained whenever `email` changes, so it consumes storage and adds write work. If
the product requires case-insensitive identity, pair the chosen model with the appropriate uniqueness rule;
an index that merely makes lookup fast does not by itself prevent duplicate accounts. Roll it out through
the database's production-safe index-build procedure, inspect the plan and real latency/error metrics, and
remove the obsolete index only after confirming no workload still needs it.

## Quick recall

**Q. Why do equality columns normally precede a range column in a composite B-tree?**
A. They narrow the run first; the range then walks a small ordered segment. Verify with the engine plan—this
is a strong default, not a magic rule.

**Q. Why can `LOWER(email)` miss an `email` index?**
A. The indexed value and predicate expression differ. Use a matching expression index or a deliberate
canonical/case-insensitive data model.
