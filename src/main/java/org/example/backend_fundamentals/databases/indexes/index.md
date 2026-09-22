---
order: 30
---

# Database Indexes (B+ Trees)

Without an index, `SELECT * FROM users WHERE id = 500000` scans rows — O(n). An index lets the engine jump near-directly to the row — like jumping to a page via a book's index instead of reading from page one.

This page uses the **B+ tree** as the common mental model for a sorted disk index. Exact page layout and
clustered-index behaviour are engine-specific; the InnoDB examples below are labelled as such. Start an
interview answer from the query shape and write cost, not from "indexes make reads fast."

## Why not just a hash index

A hash index handles `WHERE id = 100` well but **cannot do range queries efficiently** — hashing destroys ordering:

```text
100 → bucket 7
101 → bucket 31
102 → bucket 2     ← no concept of "the next value"
```

So that **hash index** cannot answer `WHERE id > 100`, `BETWEEN`, `ORDER BY`, or `LIKE 'prefix%'` by
ordered traversal; the optimizer must use another access path or scan. A **B+ tree keeps keys sorted**, so
it can support those patterns. PostgreSQL, for example, creates a B-tree by default, while its hash index
supports simple equality only.

## Pages: the unit of I/O

Databases think in **pages** (e.g. 16 KB), not rows. A read pulls an entire page, not a single row. The key cost asymmetry:

- **Expensive:** reading a page (disk / cache miss).
- **Cheap:** comparing values in memory.

So the engine optimizes for **fewer page reads**, not fewer comparisons:

```text
Option A: 1000 comparisons,  3 page reads   ← faster
Option B:   20 comparisons, 20 page reads
```

**Why plain binary search isn't enough:** binary search on disk-ordered rows jumps to page 5,000,000, then 2,500,000, then 1,250,000 … — log₂(n) *page reads*, which is many disk hits. The B+ tree minimizes page reads via huge fanout, not comparison count.

## B+ tree structure

The trick is a huge **branching factor (fanout)** — a node has hundreds or thousands of children, not two:

```text
fanout ≈ page size / (key size + pointer size)
       = 16 KB / (8 B + 8 B) ≈ 1000 children per node
```

That makes the tree extremely shallow — height 3–5 covers enormous tables:

```text
Level 1: 1,000
Level 2: 1,000,000
Level 3: 1,000,000,000
```

So a billion-row lookup can be only a few page reads from root to leaf. Actual fanout and cache hits depend
on the engine, page size, key width, row layout, and workload.

**Internal nodes** hold only keys + child pointers — pure navigation. **Leaf nodes** hold the actual entries (full rows for a clustered index, or indexed-value + PK for a secondary index). Leaves are linked left-to-right so a range scan walks siblings without re-descending.

**Why not store data in internal nodes?** It would shrink fanout (fewer keys per page) → taller tree → more page reads. Keeping internal nodes navigation-only maximizes fanout.

## Page splits, rebalancing, fragmentation

The following is the **textbook B+ tree model**. Storage engines can use different deletion, merge, and
background-maintenance strategies, so use it to reason about write locality rather than as a vendor contract.

- **Page split:** a full leaf receives an insert with no room → it splits into two, and the parent is updated with the new separator key.
- **Rebalancing is local:** usually only the leaf and its parent change — *not* a full-tree rebuild. A split only propagates upward if the parent is also full.
- **Root split:** if splits cascade all the way up and the root is full, the root splits and **tree height grows by one** — the only way a B+ tree gets taller.
- **Underflow:** a delete leaves a page mostly empty → borrow from a sibling, or merge pages.
- **Fragmentation:** random inserts/deletes leave half-empty, scattered pages → larger indexes → more page reads.

## AUTO_INCREMENT vs UUID primary keys

This is the canonical "why is UUID worse" question — it's about **write locality in the B+ tree**.

- **AUTO_INCREMENT (1, 2, 3, …):** every insert lands on the right-most leaf page. Only one region of the tree is ever touched; old pages become stable and are never revisited. Result: better cache locality, fewer page splits, predictable writes.
- **Random UUID:** insert positions are scattered across the whole tree:

  ```text
  insert1 → page 52
  insert2 → page 900
  insert3 → page 127
  insert4 → page 3000
  ```
  Result: random page modifications, constant re-splitting of old pages, more fragmentation, more cache misses.

**Worse in a clustered index specifically:** the leaf holds the *full row*, so a UUID-driven page split moves entire (possibly large) rows around — far more expensive than moving a secondary index's slim entries. (Mitigation: time-ordered UUIDv7 restores append-like locality.)

## Clustered vs secondary vs non-clustered

- **Clustered index:** the primary-key B+ tree whose **leaves contain the full row**. In InnoDB, *the table is the clustered index*. Only one is possible — data can be physically sorted exactly one way.
- **Secondary index:** a separate B+ tree whose leaf holds `indexed value + primary key` (e.g. `email → PK`), **not** the full row. Storing the full row in every secondary index would explode storage and write amplification.
- **Non-clustered index:** leaf stores a pointer/reference rather than the row (SQL Server terminology); MySQL secondary indexes are conceptually similar.

**InnoDB two-hop lookup:** secondary index → PK → clustered index → row. It is still often fast because each
tree is shallow, but it is not free:

