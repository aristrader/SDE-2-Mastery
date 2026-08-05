---
order: 120
---

# Pagination

## Core idea

Pagination controls how clients read a large ordered result set in smaller chunks.

Common API shapes:

```text
GET /transactions?offset=40&limit=20
GET /transactions?cursor=eyJjcmVhdGVkQXQiOiIyMDI2LTA4LTA0VDEwOjAwOjAwWiIsImlkIjo5ODd9&limit=20
GET /transactions?afterId=987&limit=20
```

The interview decision is not syntax. The decision is whether the data is stable enough for offset pagination, or whether you need cursor/keyset pagination because rows are constantly inserted/deleted.

## Offset pagination

Offset pagination says: "skip N rows, then return the next page."

API:

```text
GET /orders?offset=40&limit=20
GET /orders?page=3&size=20
```

SQL:

```sql
SELECT id, created_at, amount
FROM orders
ORDER BY created_at DESC
LIMIT 20 OFFSET 40;
```

Use it for relatively stable result sets:

- admin tables
- reports
- search results where random page jump matters
- small/medium datasets

Main problems:

| Problem | Why it happens |
| --- | --- |
| Slow deep pages | DB still has to walk/skip many rows before returning the page |
| Duplicates under inserts | new rows inserted before page 2 shift the offset |
| Missing rows under deletes | deleted rows before page 2 shift the offset backward |
| Unstable order | `ORDER BY created_at` alone can tie; rows with same timestamp may move around |

Offset is fine when users need "go to page 12" and the dataset is not changing aggressively.

## Cursor pagination

Cursor pagination says: "continue after this last item I already saw."

API:

```text
GET /feed?limit=20
GET /feed?cursor=<opaqueCursor>&limit=20
```

The cursor should usually be opaque to clients. A simple cursor may encode:

```json
{
  "createdAt": "2026-08-04T10:00:00Z",
  "id": 987
}
```

Base64 makes it transport-friendly:

```text
eyJjcmVhdGVkQXQiOiIyMDI2LTA4LTA0VDEwOjAwOjAwWiIsImlkIjo5ODd9
```

Cursor response:

```json
{
  "items": [
    { "id": 987, "createdAt": "2026-08-04T10:00:00Z" }
  ],
  "nextCursor": "eyJjcmVhdGVkQXQiOiIyMDI2LTA4LTA0VDEwOjAwOjAwWiIsImlkIjo5ODd9",
  "hasMore": true
}
```

Use it for changing streams:

- news feeds
- transaction history
- audit logs
- event logs
- chat history
- notifications

## Keyset pagination

Keyset pagination is the database query technique behind most cursor pagination.

Instead of:

```sql
LIMIT 20 OFFSET 100000;
```

use a stable "last seen" key:

```sql
SELECT id, created_at, amount
FROM transactions
WHERE (created_at, id) < (:lastCreatedAt, :lastId)
ORDER BY created_at DESC, id DESC
LIMIT 20;
```

For ascending ID order:

```sql
SELECT id, created_at, amount
FROM transactions
WHERE id > :lastSeenId
ORDER BY id ASC
LIMIT 20;
```

The important rule: the sort order must be stable and indexed.

Good sort keys:

```text
ORDER BY created_at DESC, id DESC
ORDER BY id ASC
ORDER BY event_time DESC, event_id DESC
```

Bad sort keys:

```text
ORDER BY created_at DESC        -- timestamp ties can reorder
ORDER BY display_name ASC       -- names can change and are often non-unique
ORDER BY random_score DESC      -- unstable unless snapshot/versioned
```

## Offset vs cursor vs keyset

| Approach | API shape | Best for | Weakness |
| --- | --- | --- | --- |
| Offset | `?page=3&size=20` or `?offset=40&limit=20` | static/admin pages, random page jumps | slow deep pages; duplicates/misses under writes |
| Cursor | `?cursor=opaque&limit=20` | feeds, logs, timelines, notifications | no easy random page jump |
| Keyset | SQL `WHERE key > lastKey` / `WHERE key < lastKey` | efficient cursor implementation | needs stable indexed ordering |

