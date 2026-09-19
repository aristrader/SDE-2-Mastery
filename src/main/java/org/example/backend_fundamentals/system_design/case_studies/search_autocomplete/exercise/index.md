---
order: 10
search: false
---

# Search Autocomplete Exercise

## Exercise: search-autocomplete-hld - Design Typeahead

### Goal

Practice an HLD answer for a global prefix autocomplete system that returns five popular suggestions in under 100 ms.

### Task

Give a 35–40 minute answer. Begin with the deliberately narrow contract, then show why a direct ranked database query on every keystroke fails before naming a trie. Design both the asynchronous build path and the request path. Include:

- search-event logs and windowed aggregation
- trie snapshot construction
- top-K results stored per prefix node
- persistent trie DB and in-memory trie cache
- request filtering and browser caching
- prefix-range sharding with a shard map
- a clear boundary between stable batch results and trending results
- versioned snapshot rollout that avoids a cache stampede
- a completed-search event emitted by Search Service rather than a client-side popularity write
- what is durable, what is an in-memory serving copy, and what the user sees when the serving tier is unhealthy

### Acceptance criteria

- Derive QPS from keystrokes, not only completed searches.
- Explain why a direct SQL frequency query is not the read-path design at scale.
- Explain why cached top-K trie nodes avoid subtree traversal and sorting.
- State the complexity as `O(prefix length + K)` and explain why it is practically constant here.
- Trace the accepted `QuerySearched` event through a validated versioned artifact, then trace one prefix read through the active version.
- Use a filter layer for immediate blocked-suggestion removal.
- Explain why full-query hashing breaks prefix-subtree locality.
- Explain why a trie is correct for prefix matching but not automatically for substring or fuzzy matching.
- State the recovery rule for duplicate events, an incomplete new snapshot, a replica outage, and a stale client response.