```text
3 page reads (secondary) + 3 page reads (clustered) = ~6 reads
vs. thousands for a full scan
```

PostgreSQL uses a heap-table storage model rather than an always-maintained clustered index in this sense;
the same words do not imply the same physical path across engines. State the engine before claiming its
exact storage behaviour.

## Choose an index from a query, then prove it

The useful question is not "which columns look important?" It is: which predicate narrows this query, which
order must it return, and which columns must it fetch?

```sql
SELECT id, total
FROM orders
WHERE customer_id = :customer
  AND status = 'PAID'
  AND created_at >= :since
ORDER BY created_at DESC
LIMIT 50;
```

The naive answer is one index per filtered column. That gives the optimizer several partial choices but
also makes every insert/update maintain several structures. A candidate B-tree is usually shaped around the
access path: `(customer_id, status, created_at)`. Equality filters on the leading columns narrow the range;
the range/order column comes after them. A B-tree can then seek to one customer's paid rows and walk the
recent part of that ordered run.

### Composite order, coverage, and selectivity

With a B-tree on `(a, b, c)`, constraints on the leading columns normally determine how much of the tree
must be scanned. PostgreSQL can sometimes use a skip scan for a later column, but that is a planner choice,
not a licence to ignore column order. For the shown query, both `(customer_id, status, created_at)` and
`(status, customer_id, created_at)` have equality constraints on their first two columns and can reach the
same combined range. Choose between them from the wider workload—for example, whether customer-only queries
also need the prefix—not from `status` selectivity alone.

A **covering index** contains every value a query needs, so an engine may avoid fetching the base row. That
can save a lookup but widens the index and increases write cost; the exact feature differs by engine
(`INCLUDE` columns in PostgreSQL, primary-key columns already carried by an InnoDB secondary index).

**Selectivity** means how strongly a predicate narrows rows. `email = ?` is usually selective; `status =
'PAID'` may match most of the table. An index can be perfectly valid yet lose to a sequential scan when the
planner estimates that following many index entries and fetching rows costs more.

### Recovery: use the plan, not intuition

After adding or changing an index, run the engine's plan tool (`EXPLAIN`, and `EXPLAIN ANALYZE` only when
safe for the environment). Check the predicate, estimated versus actual rows, scan type, sort, and row
fetches. If the plan is still poor, repair the query shape, statistics, or index—not just add another index.
Drop an unused index after confirming its workload is gone; it otherwise remains a permanent write and
storage tax.

## Dense vs sparse indexes

- **Dense:** one index entry per row (`1→Row1, 2→Row2, 3→Row3`).
- **Sparse:** one entry per page/section — find the nearest entry, then scan within the page (`1→Page1, 5→Page2, 9→Page3`).

As a **textbook mental model**, leaves are often called dense because they hold the searchable entries, while
internal levels are sparse routing separators. Real engines may deduplicate equal keys, use posting lists,
or store records differently, so do not turn that analogy into a portable physical-storage claim.

## NULLs in indexes

Whether `NULL` is indexed and how uniqueness treats it are **engine and configuration details**. PostgreSQL
B-tree indexes can support `IS NULL` and `IS NOT NULL`; by default its unique constraints treat two `NULL`
values as distinct, with a `NULLS NOT DISTINCT` option for the opposite rule. Do not rely on a made-up
physical ordering rule such as "NULL is always left-most"—ask the engine and its collation/operator class.

The SQL meaning of `NULL` and a particular B-tree's physical ordering are separate questions. Model the
business rule explicitly: `UNIQUE` alone may allow several missing values, while `NOT NULL` plus `UNIQUE`
requires one real value per row.

## Quick recall

**Q. Why a B+ tree instead of a hash index?**
A. Hash supports equality only — no ordering, so range/ORDER BY/prefix queries need a full scan. B+ trees keep keys sorted, making all of those cheap.

**Q. What cost does a B+ tree actually minimize?**
A. Page reads, not comparisons. Pages are the I/O unit; comparisons are cheap. Huge fanout keeps the tree 3–5 levels deep so any row is a few page reads away.

**Q. Why are AUTO_INCREMENT keys faster to insert than random UUIDs?**
A. Sequential keys always hit the right-most page (locality, few splits, stable old pages); random UUIDs scatter inserts → page splits, fragmentation, cache misses — worst in a clustered index where leaves hold full rows.

**Q. Clustered vs secondary index?**
A. In InnoDB, the clustered leaf holds the full row (one per table) and a secondary entry carries the PK for
the second lookup. Other engines use different storage, so name the engine before claiming this path.

**Q. Is a B+ tree dense or sparse?**
A. In the textbook model, leaves hold searchable entries while internal levels hold sparse routing
separators. Real engines can represent duplicate keys and leaves differently.

**Q. Where do page splits stop?**
A. Usually local (leaf + parent). They only cascade upward when parents are full; a full root splits and increases tree height by one — the only way the tree grows taller.

**Q. How do you choose a composite index?**
A. Start from a measured query: put equality filters on useful leading columns, then the range/order column;
verify the plan and account for the read benefit, write cost, and engine-specific exceptions.

**Q. Why can an existing index be ignored?**
A. A low-selectivity predicate, incompatible expression/operator, stale statistics, or a cheaper scan/sort
can make the optimizer choose another plan. Read `EXPLAIN` before adding indexes.