Interview default:

```text
Offset for stable tables where page jump matters.
Cursor/keyset for high-write feeds, logs, notifications, transactions.
```

## Concurrent modification example

Offset bug:

```text
Initial order:
[100, 99, 98, 97, 96, 95]

Client reads page 1, limit 3:
[100, 99, 98]

New row 101 is inserted at the top.

Client reads page 2 with offset 3:
[98, 97, 96]
```

`98` appears twice because the offset shifted.

Cursor/keyset version:

```text
Client reads first page:
[100, 99, 98]
nextCursor = 98

New row 101 is inserted at the top.

Client reads after cursor 98:
[97, 96, 95]
```

The new row does not disturb the user's current scroll.

## Cursor design rules

- Cursor must include all columns needed to resume the sort.
- Add a unique tie-breaker, usually `id`.
- Treat the cursor as opaque; clients should not construct it.
- Validate cursor shape and reject invalid/tampered cursors.
- Keep ordering deterministic.
- Keep `limit` bounded, for example max 100.
- Use indexes matching the filter and order.

Example index:

```sql
CREATE INDEX idx_transactions_user_created_id
ON transactions(user_id, created_at DESC, id DESC);
```

Matching query:

```sql
SELECT id, created_at, amount
FROM transactions
WHERE user_id = :userId
  AND (created_at, id) < (:lastCreatedAt, :lastId)
ORDER BY created_at DESC, id DESC
LIMIT :limit;
```

## Previous page support

Forward-only cursor is simplest.

If the product needs previous-page navigation, either:

- return `previousCursor` too, or
- reverse the sort direction for the previous query, then reverse results before returning.

Do not add previous-page complexity unless the UI actually needs it. Infinite scroll usually only needs next cursor.

## Snapshot pagination

Sometimes you need a stable snapshot: page 1 and page 2 should reflect the same dataset even if new rows arrive.

Options:

- include `snapshotTime` in the cursor
- query only rows `created_at <= snapshotTime`
- use a search backend snapshot/point-in-time token if available

Example:

```json
{
  "snapshotTime": "2026-08-04T10:00:00Z",
  "createdAt": "2026-08-04T09:55:00Z",
  "id": 987
}
```

This is useful for audit/export/search-like flows. Feeds usually do not need strict snapshots.

## Common API response

```json
{
  "data": [
    {
      "id": 987,
      "createdAt": "2026-08-04T10:00:00Z",
      "amount": "499.00"
    }
  ],
  "pageInfo": {
    "nextCursor": "opaque",
    "hasMore": true,
    "limit": 20
  }
}
```

Avoid returning raw DB offset as the cursor. A cursor should represent position in the ordered result, not "row number 40".

## Interview traps

| Question | Strong answer |
| --- | --- |
| Why is large `OFFSET` slow? | DB still scans/skips rows before returning the page; keyset jumps from the last seen key |
| Why can offset miss/duplicate rows? | Inserts/deletes before the current offset shift subsequent pages |
| Why include `id` with `created_at`? | `created_at` can tie; `id` makes ordering deterministic |
| Can cursor support random page 20? | Not naturally; use offset/search if random jumps are required |
| Is Base64 cursor secure? | No, it is encoding, not encryption/signing; sign if tamper matters |
| What if sort field can change? | Cursor becomes unstable; prefer immutable sort keys or snapshot/versioned ordering |

## Quick recall

**Q. Offset pagination means what?**
A. Skip N rows and return the next `limit` rows.

**Q. Cursor pagination means what?**
A. Continue after the last item the client already saw.

**Q. Keyset pagination means what?**
A. Use indexed sort keys in `WHERE` instead of `OFFSET`.

**Q. What is the strongest default for feeds?**
A. Cursor/keyset pagination with `created_at` plus `id`.

**Q. Why is `ORDER BY created_at` alone weak?**
A. Multiple rows can share the same timestamp, causing unstable ordering.

**Q. When is offset acceptable?**
A. Stable admin/report tables where random page jumps matter more than write-stability.
