---
order: 30
---

# Database Indexes (B+ Trees)

Without an index, `SELECT * FROM users WHERE id = 500000` scans rows — O(n). An index lets the engine jump near-directly to the row — like jumping to a page via a book's index instead of reading from page one.

## Why not just a hash index

A hash index handles `WHERE id = 100` well but **cannot do range queries efficiently** — hashing destroys ordering:

```text
100 → bucket 7
101 → bucket 31
102 → bucket 2     ← no concept of "the next value"
```

So `WHERE id > 100`, `BETWEEN`, `ORDER BY`, and `LIKE 'prefix%'` all fall back to a full scan on a hash index. A **B+ tree keeps data sorted**, so all of those are cheap. That's why relational engines default to B+ trees, not hash maps.

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

So a billion-row lookup is ~3–4 page reads from root to leaf.

**Internal nodes** hold only keys + child pointers — pure navigation. **Leaf nodes** hold the actual entries (full rows for a clustered index, or indexed-value + PK for a secondary index). Leaves are linked left-to-right so a range scan walks siblings without re-descending.

**Why not store data in internal nodes?** It would shrink fanout (fewer keys per page) → taller tree → more page reads. Keeping internal nodes navigation-only maximizes fanout.

## Page splits, rebalancing, fragmentation

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

**Two-hop lookup** for a secondary index: secondary index → PK → clustered index → row. Still fast because each tree is shallow:

```text
3 page reads (secondary) + 3 page reads (clustered) = ~6 reads
vs. thousands for a full scan
```

## Dense vs sparse indexes

- **Dense:** one index entry per row (`1→Row1, 2→Row2, 3→Row3`).
- **Sparse:** one entry per page/section — find the nearest entry, then scan within the page (`1→Page1, 5→Page2, 9→Page3`).

**A B+ tree is both, by level:** **leaf level is dense** (every key present), while **internal levels are sparse** — they hold only separator keys used for routing.

## NULLs in indexes

Indexes do store NULLs, and `WHERE email IS NULL` can use the index. In a **unique** index, multiple NULLs are allowed because NULL is not equal to NULL (SQL three-valued logic).

Physically, the B+ tree just needs a **consistent ordering** — treat `NULL < everything`, cluster all NULL entries on the left, and `WHERE x IS NULL` traverses to the left-most leaves and walks the linked leaves. The SQL *meaning* of NULL is separate from the B+ tree's ordering mechanics.

## Quick recall

**Q. Why a B+ tree instead of a hash index?**
A. Hash supports equality only — no ordering, so range/ORDER BY/prefix queries need a full scan. B+ trees keep keys sorted, making all of those cheap.

**Q. What cost does a B+ tree actually minimize?**
A. Page reads, not comparisons. Pages are the I/O unit; comparisons are cheap. Huge fanout keeps the tree 3–5 levels deep so any row is a few page reads away.

**Q. Why are AUTO_INCREMENT keys faster to insert than random UUIDs?**
A. Sequential keys always hit the right-most page (locality, few splits, stable old pages); random UUIDs scatter inserts → page splits, fragmentation, cache misses — worst in a clustered index where leaves hold full rows.

**Q. Clustered vs secondary index?**
A. Clustered leaf holds the full row (one per table, IS the table in InnoDB); secondary leaf holds indexed-value + PK and does a two-hop lookup (secondary → PK → clustered → row).

**Q. Is a B+ tree dense or sparse?**
A. Both: leaves are dense (every key), internal levels are sparse (separator keys for routing).

**Q. Where do page splits stop?**
A. Usually local (leaf + parent). They only cascade upward when parents are full; a full root splits and increases tree height by one — the only way the tree grows taller.


