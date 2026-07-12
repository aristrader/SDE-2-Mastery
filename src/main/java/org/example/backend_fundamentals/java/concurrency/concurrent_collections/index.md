---
order: 70
---

# Concurrent Collections

Use concurrent collections when the collection itself is shared across threads. They protect collection-level operations; they do not automatically make the objects stored inside them thread-safe.

## ConcurrentHashMap

`HashMap` is not safe for concurrent mutation. `Collections.synchronizedMap(new HashMap<>())` is correct but uses one map-level lock, so readers and writers contend heavily.

`ConcurrentHashMap` supports safe concurrent access with better scalability and atomic helper methods.

```java
ConcurrentHashMap<String, User> cache = new ConcurrentHashMap<>();
```

It rejects `null` keys and values. In concurrent code, `map.get(key) == null` must unambiguously mean "no mapping exists."

## Atomic Map Operations

This is still a race:

```java
if (!map.containsKey(key)) {
    map.put(key, value);
}
```

Use one atomic map operation:

```java
map.putIfAbsent(key, value);
map.computeIfAbsent(key, this::loadUser);
map.merge(endpoint, 1, Integer::sum);
map.replace(key, oldValue, newValue);
map.remove(key, expectedValue);
```

Use `putIfAbsent()` when the value is already built. Use `computeIfAbsent()` when the value should be created lazily.

Keep mapping functions short and side-effect-light. A slow `computeIfAbsent()` loader can create contention and may be retried after removal or failure.

## Mutable Values Trap

This is not fixed by the map being concurrent:

```java
ConcurrentHashMap<String, List<Item>> map = new ConcurrentHashMap<>();
map.get(key).add(item); // ArrayList mutation can still race
```

The map protects map structure and map methods. The `ArrayList` remains a mutable, non-thread-safe value.

Fix by storing immutable values, thread-safe values, or using one atomic map update that replaces the value.

## CopyOnWriteArrayList

`CopyOnWriteArrayList` fits listener-style workloads:

- many reads and iterations
- rare writes
- readers need stable iteration without external locking

Every write copies the underlying array, so it is poor for write-heavy lists.

## Iteration And Size

Concurrent collection iterators are often weakly consistent, not frozen snapshots. They avoid corruption, but they may not reflect every latest update.

`size()` is not an atomic admission gate:

```java
if (map.size() < limit) {
    map.put(key, value);
}
```

Another thread can change the map between the check and the put.

## Quick recall

**Q. Why is a shared mutable `HashMap` unsafe?**
A. Concurrent writers can lose updates or corrupt internal structure.

**Q. Why does `synchronizedMap` scale worse than `ConcurrentHashMap`?**
A. It serializes operations through one map-level lock.

**Q. Why does `ConcurrentHashMap` reject null values?**
A. So `get(key) == null` means no mapping exists, with no ambiguity.

**Q. `containsKey()` then `put()` on `ConcurrentHashMap` — atomic?**
A. No. Use `putIfAbsent()` or `computeIfAbsent()`.

**Q. Does `ConcurrentHashMap<String, ArrayList<T>>` make list mutation safe?**
A. No. The values need their own thread-safety or immutability.

**Q. When does `CopyOnWriteArrayList` fit?**
A. Read-heavy, write-rare listener or callback registries.
