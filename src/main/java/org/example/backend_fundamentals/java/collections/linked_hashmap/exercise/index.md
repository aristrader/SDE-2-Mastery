---
order: 10
search: false
---

# Practice

## Exercise: lru-cache-eviction - Building an LRU Cache

### Goal
Use `LinkedHashMap` to build a simple Least Recently Used (LRU) cache.

### Task
Create a `LinkedHashMap<Integer, String>` with a capacity of 3. 
Ensure you pass `true` to the constructor's `accessOrder` parameter.
Override the `removeEldestEntry` method to return `size() > 3`.
Add 4 items to the map (e.g., keys 1, 2, 3, 4). Print the map.

### Checks
- Which key was automatically evicted when the 4th item was added?

## Exercise: access-order-promotion - Access Order

### Goal
Observe how `get()` alters the iteration order in access-order mode.

### Task
Using the LRU cache from the previous exercise (which currently holds keys 2, 3, 4), call `cache.get(2)`.
Then, print the map again.

### Checks
- Where is key `2` located in the printed output now? (It should have moved from the head/front to the tail/end).
