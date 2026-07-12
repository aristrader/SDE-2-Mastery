---
order: 10
search: false
---

# Concurrent Collections Practice

## Exercise: concurrent-cache - Concurrent Cache

### Goal
Use `ConcurrentHashMap` for a shared cache.

### Task
Implement `get`, `put`, `remove`, and `getOrLoad(id)` using `computeIfAbsent()`.

### Checks
- No `containsKey()` plus `put()`.
- Explain what happens if the loader throws.
- Explain whether failed loads should be cached.

## Exercise: endpoint-counter - Endpoint Counter

### Goal
Count requests per endpoint safely.

### Task
Implement the counter twice:

1. `ConcurrentHashMap<String, AtomicInteger>`
2. `ConcurrentHashMap<String, Integer>` with `merge()`

### Checks
- Explain why `getOrDefault() + put()` is unsafe.
- Explain which version is simpler for interview code.

## Exercise: inflight-async-cache - In-Flight Async Cache

### Goal
Prevent duplicate async loads for the same key.

### Task
Use `ConcurrentHashMap<String, CompletableFuture<User>>` so concurrent requests for the same missing user share one in-flight future.

### Checks
- Use `computeIfAbsent()`.
- Remove failed futures.
- Do not block inside the cache method.

## Exercise: listener-registry - Listener Registry

### Goal
Pick `CopyOnWriteArrayList` for the right workload.

### Task
Implement register, unregister, and publish for a listener list that is read frequently and modified rarely.

### Checks
- Iteration is safe while another thread registers.
- Explain why writes are expensive.

## Exercise: conditional-replace-remove - Conditional Replace And Remove

### Goal
Use compare-and-set style map operations.

### Task
Use `replace(key, oldValue, newValue)` and `remove(key, expectedValue)` to update/remove only if the mapping has not changed.

### Checks
- Explain why separate `get()` then `replace()` is racy.
- Explain when conditional remove prevents deleting another thread's update.

## Exercise: size-admission-gate - size() Admission Gate

### Goal
Catch a common ConcurrentHashMap interview trap.

### Task
Spot the bug:

```java
if (sessions.size() < 100) {
    sessions.put(sessionId, session);
}
```

Assume many threads can register sessions concurrently.

### Checks
- Explain why `size()` and `put()` are separate operations.
- Explain why the limit can be exceeded.
- Propose a safer design at a high level.
