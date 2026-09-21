---
search: false
---

# Candidate discussion — LRU Cache

Start by proposing a narrow scope:

> “I will model an in-memory generic cache with a fixed entry capacity. Reads and writes will make a key
> most recently used; inserting a new key beyond capacity will evict the least recently used key.”

Then confirm the decisions that change the model.

## Questions to ask

1. Is capacity a fixed positive number of entries, or must entries have different weights?
2. Should this cache be generic for arbitrary key-value types, or dedicated to one value type?
3. What should `get` return for a missing key: `Optional`, `null`, or a value sentinel?
4. Should updating an existing key also refresh its recency?
5. Are `null` keys or values allowed?
6. Must `get` and `put` be thread-safe in this first version?
7. Are expiry, cache statistics, persistence, and eviction callbacks part of the round?

## Agreed scope for this exercise

- One in-memory, generic cache has a fixed positive entry capacity.
- `get(key)` has a documented, predictable cache-miss outcome and refreshes a present key as most
  recently used.
- `put(key, value)` rejects `null`, updates an existing key in place, and makes it most recently used.
- A new key inserted at capacity evicts exactly one least recently used entry.
- `get` and `put` run in O(1) on average.
- Thread safety, TTL, weighted capacity, persistence, metrics, and eviction callbacks are follow-ups.

The final [exercise](../) records the resulting requirements and test scenarios.
