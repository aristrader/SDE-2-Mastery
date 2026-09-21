---
order: 10
search: false
---

# Final exercise — LRU Cache LLD

## Exercise: lru-cache-lld - Fixed-Capacity In-Memory Cache

### Goal

Model a generic, fixed-capacity in-memory LRU cache with O(1) average-time reads and writes.

This is the agreed scope after discussing the initial [interviewer prompt](problem_statement/) and
[candidate clarifications](candidate_discussion/).

### Requirements

- Construct the cache with a positive fixed capacity.
- `get(key)` returns a cached value on a hit and has a documented, predictable miss outcome; a hit makes
  its key most recently used.
- `put(key, value)` inserts or updates a value and makes its key most recently used.
- Updating an existing key must not change cache size.
- Inserting a new key into a full cache evicts exactly one least recently used entry.
- Reject `null` keys and values with a clear exception.
- Keep `get` and `put` O(1) on average.

### Constraints

- Keep the base implementation in memory and single-threaded.

### Test scenarios

- Read a missing key and demonstrate the chosen miss outcome.
- Insert and read a value.
- Read one key, insert another at capacity, and verify the untouched least-recently-used key was evicted.
- Update an existing key at capacity and verify no unrelated key is evicted.
- Reinsert a previously evicted key and verify the correct current least-recently-used key is evicted.
- Reject zero/negative capacity and `null` keys or values.

### Interview follow-ups

- How would you make compound recency updates thread-safe?
- How would you add TTL without making every `get` scan the cache?
- How would you enforce memory weight rather than entry count?
- How would you expose cache hit/miss/eviction metrics?
- When would `LinkedHashMap` be sufficient instead of a custom implementation?
