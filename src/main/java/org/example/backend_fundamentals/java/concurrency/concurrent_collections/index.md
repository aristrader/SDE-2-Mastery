---
order: 70
---

# Concurrent Collections

Use concurrent collections when the collection object itself is shared across threads. They protect collection-level operations and internal structure. They do not automatically make your whole workflow, your stored values, or multi-step logic thread-safe.

Main rule:

```text
One concurrent collection method call can be thread-safe.
Several separate method calls are usually not one atomic operation.
```

## Why normal collections are unsafe

`HashMap`, `ArrayList`, and `HashSet` are not safe for concurrent mutation.

Typical failures:

- lost updates
- stale reads
- `ConcurrentModificationException`
- corrupted internal structure
- logic bugs from check-then-act races

This is unsafe:

```java
Map<String, Integer> counts = new HashMap<>();

counts.put(endpoint, counts.getOrDefault(endpoint, 0) + 1);
```

Two threads can read the same old count and both write the same new count.

## `ConcurrentHashMap`

`ConcurrentHashMap` supports safe concurrent access with better scalability than one big synchronized map.

```java
ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
```

It rejects `null` keys and values. In concurrent code, `map.get(key) == null` must clearly mean "no mapping exists". If null values were allowed, a thread could not distinguish "key missing" from "key present with null value".

`ConcurrentHashMap` operations are safe individually. The map will not corrupt itself under concurrent reads and writes.

## Atomic map operations

This is still a race, even with `ConcurrentHashMap`:

```java
if (!map.containsKey(key)) {
    map.put(key, value);
}
```

Another thread can insert the key between `containsKey()` and `put()`.

Use one atomic map operation:

```java
map.putIfAbsent(key, value);
map.computeIfAbsent(key, this::loadUser);
map.merge(endpoint, 1, Integer::sum);
map.replace(key, oldValue, newValue);
map.remove(key, expectedValue);
```

Pick by intent:

| Need | Method |
|---|---|
| Insert already-built value only if missing | `putIfAbsent` |
| Build value lazily only if missing | `computeIfAbsent` |
| Add/update a counter or aggregate | `merge` |
| Replace only if current value still matches | `replace(key, old, next)` |
| Remove only if current value still matches | `remove(key, expected)` |

Counter example:

```java
ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();

counts.merge(endpoint, 1, Integer::sum);
```

That is one atomic map update. Do not split it into `get()` and `put()`.

## `computeIfAbsent` caveats

`computeIfAbsent()` is useful for cache-style code:

```java
User user = cache.computeIfAbsent(userId, this::loadUser);
```

Keep the mapping function short and side-effect-light.

Reasons:

- Other updates for the same key may block while the computation runs.
- If the function throws, no mapping is recorded.
- In complex races involving removal or retries, loader-style functions should be safe to run again.
- Calling back into the same map from the mapping function can create hard-to-reason-about behavior.

For expensive IO loading, consider whether a `ConcurrentHashMap<Key, CompletableFuture<Value>>` or a real cache library is the better model.

## Mutable values trap

This is not fixed by the map being concurrent:

```java
ConcurrentHashMap<String, List<Item>> map = new ConcurrentHashMap<>();

map.computeIfAbsent(userId, id -> new ArrayList<>()).add(item);
```

The map operation is safe. The `ArrayList` stored inside the map is still mutable and not thread-safe. Two threads can call `add()` on the same list at the same time and corrupt or lose data.

Fix options:

```java
// Immutable replacement: each update creates a new list value.
map.compute(userId, (id, oldList) -> {
    List<Item> next = oldList == null ? new ArrayList<>() : new ArrayList<>(oldList);
    next.add(item);
    return List.copyOf(next);
});
```

Or store a thread-safe value when it fits:

```java
ConcurrentHashMap<String, Queue<Item>> map = new ConcurrentHashMap<>();
map.computeIfAbsent(userId, id -> new ConcurrentLinkedQueue<>()).add(item);
```

Choose based on read/write pattern and invariants.

## `CopyOnWriteArrayList`

`CopyOnWriteArrayList` is for read-heavy, write-rare lists.

Good fit:

- listener registries
- callback lists
- feature observers
- small config lists read often and changed rarely

Every write copies the entire backing array. That makes iteration simple and stable for readers, but writes are expensive.

```java
CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

for (Listener listener : listeners) {
    listener.onEvent(event);
}
```

Do not use it for large lists with frequent writes.

## Iteration and `size()`

Concurrent collection iterators are usually weakly consistent. They do not throw `ConcurrentModificationException`, but they may reflect some updates and miss others while iteration is in progress.

That is usually fine for monitoring, cleanup scans, and best-effort reporting. It is not a consistent snapshot.

`size()` is also not an admission-control primitive:

```java
if (map.size() < limit) {
    map.put(key, value);
}
```

Another thread can change the map between the size check and the put. Use a `Semaphore`, bounded queue, or explicit lock if you need a hard limit.

## Synchronized wrappers

`Collections.synchronizedMap(new HashMap<>())` is correct for basic synchronized access, but it serializes operations through one map-level lock.

```java
Map<String, Integer> map = Collections.synchronizedMap(new HashMap<>());
```

It can be acceptable for small, low-contention maps. Under heavy concurrency, `ConcurrentHashMap` usually scales better.

Important: synchronized wrappers still require external synchronization during iteration:

```java
synchronized (map) {
    for (String key : map.keySet()) {
        use(key);
    }
}
```

## Quick recall

**Q. Why is a shared mutable `HashMap` unsafe?**
A. Concurrent writers can lose updates or corrupt internal structure.

**Q. What does `ConcurrentHashMap` make safe?**
A. Individual map operations and the map's internal structure. It does not make multi-call workflows or mutable stored values safe.

**Q. Why does `ConcurrentHashMap` reject null values?**
A. So `get(key) == null` unambiguously means no mapping exists.

**Q. Is `containsKey()` then `put()` atomic on `ConcurrentHashMap`?**
A. No. Use `putIfAbsent()` or `computeIfAbsent()`.

**Q. How should you update a per-key counter?**
A. Use `merge(key, 1, Integer::sum)` or store an appropriate atomic counter.

**Q. Does `ConcurrentHashMap<String, ArrayList<T>>` make list mutation safe?**
A. No. The `ArrayList` remains unsafe; use immutable replacement or a thread-safe value.

**Q. When does `CopyOnWriteArrayList` fit?**
A. Read-heavy, write-rare listener or callback lists.

