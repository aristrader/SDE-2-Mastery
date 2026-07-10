---
order: 20
search: false
---

# Solutions

## Solution: linkedhashmap-insertion-order - LinkedHashMap Insertion Order

```java
Map<Integer, String> employees = new LinkedHashMap<>();
employees.put(103, "Asha");
employees.put(101, "Ravi");
employees.put(102, "Mina");

for (Integer id : employees.keySet()) {
    System.out.println(id);
}
```

`LinkedHashMap` preserves insertion order. `HashMap` gives no ordering guarantee.

## Solution: lru-cache-eviction - Building an LRU Cache

```java
int capacity = 3;
Map<Integer, String> cache = new LinkedHashMap<>(capacity, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
        return size() > capacity; // Evict when size exceeds 3
    }
};

cache.put(1, "A");
cache.put(2, "B");
cache.put(3, "C");
cache.put(4, "D"); // Triggers eviction of the eldest entry (Key 1)

System.out.println(cache.keySet()); // Prints [2, 3, 4]
```
Key 1 was the "eldest" (inserted first and never accessed again), so `removeEldestEntry` caused it to be automatically evicted when the size reached 4.

## Solution: access-order-promotion - Access Order

```java
// Assuming the cache from above contains [2, 3, 4]
cache.get(2); // Accessing key 2 promotes it to the tail (most recently used)

System.out.println(cache.keySet()); // Prints [3, 4, 2]
```
Because `accessOrder` was set to `true`, simply reading the value via `get()` modifies the internal doubly-linked list, moving that entry to the tail. If we were to add a 5th item now, key `3` would be evicted, because it is now at the head (least recently used).
