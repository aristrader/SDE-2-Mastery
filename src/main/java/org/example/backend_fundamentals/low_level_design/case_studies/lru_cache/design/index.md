---
order: 20
search: false
---

# LRU Cache — Improved Design Reference

Read this after attempting the [entity-identification and class-diagram worksheet](../playground/entity_identification_and_class_diagrams.md).
It is an improved explanation of that exact two-class model and the runnable implementation; it does not
replace your worksheet.

## Your model: two structures, one entry

Your `LruCache<K, V>` owns the public API, the map, capacity, and sentinel nodes. Your `Node<K, V>` is one
real cache entry, linked by `prev` and `next`. The map points to those same nodes; it is not a second copy
of each entry.

```mermaid
classDiagram
    class LruCache~K,V~ {
        -Node~K,V~ head
        -Node~K,V~ tail
        -Map~K,Node~K,V~~ nodeMap
        -int capacity
        +LruCache(int capacity)
        +Optional~V~ get(K key)
        +void put(K key, V value)
        -void removeNode(Node node)
        -void addAsMostRecentlyUsed(Node node)
        -Node removeLeastRecentlyUsed()
    }

    class Node~K,V~ {
        -K key
        -V value
        -Node~K,V~ prev
        -Node~K,V~ next
    }

    LruCache *-- Node : owns sentinels and entries
    LruCache --> Node : map points to real entries
    Node --> Node : prev / next
```

This is intentionally not split into a separate `DoublyLinkedList` class. That class would be valid, but
the list has no independent public use, so keeping the sentinels and private pointer helpers in `LruCache`
is the smaller design.

## Invariant

The list is ordered from least recently used immediately after `head` to most recently used immediately
before `tail`. `head` and `tail` are sentinel nodes: they are never cached, mapped, or evicted.

Every real node has exactly one key in the map, and every map entry points to exactly one list node. A
cache hit, insert, and update must leave both structures agreeing. The node stores its key because eviction
starts from a node; its key is needed to delete the corresponding map entry in O(1).

```mermaid
flowchart LR
    Map[Map: key to node] --> Node[Known list node]
    Head[head sentinel] --> LRU[Least recently used]
    LRU --> More[Other cached entries]
    More --> MRU[Most recently used]
    MRU --> Tail[tail sentinel]
```

The reader question: how can the cache find an entry and also change its recency without scanning the
list? The map locates the node; the node's `prev` and `next` links let the cache detach and reattach it.

## Core operations

### Cache hit

1. Find the node by key in the map.
2. Detach it from its current position.
3. Attach it immediately before `tail`.
4. Return its value.

The tail side is the most-recent side, so a read refreshes the key without changing cache size.

### Insert, update, and eviction

For an existing key, update the value and move the same node to the most-recent side. Do not evict: the
number of cached keys did not change.

For a new key, evict `head.next` first only when the map is already at capacity, remove that key from the
map, then attach the new node before `tail`. Removing from both structures is essential; otherwise a
future lookup can find a detached, stale node.

| Operation | Map work | List work | Result |
| --- | --- | --- | --- |
| `get` hit | Find node | Move it before `tail` | O(1) average |
| `put` update | Find existing node | Move it before `tail` | O(1) average |
| `put` at capacity | Remove LRU key and add new key | Detach `head.next`, attach new node | O(1) average |

## Edge cases worth saying aloud

- Updating an existing key when full is an update, not a new insert: move it to MRU, change its value, and
  do not evict.
- Capacity one must evict the only real node before adding a different key.
- A cache miss must not alter recency order.
- Rejecting null keys and values makes `Optional.empty()` an unambiguous miss result.
- After an eviction, remove the victim from both the list and map. Removing it from one structure only is
  the classic stale-entry bug.

## Follow-up answers

### Thread safety

`get` changes recency, so it is a write operation too. The simplest correct extension is to synchronize
both `get` and `put` on the same cache instance. A non-static `synchronized` method is equivalent to
locking `this`; it does not lock every `LruCache` instance or the `LruCache` class.

### TTL

Store an expiry time per node using a monotonic clock. On `get`, remove an expired node and return a miss;
this is lazy expiry and keeps a normal read O(1). A background cleanup worker needs an additional
expiry-ordered structure; it is an optimization for memory cleanup, not part of the base cache.

### Weighted capacity

Replace entry-count capacity with `maxWeight` and `currentWeight`. Reject an entry larger than the limit;
otherwise evict least-recent entries until the new total fits. The recency structure remains unchanged.

### Metrics

Keep hit, miss, and eviction counters with each cache instance. They are observations, not an eviction
policy and not a list of events. In a Spring service, export those counters through the application's
normal metrics integration rather than adding custom logging code to the cache.

### `LinkedHashMap` alternative

Use an access-ordered `LinkedHashMap` for a simple in-memory application cache when standard-library use
is allowed. It refreshes order on access and can evict the eldest entry. Implement the map-plus-list model
directly when an interview asks for the mechanism or when custom node-level behavior makes the shortcut
less clear. `LinkedHashMap` still needs external synchronization for concurrent access.

## Interview delivery

> “I need O(1) lookup by key and O(1) recency movement. I will map each key to its linked-list node and
> keep those same nodes ordered from LRU to MRU. On a hit or update, I move the node to MRU. On a full new
> insert, I remove the LRU node from both the list and map before adding the new node.”

For an independent coverage reference, see
[System Design Academy's LRU guide](https://www.systemdesign.academy/lld/lru-cache). This page uses
original wording and preserves the local implementation's LRU-to-MRU direction.

## Deferred extensions

- Thread safety and contention policy.
- Time-to-live expiry and cleanup.
- Size or weight-based capacity.
- Eviction callbacks, statistics, and persistence.

## Quick recall

**Q. Why use a doubly linked list?**
A. A map finds a node, and both links let the cache remove that known node in O(1).

**Q. Why must a node store its key?**
A. Eviction starts from the node, and the key identifies the map entry to remove in O(1).

**Q. Why are head and tail sentinels useful?**
A. They make the empty and one-entry list follow the same detach/attach logic as every other list state.

**Q. Why must `get` be synchronized in a concurrent version?**
A. A hit moves a node, so concurrent reads can otherwise corrupt recency links.

**Q. When is `LinkedHashMap` enough?**
A. For a simple local LRU when the standard library is allowed and custom eviction behavior is unnecessary.
