# LRU Cache — entity identification and class diagrams

## Goal

Model a generic, fixed-capacity in-memory LRU cache with O(1) average-time reads and writes.

## Constraints

- Keep the base implementation in memory and single-threaded.

## Test scenarios

- Read a missing key and demonstrate the chosen miss outcome.
- Insert and read a value.
- Read one key, insert another at capacity, and verify the untouched least-recently-used key was evicted.
- Update an existing key at capacity and verify no unrelated key is evicted.
- Reinsert a previously evicted key and verify the correct current least-recently-used key is evicted.
- Reject zero/negative capacity and `null` keys or values.

## Interview follow-ups

- How would you make compound recency updates thread-safe?
- How would you add TTL without making every `get` scan the cache?
- How would you enforce memory weight rather than entry count?
- How would you expose cache hit/miss/eviction metrics?
- When would `LinkedHashMap` be sufficient instead of a custom implementation?

## Requirements

- Construct the cache with a positive fixed capacity.
- `get(key)` returns a cached value on a hit and has a documented, predictable miss outcome; a hit makes
  its key most recently used.
- `put(key, value)` inserts or updates a value and makes its key most recently used.
- Updating an existing key must not change cache size.
- Inserting a new key into a full cache evicts exactly one least recently used entry.
- Reject `null` keys and values with a clear exception.
- Keep `get` and `put` O(1) on average.

## Entity identification
LruCache

## Class diagrams

// has to be generic since we may wanna support any key and value pair.
LruCache<K,V>
- head : Node<K,V>
- tail : Node<K,V>
- nodeMap : HashMap<K, Node<K,V>>
- capacity : int
+ LruCache(int capacity)
+ get(K key) : V
+ put(K key, V value) : void

Node<K,V>
- key : K
- value : V
- prev : Node<K,V>
- next : Node<K,V>
+ Node(K key, V value)
+ getKey() : K
+ getValue() : V
+ getNext() : Node<K,V>
+ getPrev() : Node<K,V>