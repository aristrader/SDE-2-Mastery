---
order: 30
---

# Spring Caching Abstraction

---

## What the abstraction is

Spring's caching layer sits between your code and any cache provider via a `CacheManager` interface. Your annotations (`@Cacheable`, `@CacheEvict`, `@CachePut`) are provider-agnostic — swap Caffeine for Redis by changing one bean declaration and zero application code.

```
@Cacheable("users")          ← your code, unchanged
    │
    ▼
CacheInterceptor (AOP)
    │
    ▼
CacheManager  (interface)
    │
    ├─ ConcurrentMapCacheManager    (in-memory, no TTL — dev/test only)
    ├─ CaffeineCacheManager         (in-process, TTL, eviction — single-node prod)
    └─ RedisCacheManager            (distributed, TTL per cache — multi-node prod)
```

Enable caching:

```java
@Configuration
@EnableCaching
public class CacheConfig { }
```

Without `@EnableCaching`, all caching annotations are silently ignored — same failure mode as `@EnableMethodSecurity`.

---

## @Cacheable

Check cache first; on hit return cached value; on miss call the method and store the result.

```java
@Cacheable(cacheNames = "users", key = "#id")
public User findById(Long id) { ... }
```

Execution flow:

```
call findById(42)
    │
    ├─ cache "users" has key 42? → HIT  → return cached User, method NOT called
    └─ MISS → call method → store result under key 42 → return User
```

**Key defaults:** if `key` is omitted, Spring uses all method parameters as a compound key via `SimpleKeyGenerator`. A method with one `Long id` parameter defaults to the `Long` value itself. A method with zero parameters uses `SimpleKey.EMPTY`.

**Sync mode:**

```java
@Cacheable(cacheNames = "users", key = "#id", sync = true)
```

`sync=true` prevents cache stampede — only one thread calls the method for a given key; others wait. Supported by Caffeine but not all providers (check before enabling with Redis).

---

## @CacheEvict

Remove one or more entries from the cache.

```java
// remove single entry
@CacheEvict(cacheNames = "users", key = "#id")
public void deleteUser(Long id) { ... }

// flush the entire cache
@CacheEvict(cacheNames = "users", allEntries = true)
public void importUsers(List<User> users) { ... }

// evict BEFORE method runs — use when method may throw
@CacheEvict(cacheNames = "users", key = "#id", beforeInvocation = true)
public void dangerousUpdate(Long id) { ... }
```

`beforeInvocation=false` (default): eviction happens **after** the method completes successfully. If the method throws, the entry is **not** removed — the stale entry survives. Use `beforeInvocation=true` to guarantee the entry is gone regardless of method outcome.

---

## @CachePut

**Always** calls the method and stores the result. Never checks for an existing entry — the cache is updated unconditionally.

```java
@CachePut(cacheNames = "users", key = "#result.id")
public User updateUser(UserUpdateRequest req) { ... }
```

Use on write paths to keep the cache warm. The caller gets fresh data from the method; the cache gets the new value for subsequent reads.

**Never put `@Cacheable` and `@CachePut` on the same method.** `@Cacheable` may skip the method entirely, preventing `@CachePut` from ever updating the cache.

---

## @CacheConfig

Set defaults for all caching annotations in the class. Avoids repeating `cacheNames` on every method.

```java
@Service
@CacheConfig(cacheNames = "users")
public class UserService {

    @Cacheable(key = "#id")          // cacheNames inherited
    public User findById(Long id) { ... }

    @CacheEvict(key = "#id")
    public void deleteUser(Long id) { ... }

    @CachePut(key = "#result.id")
    public User updateUser(UserUpdateRequest req) { ... }
}
```

Method-level `cacheNames` override the class-level default.

---

## Key generation with SpEL

| Expression | Resolves to |
|---|---|
| `#id` | Value of the `id` parameter |
| `#user.id` | `.id` field/getter on the `user` parameter |
| `#root.methodName` | Name of the annotated method |
| `#root.method.name + '-' + #id` | Compound key string |
| `T(String).valueOf(#id)` | Explicit type conversion |
| `#result.id` | Return value's `id` (only in `@CachePut` — evaluated after method runs) |

For keys that cannot be expressed as SpEL, implement `KeyGenerator`:

```java
@Bean
public KeyGenerator userKeyGenerator() {
    return (target, method, params) -> {
        // build a custom key object
        return method.getName() + "_" + Arrays.toString(params);
    };
}

@Cacheable(cacheNames = "users", keyGenerator = "userKeyGenerator")
public User findByComplexCriteria(SearchCriteria criteria) { ... }
```

---

## condition and unless

Both accept SpEL but evaluate at different times.

