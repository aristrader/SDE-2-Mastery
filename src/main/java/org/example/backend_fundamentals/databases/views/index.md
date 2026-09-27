---
order: 90
---

# Views and Materialized Views

This topic tests whether you can separate three things that look similar in SQL: a reusable query, a stored
derived result, and an application-managed summary table.

## What is a View?

A **view** is a named, stored query that acts like a virtual table. It does not normally store its result.

If you frequently run a complex query with multiple joins, you can save it:

```sql
CREATE VIEW user_orders AS
SELECT u.name, o.amount
FROM users u
JOIN orders o ON u.id = o.user_id;
```

Now you can simply query it like a table:

```sql
SELECT * FROM user_orders;
```

**Important:** the view itself does **not store data**. When queried, the database executes its underlying query
against the current base tables. The optimizer may combine that query with the caller's filter, but the joins and
aggregations have not disappeared.

### Why use Views?

1. **Simplify complex queries:** developers query `sales_report` instead of copying a five-table join.
2. **Expose a deliberate contract:** a view can expose only approved columns or rows to a reporting role.
3. **Centralize business meaning:** one definition of “paid order” avoids several subtly different aggregations.

A view is not automatic security by itself. Permissions, ownership, row-level-security policy, and database-specific
view semantics still decide what a caller may see. Treat the view as one boundary to review, not as proof that the
base tables are protected.

### The Downside

Every access can rerun the underlying joins and aggregations. That is fine when the query is cheap or live data is
required; it becomes expensive for a dashboard that repeatedly scans a large history.

## The normal read path

For a live view, `SELECT * FROM user_orders WHERE name = ?` means: expand the view definition, combine the caller's
predicate where the optimizer can, read the base tables, and return current committed data. A view therefore solves
*repeated query text and an interface boundary*, not a read-performance problem by itself.

Some simple single-table views can be updatable, but a join, aggregation, `DISTINCT`, or grouped reporting view is
normally a read model. Do not make an interview design depend on updating a complex view unless the database-specific
rules or an explicit trigger are part of the scope.

---

## Materialized Views

A **materialized view** stores the query definition **and a precomputed result** physically on disk. It is a database-
managed snapshot of a query, not a magically current cache.

```sql
CREATE MATERIALIZED VIEW user_orders_mv AS
SELECT u.name, o.amount
FROM users u
JOIN orders o ON u.id = o.user_id;
```

Reads use that stored result, avoiding the expensive source query on every dashboard request.

### The refresh contract: speed for freshness

When base tables change, a materialized view becomes stale. It will not reflect them until a refresh recomputes it.

To update it, you must tell the database to rerun the query:

```sql
REFRESH MATERIALIZED VIEW sales_summary;
```

On PostgreSQL, a normal refresh can block readers of that materialized view. `REFRESH MATERIALIZED VIEW CONCURRENTLY`
keeps the previous result available to readers while the replacement is prepared, but requires a qualifying unique
index and cannot be combined with `WITH NO DATA`. It is a availability-versus-refresh-cost choice, not an incremental
refresh guarantee.

```sql
CREATE UNIQUE INDEX sales_summary_day_uq ON sales_summary (day);
REFRESH MATERIALIZED VIEW CONCURRENTLY sales_summary;
```

### Failure and recovery

The naive design is “refresh every night and assume it worked.” If the job fails, readers should receive the last
successful snapshot, while monitoring exposes its `refreshed_at` age and retries or alerts the owner. The product must
state whether an old dashboard is acceptable, whether it should show “data delayed,” or whether that screen instead
needs a live query or a separately maintained read model.

Keep refresh work off an OLTP critical path. A full refresh can consume substantial CPU and I/O, so choose a schedule,
concurrency mode, and capacity budget that do not turn reporting into checkout latency.

### Real-World Use Case

A common system-design pattern:

```text
OLTP database (live transactions)
          ↓ scheduled or event-driven refresh
materialized view (last successful snapshot)
          ↓
analytics dashboard
```

This lets you keep transactional queries fast while serving reporting workloads efficiently without computing heavy aggregations on the fly.

## Pick the right boundary

| Need | Better default | Cost to say out loud |
| --- | --- | --- |
| Reuse a live, readable query or expose a narrow database contract | View | Source query still runs; base-table and view permissions need review. |
| Fast dashboard over data that may be minutes old | Materialized view | Refresh work, stored data, and explicit staleness policy. |
| Low-latency serving model with a domain-specific update path | Application-managed projection/table | The application owns idempotency, ordering, rebuild, and reconciliation. |

An application projection is a follow-up, not a synonym for a materialized view: it can update incrementally, but
the database no longer owns the derivation and refresh lifecycle.

---

## Quick recall

**Q. What is a View?**
A. A virtual table that stores only the query definition. Accessing it executes the underlying query.

**Q. What is a Materialized View?**
A. Stores both the query definition and the precomputed results on disk, trading storage and freshness for significantly faster reads.

**Q. Does a materialized view update when its source table changes?**
A. No. It serves the last refreshed result until a refresh recomputes it; make the acceptable age visible in the product contract.

**Q. Why use `REFRESH ... CONCURRENTLY` in PostgreSQL?**
A. It avoids blocking concurrent readers, but needs a qualifying unique index and can cost more than a blocking refresh.

**Q. Why not just use a normal table instead of a materialized view?**
A. The materialized view's data is derived automatically from a query; the database knows how to refresh it (via `REFRESH`) without requiring manual application logic to keep a summary table synced.

**Q. When should you use a view vs a materialized view?**
A. Use a view to simplify query logic or restrict column access when live data is required. Use a materialized view for heavy analytical dashboards where read speed is critical and slight staleness is acceptable.

