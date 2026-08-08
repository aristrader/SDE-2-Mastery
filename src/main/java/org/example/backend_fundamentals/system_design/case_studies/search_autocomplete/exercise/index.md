---
order: 10
search: false
---

# Search Autocomplete Exercise

## Exercise: search-autocomplete-hld - Design Typeahead

### Goal

Practice an HLD answer for a global prefix autocomplete system that returns five popular suggestions in under 100 ms.

### Task

Design both the asynchronous data path and the request path. Include:

- search-event logs and windowed aggregation
- trie snapshot construction
- top-K results stored per prefix node
- persistent trie DB and in-memory trie cache
- request filtering and browser caching
- prefix-range sharding with a shard map
- a clear boundary between stable batch results and trending results
- versioned snapshot rollout that avoids a cache stampede
- a completed-search event emitted by Search Service rather than a client-side popularity write

### Acceptance criteria

- Derive QPS from keystrokes, not only completed searches.
- Explain why a direct SQL frequency query is not the read-path design at scale.
- Explain why cached top-K trie nodes avoid subtree traversal and sorting.
- State the complexity as `O(prefix length + K)` and explain why it is practically constant here.
- Use a filter layer for immediate blocked-suggestion removal.
- Explain why full-query hashing breaks prefix-subtree locality.
- Explain why a trie is correct for prefix matching but not automatically for substring or fuzzy matching.