```java
// condition — evaluated BEFORE method call; if false, cache is bypassed entirely (no read, no write)
@Cacheable(cacheNames = "users", key = "#id", condition = "#id > 0")
public User findById(Long id) { ... }

// unless — evaluated AFTER method call; if true, result is NOT stored (method always runs)
@Cacheable(cacheNames = "users", key = "#id", unless = "#result == null")
public User findById(Long id) { ... }
```

| | `condition` | `unless` |
|---|---|---|
| Evaluated | Before method | After method |
| Cache read | Skipped if false | Normal |
| Cache write | Skipped if false | Skipped if true |
| Access to `#result` | No | Yes |

Common pattern: `unless="#result == null"` to avoid caching `null` results (e.g., user not found). Without it, subsequent calls for the same missing ID return a cached `null` and bypass any eventual-consistency recovery.

---

## Provider adapters

### ConcurrentMapCacheManager (default)

```java
// auto-configured if no other CacheManager bean present
```

- In-process `ConcurrentHashMap`.
- No TTL, no size limit, no eviction policy.
- Cache grows unbounded — memory leak risk in production.
- Use only for dev/testing.

### CaffeineCacheManager (single-node production)

```java
@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager manager = new CaffeineCacheManager("users", "products");
    manager.setCaffeine(Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .maximumSize(10_000)
        .recordStats());
    return manager;
}
```

- In-process — data lost on restart, not shared across nodes.
- Window TinyLFU eviction policy (near-optimal hit rate).
- `expireAfterWrite` vs `expireAfterAccess`: write TTL is usually safer — stale data is bounded by TTL regardless of access patterns.
- Per-cache TTL: use `CaffeineSpec` per cache name or build separate caches with different specs.

### RedisCacheManager (distributed / multi-node)

```java
@Bean
public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer()));  // JSON — human-readable, schema-tolerant

    Map<String, RedisCacheConfiguration> perCacheConfig = Map.of(
        "sessions", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)),
        "products", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5))
    );

    return RedisCacheManager.builder(factory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(perCacheConfig)
        .build();
}
```

- Distributed — all nodes share the same cache; survives individual node restarts.
- Serialization: use JSON (`GenericJackson2JsonRedisSerializer` or `Jackson2JsonRedisSerializer`) rather than Java serialization. Java serialization breaks on class changes; JSON is resilient to additive changes.
- TTL is configured per cache in `RedisCacheConfiguration`, not per entry. For entry-level TTL control you need a custom `CacheWriter`.
- Network cost: each cache operation is a network round-trip (~0.5–2 ms). For frequently-accessed, low-change data consider a near-cache (Caffeine in front of Redis) pattern.

---

## Common pitfalls

| Pitfall | Root cause | Fix |
|---|---|---|
| `@Cacheable` ignored on self-invocation | Spring AOP proxy bypassed | Inject bean into itself or move cached method to separate bean |
| Method must be `public` | CGLIB proxy cannot intercept non-public methods | Make annotated methods `public` |
| `@Cacheable` + `@CachePut` on same method | `@Cacheable` may skip the method, preventing cache update | Use only `@CachePut` on write paths |
| Cache not populated on update | Using `@Cacheable` on update path | Use `@CachePut` on write, `@Cacheable` on read |
| Null results cached | No `unless` guard | Add `unless="#result == null"` |
| Java serialization in Redis | Default serializer — breaks on class change | Switch to JSON serializer |
| `ConcurrentMapCacheManager` in prod | No TTL, unbounded growth | Use Caffeine or Redis |
| Missing `@EnableCaching` | Annotations silently ignored | Always add to a `@Configuration` |

---

## Quick recall

**Q. What does `@EnableCaching` actually enable?**
A. Registers a `CacheInterceptor` AOP interceptor. Without it, all caching annotations are no-ops.

**Q. `@CacheEvict` with `beforeInvocation=false` (default) — what happens if the method throws?**
A. The cache entry is NOT evicted — stale data survives. Use `beforeInvocation=true` if the entry must be gone regardless of method outcome.

**Q. Difference between `condition` and `unless` on `@Cacheable`?**
A. `condition` is evaluated before the call (can skip cache read + write); `unless` is evaluated after (can skip write but cache is still checked for a hit). Only `unless` can access `#result`.

**Q. Why avoid Java serialization in Redis?**
A. Any class change (rename field, add field) breaks deserialization of existing cache entries. JSON serialization is resilient to additive changes.

**Q. Self-invocation and `@Cacheable` — same problem as `@Transactional`?**
A. Exactly — the AOP proxy is bypassed, so the cache check is skipped and the method always runs.

**Q. Caffeine vs Redis — key deciding factor?**
A. Single node or multiple nodes. Caffeine is in-process (fast, no network, not shared). Redis is distributed (shared across all nodes, survives restarts, ~1ms per operation overhead).
