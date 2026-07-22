---
order: 20
search: false
---

# Concurrent Collections Solutions

## Solution: concurrent-cache - Concurrent Cache

```java
final class UserCache {
    private final ConcurrentHashMap<String, User> cache = new ConcurrentHashMap<>();

    User get(String id) {
        return cache.get(id);
    }

    void put(String id, User user) {
        cache.put(id, user);
    }

    void remove(String id) {
        cache.remove(id);
    }

    User getOrLoad(String id) {
        return cache.computeIfAbsent(id, this::loadUser);
    }
}
```

`computeIfAbsent()` makes the "check if missing, then load, then install" flow atomic for that key. That is why it is better than:

```java
if (!cache.containsKey(id)) {
    cache.put(id, loadUser(id));
}
```

The `containsKey()` version is racy. Two threads can both observe the key as missing, both call the loader, and then race to store a value. `computeIfAbsent()` ensures the map coordinates the update for the key.

If the loader throws, the exception is propagated to the caller and no mapping is installed. A later call can retry the load. That is usually the right default because caching failures forever can turn one temporary outage into a permanent bad cache entry.

Common trap: keep the mapping function small. It may run while the map is coordinating updates for that key, so it should not perform unrelated map mutations or block longer than the real load requires.

## Solution: endpoint-counter - Endpoint Counter

```java
final class EndpointMetrics {
    private final ConcurrentHashMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();

    void record(String endpoint) {
        counts.computeIfAbsent(endpoint, ignored -> new AtomicInteger())
                .incrementAndGet();
    }

    int count(String endpoint) {
        AtomicInteger value = counts.get(endpoint);
        return value == null ? 0 : value.get();
    }
}
```

```java
final class EndpointMetricsWithMerge {
    private final ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();

    void record(String endpoint) {
        counts.merge(endpoint, 1, Integer::sum);
    }

    int count(String endpoint) {
        return counts.getOrDefault(endpoint, 0);
    }
}
```

The `AtomicInteger` version stores a mutable counter per endpoint. `computeIfAbsent()` safely creates the counter once per key, then `incrementAndGet()` performs an atomic increment on that counter.

The `merge()` version is often the cleaner interview answer for simple counts. It says: if the key is absent, store `1`; if it is present, combine the old value and `1` using `Integer::sum`.

This version is unsafe:

```java
counts.put(endpoint, counts.getOrDefault(endpoint, 0) + 1);
```

The read and write are separate operations. Two threads can both read `10`, both calculate `11`, and both store `11`, losing one increment.

Use `AtomicInteger` when you need a counter object that can be updated or read frequently after lookup. Use `merge()` when you want the shortest correct map-level update for simple counting.

## Solution: inflight-async-cache - In-Flight Async Cache

```java
final class AsyncUserCache {
    private final ConcurrentHashMap<String, CompletableFuture<User>> cache =
            new ConcurrentHashMap<>();

    CompletableFuture<User> getUser(String id) {
        return cache.computeIfAbsent(id, key ->
                loadUserAsync(key).whenComplete((user, ex) -> {
                    if (ex != null) {
                        cache.remove(key);
                    }
                }));
    }
}
```

The cache stores the in-flight `CompletableFuture`, not only the finished `User`. That means if five threads request the same missing user at the same time, they all receive the same future and only one async load starts.

`computeIfAbsent()` is the key operation. It makes future creation atomic for the user id. Without it, multiple callers could all see "missing" and each start a duplicate remote call.

The method should not call `join()` or `get()` internally. Returning the future keeps the cache method non-blocking and lets the caller decide how to compose, timeout, or wait.

Failed futures are removed:

```java
cache.remove(key);
```

That allows a later request to retry. If the failed future stayed in the map, every future caller for that key would immediately receive the same old failure.

Common trap: `cache.remove(key)` can remove a newer future if another thread replaced the value. A stricter version removes conditionally:

```java
CompletableFuture<User> future = loadUserAsync(key);
future.whenComplete((user, ex) -> {
    if (ex != null) {
        cache.remove(key, future);
    }
});
return future;
```

## Solution: listener-registry - Listener Registry

```java
final class ListenerRegistry {
    private final CopyOnWriteArrayList<EventListener> listeners =
            new CopyOnWriteArrayList<>();

    void register(EventListener listener) {
        listeners.add(listener);
    }

    void unregister(EventListener listener) {
        listeners.remove(listener);
    }

    void publish(String event) {
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
```

`CopyOnWriteArrayList` is a good fit when reads are frequent and writes are rare. Publishing an event iterates over a stable snapshot of the array. If another thread registers or unregisters a listener during publication, the current iteration is not corrupted and does not throw `ConcurrentModificationException`.

The tradeoff is write cost. Every `add()` or `remove()` copies the underlying array. That is acceptable for listener registries where registration is occasional and event delivery is frequent. It is a bad fit for a high-write list.

Common trap: snapshot iteration means a newly registered listener may not receive the event currently being published. It will receive later events. That behavior is usually acceptable for listener lists, but it should be understood.

## Solution: conditional-replace-remove - Conditional Replace And Remove

```java
boolean updated = map.replace(key, oldValue, newValue);
boolean removed = map.remove(key, expectedValue);
```

These methods are compare-and-set style operations for maps.

`replace(key, oldValue, newValue)` means: update the value only if the key is still mapped to `oldValue`. If another thread changed the mapping after you read it, the replace fails and returns `false`.

`remove(key, expectedValue)` means: remove the entry only if the key is still mapped to `expectedValue`. This prevents deleting another thread's newer update.

This is racy:

```java
Value current = map.get(key);
if (current.equals(oldValue)) {
    map.replace(key, newValue);
}
```

Another thread can change the value between `get()` and `replace()`. The conditional map methods combine the check and the update into one atomic operation for that key.

Common trap: the comparison uses value equality. Make sure the value type has meaningful `equals()` semantics, or use stable immutable values.

## Solution: size-admission-gate - size() Admission Gate

`size()` is a moment-in-time observation. Another thread can insert after the check and before this thread's `put()`, so many threads can all pass the gate and exceed the limit.

Safer options depend on the invariant:

- protect the admission check and insertion with one lock
- use a `Semaphore` to represent available capacity
- move the limit to a stronger external system if it is distributed or business-critical

For a single JVM, a simple lock keeps the invariant clear:

```java
final class SessionRegistry {
    private final Map<String, Session> sessions = new HashMap<>();

    synchronized boolean register(String sessionId, Session session) {
        if (sessions.size() >= 100) {
            return false;
        }
        sessions.put(sessionId, session);
        return true;
    }
}
```

A `Semaphore` is useful when you want capacity permits:

```java
final class SessionRegistry {
    private final Semaphore capacity = new Semaphore(100);
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    boolean register(String sessionId, Session session) {
        if (!capacity.tryAcquire()) {
            return false;
        }
        Session previous = sessions.putIfAbsent(sessionId, session);
        if (previous != null) {
            capacity.release();
            return false;
        }
        return true;
    }

    void unregister(String sessionId) {
        if (sessions.remove(sessionId) != null) {
            capacity.release();
        }
    }
}
```

Common trap: `ConcurrentHashMap` makes individual map operations thread-safe. It does not automatically make multi-step business rules atomic.
