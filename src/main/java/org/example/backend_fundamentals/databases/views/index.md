---
order: 90
---

# Views and Materialized Views

This is a common interview topic because it tests whether you understand the difference between **storing data** and **storing a query**.

## What is a View?

A **View** is basically a saved SQL query. It acts as a virtual table. 

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

**Important:** The view itself does **not store data**. When you query the view, the database dynamically expands it into the underlying query and executes it against the actual tables.

### Why use Views?

1. **Simplify Complex Queries:** Developers can query `sales_report` instead of writing a 5-table join every time.
2. **Security:** You can create a view that excludes sensitive columns (e.g., `salary`) and grant analysts access only to the view, not the underlying table.
3. **Consistency:** Ensure everyone uses the same business logic for common aggregations.

### The Downside

Every time you query a view, the underlying joins and aggregations run again. For massive datasets, this becomes too expensive for frequent dashboard reads.

---

## Materialized Views

A **Materialized View** stores both the query definition **and the precomputed query results** physically on disk.

```sql
CREATE MATERIALIZED VIEW user_orders_mv AS
SELECT u.name, o.amount
FROM users u
JOIN orders o ON u.id = o.user_id;
```

When you query a materialized view, the database simply reads the stored results — making reads extremely fast.

### The Trade-off: Stale Data

Because the data is precomputed, if the underlying tables change, the materialized view becomes stale. It will not reflect the new data until it is refreshed.

To update it, you must tell the database to rerun the query:

```sql
REFRESH MATERIALIZED VIEW sales_summary;
```

### Real-World Use Case

A common system-design pattern:

```text
OLTP Database (live transactions)
          ↓ (nightly job refreshes)
Materialized Views
          ↓
Analytics Dashboards
```

This lets you keep transactional queries fast while serving reporting workloads efficiently without computing heavy aggregations on the fly.

---

## Quick recall

**Q. What is a View?**
A. A virtual table that stores only the query definition. Accessing it executes the underlying query.

**Q. What is a Materialized View?**
A. Stores both the query definition and the precomputed results on disk, trading storage and freshness for significantly faster reads.

**Q. Why not just use a normal table instead of a materialized view?**
A. The materialized view's data is derived automatically from a query; the database knows how to refresh it (via `REFRESH`) without requiring manual application logic to keep a summary table synced.

**Q. When should you use a view vs a materialized view?**
A. Use a view to simplify query logic or restrict column access when live data is required. Use a materialized view for heavy analytical dashboards where read speed is critical and slight staleness is acceptable.
