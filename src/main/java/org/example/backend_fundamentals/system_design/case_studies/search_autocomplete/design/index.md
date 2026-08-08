---
order: 20
search: false
---

# Design a Search Autocomplete System

## Problem

Design a search-as-you-type service that returns the five most popular stored queries matching a typed prefix. Assume 10M daily active users, English lowercase query strings, prefix-only matching, no spell correction, and a sub-100 ms target.

## Clarify scope

Ask:

- Does matching apply only at the beginning of the query, or can it match in the middle?
- How many suggestions are returned, and what ranks them?
- Are results global, country-specific, personalized, or trending?
- How fresh must the ranking be?
- Are spell correction, special characters, and multiple languages in scope?
- Is it safe to cache results in browsers and shared caches?

The base answer assumes prefix matching, top 5 by historical frequency, and periodically rebuilt global results.

## Requirements

### Functional

- Return five ranked suggestions for a prefix.
- Record completed search queries for future rankings.
- Remove unsafe suggestions quickly.
- Rebuild and publish updated suggestion data without interrupting reads.

### Non-functional

- Serve autocomplete below 100 ms.
- Support roughly 24K average QPS and 48K peak QPS from keystrokes.
- Keep query serving highly available and cache-first.
- Scale storage without making popular prefix ranges hot partitions.

## API

```text
GET /v1/autocomplete?q=kaf&limit=5&locale=en-IN

200 OK
{
  "prefix": "kaf",
  "suggestions": [
    { "query": "kafka", "score": 98231 },
    { "query": "kafka consumer group", "score": 40218 }
  ]
}
```

The client should debounce input and discard responses that do not match its latest prefix.

There is intentionally no public `POST /save-query` in the base design. The Search Service emits `QuerySearched` only after it executes an actual submitted search. This avoids counting keystrokes and avoids trusting a redundant client-side popularity write.

## Architecture

![Search autocomplete architecture](../assets/search-autocomplete-architecture.svg)

| Component | Responsibility |
| --- | --- |
| Search-event log | Append-only input for analytics and ranking. |
| Aggregator | Produces query-frequency totals for a fixed time window. |
| Trie workers | Build a new trie snapshot and top-K lists. |
| Trie DB | Persists completed trie snapshots or `prefix -> node` records. |
| Trie cache | Serves the active trie snapshot from memory. |
| Filter service | Blocks unsafe or disallowed suggestions before response. |
| API servers | Read cache, enforce request limits, and return suggestions. |
| Shard-map manager | Routes prefix ranges to balanced trie shards. |

## Trie representation

Each trie node represents a prefix and stores references to its top 5 completed query strings. For `tr`, the node might contain `[true, try, tree]` ordered by score.

```text
prefix "tr" -> node.topSuggestions = [true (35), try (29), tree (10)]
```

This makes a request `O(prefix length + K)`. Do not traverse the entire subtree and sort candidates at request time.

## Build path

1. Search Service emits `QuerySearched`; events are appended to analytics logs.
2. Aggregators calculate frequency by query and time window.
3. Workers build and validate a complete new trie snapshot.
4. Workers persist it in trie DB.
5. Cache replicas load it and traffic switches atomically from the old snapshot to the new snapshot.

Use snapshot replacement for the base solution. Incremental updates must recompute the top-K list for the changed query and every ancestor prefix, which is expensive at high event volume.

The snapshot or its aggregated query-frequency inputs are durable and replicated. Serving replicas load the active snapshot into RAM. The trie is a data structure, not a requirement for a special database product.

### Publish without a thundering herd

Workers build and validate `v43` while clients continue reading `v42`. Cache replicas load `v43` in the background and switch to it atomically only after it is ready. Never invalidate the active trie first and let every request miss to durable storage. A replacement replica recovers by loading the current version; it does not rebuild the trie.

## Read path

1. Client sends a debounced prefix request.
2. API server routes to the shard for the prefix range.
3. Filter layer rejects blocked results.
4. API server reads the prefix node and its top 5 from trie cache.
5. On a miss, it fetches the node from trie DB and repopulates cache.
6. Client displays results without a full-page refresh.

## Scale and follow-ups

Partition by data-balanced prefix ranges, not equal alphabet ranges. A high-volume `s` range may need more shards than `u-z` combined. The shard map provides this prefix-to-shard lookup and supports deeper range splits.

For locale-specific results, build separate tries per locale and put read replicas near users. For trending results, merge a recency-weighted stream-processing overlay with the stable snapshot. For personalized results, check a small user-recent-search layer first.

Use a rolling historical window, a minimum-frequency threshold, and time decay to keep results relevant and bound trie size. Rebuild frequency and retention window are independent: hourly builds can still use the last 30 days of events.

### Scope boundaries

The trie solves prefix lookup. Infix/substring matching needs a different index, typically tokens in an inverted index or character n-grams. Fuzzy matching is a separate requirement, commonly backed by optimized n-gram or search-engine capabilities; do not scan every query with edit distance in the base request path.

A graph database is not the default trie store because this is deterministic prefix traversal, not multi-hop relationship exploration. A time-series store can be useful for windowed counts, but it does not replace the serving trie.

## Failure handling

| Scenario | Response |
| --- | --- |
| Cache replica fails | Route to another replica; fall back to trie DB on a miss. |
| Build job fails | Keep serving the last validated snapshot. |
| Suggestion becomes unsafe | Block it immediately in filter layer; delete it from the next snapshot asynchronously. |
| Popular prefix overloads a shard | Split its prefix range more deeply and update shard map. |
| Stale browser response arrives | Client discards it because its prefix is no longer current. |

## Quick recall

**Q. What is the main serving data structure?**
A. A trie whose nodes cache the top-K suggestions for their prefixes.

**Q. Why are immutable snapshots useful?**
A. They let the system build and validate new ranking data without disrupting reads on the old snapshot.

**Q. How are unsafe suggestions removed quickly?**
A. A filter runs before cache results are returned; durable removal follows asynchronously.

**Q. What is needed for real-time trending suggestions?**
A. Stream aggregation and a recency-weighted overlay, not only periodic trie rebuilds.

**Q. Why is sticky session routing not a consistency solution?**
A. It only pins users to independently stale mutable tries; serving state must come from shared versioned data.
