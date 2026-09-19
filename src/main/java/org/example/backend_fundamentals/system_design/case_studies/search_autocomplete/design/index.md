---
order: 20
search: false
---

# Design a Search Autocomplete System

## Agreed scope

Return five globally popular, prefix-only suggestions for normalized English input in under 100 ms. At 10M daily users, assume about 24K average and 48K peak keystroke requests per second. A result may be from the last validated build; fuzzy, substring, personalized, multilingual, and real-time trending results are follow-ups.

## Contract and ownership

```text
GET /v1/autocomplete?q=kaf&limit=5&locale=en

200 OK
{
  "version": "v43",
  "suggestions": ["kafka", "kafka consumer group"]
}
```

`q` is normalized using the same case/whitespace policy as the builder. The client debounces typing and discards a response that no longer matches its latest input. There is intentionally no public popularity-write endpoint: Search Service emits `QuerySearched(eventId, query, locale, occurredAt)` only after executing a submitted search.

| State | Owner | Purpose |
| --- | --- | --- |
| Event log and aggregate counts | Search Service and aggregation pipeline | Durable, reprocessable ranking input; aggregation deduplicates an event ID within its window. |
| Versioned prefix artifact | Builder in replicated durable storage | Recoverable validated output, carrying watermark, policy version, and checksum. |
| Active in-memory trie or prefix map | Serving replicas | Replaceable low-latency copy; no request mutates it. |
| Blocklist | Policy service | Immediate deny decision before a result reaches the client. |

![Autocomplete ownership and workload paths](../assets/search-autocomplete-architecture.svg)

## Chosen paths

### Build

1. Append completed-search events; never count abandoned prefixes.
2. Normalize, deduplicate, and aggregate each query over a selected rolling window.
3. Filter candidates, then build a trie (or flattened prefix map) whose every prefix stores its top five.
4. Persist, checksum, and validate `v43` against fixed queries.
5. Load and warm `v43` beside `v42`; atomically promote only healthy replicas.

The top-K cache changes serving work from a subtree scan and sort to `O(prefix length + K)`. It costs space and build time, which is worthwhile for a bounded `K` and a keystroke path.

### Read

1. Normalize the prefix and route using the locale plus a data-balanced prefix range.
2. Read the active replica's top five, then apply the current blocklist to those candidates.
3. On a cold or failed replica, use another replica; use bounded durable fallback only when it is protected from a herd.
4. Return a quick empty list rather than hanging if no safe serving path remains. Submitted search remains independent.

## Decisions and recovery

| Pressure | Choice | Rejected alternative / recovery |
| --- | --- | --- |
| A popular prefix has too many descendants | Store ranked top five at each prefix. | Live database/trie-subtree ranking has variable latency. Reload a verified artifact after a replica loss. |
| One query touches every ancestor prefix | Immutable, side-by-side snapshot promotion. | Node-by-node live mutation exposes partial ranks; keep `v42` on failed `v43` validation or load. |
| `s` is hotter than `u-z` | Split data-balanced prefix ranges through a shard map. | Full-query hashing destroys prefix locality; warm new shards before changing routes. |
| A suggestion becomes unsafe | Local/replicated deny policy first, durable deletion in the next build. | Waiting for a rebuild leaks it; fewer than five results is the safe degraded outcome. |

## Deferred follow-ups

Locale indexes, time-decayed trending overlays, and a bounded recent-search personalization layer can be added without changing the base artifact's correctness boundary. Substring search needs an inverted/n-gram index; fuzzy completion needs separately bounded candidate generation. Neither belongs on the default prefix request path.

## Quick recall

**Q. Why is an artifact version returned or recorded?**

A. It identifies the coherent snapshot that answered the request and makes stale-index age and rollback observable.

**Q. What is the immediate safety boundary?**

A. The blocklist check before response. Removing the value from the next durable artifact is eventual cleanup.
