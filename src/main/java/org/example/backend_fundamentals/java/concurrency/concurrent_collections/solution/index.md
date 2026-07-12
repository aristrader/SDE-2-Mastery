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

If the loader throws, no mapping is installed. Usually do not cache failures forever; make that an explicit policy.

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
counts.merge(endpoint, 1, Integer::sum);
```

`merge()` is the shorter answer for simple integer counts. `getOrDefault() + put()` splits the read and write.

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

Failed futures are removed so a later request can retry instead of reusing a permanently failed value.

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

Readers iterate over a stable snapshot. Writes copy the backing array, so this is for read-heavy lists.

## Solution: conditional-replace-remove - Conditional Replace And Remove

```java
boolean updated = map.replace(key, oldValue, newValue);
boolean removed = map.remove(key, expectedValue);
```

Both methods check the current mapping and perform the action atomically. They avoid overwriting or deleting a value another thread changed after your earlier read.

## Solution: size-admission-gate - size() Admission Gate

`size()` is a moment-in-time observation. Another thread can insert after the check and before this thread's `put()`, so many threads can all pass the gate and exceed the limit.

Safer options depend on the invariant:

- protect the admission check and insertion with one lock
- use a `Semaphore` to represent available capacity
- move the limit to a stronger external system if it is distributed or business-critical
