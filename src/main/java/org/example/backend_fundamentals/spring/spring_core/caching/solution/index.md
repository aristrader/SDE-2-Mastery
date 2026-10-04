---
order: 20
search: false
---

# Solutions: Spring Caching Abstraction

Detailed architectural solutions and code implementations for Spring Cache proxy mechanics, stampede prevention, transactional synchronization, and resilient error handling.

---

## Solution: cache-proxy-bypass-and-sync - Diagnose Proxy Self-Invocation and Stampede Deadlock

### 1. Root Cause Analysis: Self-Invocation Bypass
Spring Caching relies on Spring AOP proxies (CGLIB subclasses or JDK dynamic proxies). When an external client calls a bean method, the invocation enters through the generated proxy, triggering `CacheInterceptor` before reaching the target instance.

When `getAvailableStock` calls `this.fetchStockFromWarehouse(productId)`, `this` points directly to the unproxied target instance inside the JVM. The proxy's interceptor chain is completely bypassed, meaning annotations like `@Cacheable`, `@CachePut`, and `@CacheEvict` on `fetchStockFromWarehouse` are never evaluated.

### 2. Root Cause Analysis: `sync = true` and `unless` Conflict
When `sync = true` is configured, Spring delegates key retrieval and method execution directly to the underlying cache provider's atomic method:
```java
Cache.ValueWrapper get(Object key, Callable<?> valueLoader);
```
Under this atomic contract:
1. The cache provider locks the specific key.
2. If the key exists, it returns the cached value immediately.
3. If the key is missing, the cache provider executes `valueLoader` (the target method) and **immediately writes the returned value into cache storage inside the provider lock**.

Because value loading and storage occur atomically within the provider, control does not return to Spring's `CacheAspectSupport` between method execution and cache storage. Therefore, Spring cannot evaluate the SpEL `unless` condition against `#result` before the value is committed. To prevent undefined state, Spring throws `IllegalArgumentException` at validation time.

### 3. Idiomatic Architecture and Refactored Implementation

Extract cached data-access operations into a dedicated repository or query component. This establishes a clean bean boundary, ensuring all calls transit through the Spring AOP proxy. To avoid caching null values safely with `sync = true`, enforce a domain-level "empty" object pattern or throw a custom `EntityNotFoundException` on missing entities (Spring caching does not cache thrown exceptions):

```java
// 1. Separate component to enforce Spring proxy interception boundary
@Component
public class InventoryStockReader {

    private final InventoryRepository inventoryRepo;

    public InventoryStockReader(InventoryRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    /**
     * sync = true forces concurrent lookups for the same productId to coalesce,
     * executing the database query only once across concurrent threads.
     * If the product is not found, an exception is thrown, preventing null caching.
     */
    @Cacheable(cacheNames = "inventory", key = "#productId", sync = true)
    public AvailableStock fetchStockFromWarehouse(Long productId) {
        AvailableStock stock = inventoryRepo.queryWarehouseStock(productId);
        if (stock == null) {
            throw new ProductNotFoundException("Stock not found for product: " + productId);
        }
        return stock;
    }
}

// 2. High-level service delegating across bean boundary
@Service
public class WarehouseService {

    private final InventoryStockReader stockReader;

    public WarehouseService(InventoryStockReader stockReader) {
        this.stockReader = stockReader;
    }

    public AvailableStock getAvailableStock(Long productId) {
        try {
            return stockReader.fetchStockFromWarehouse(productId);
        } catch (ProductNotFoundException e) {
            return AvailableStock.empty(productId);
        }
    }
}
```

---

## Solution: cache-put-evict-transactional-boundary - Write Consistency with @CachePut and Transaction Rollback

### 1. Interceptor Lifecycle & Transactional Ordering
By default, Spring applies AOP advisors based on their configured order:
- `CacheInterceptor` and `TransactionInterceptor` are both registered as method interceptors.
- When `@CachePut` is annotated on a method alongside `@Transactional`, `@CachePut` runs immediately when the target Java method completes execution.
- If downstream work (such as sending an audit event) throws an exception, `TransactionInterceptor` intercepts the exception and issues a database `ROLLBACK`.
- However, `@CachePut` has already committed the new object into Redis! Redis now holds dirty data that does not exist in PostgreSQL.

### 2. Trade-Off Analysis of `@CacheEvict` Modes
- **`beforeInvocation = false` (default):** Eviction runs only if the method exits without throwing an exception. If the method throws, the eviction is skipped. If a rollback occurs due to an exception, the cache remains un-evicted (which is actually consistent if the DB was also not modified).
- **`beforeInvocation = true`:** Eviction runs before the method or transaction starts. If the transaction rolls back, the database row remains in its previous state, but the cache has already lost the entry. While safer against stale data (the next read simply fetches fresh data from the DB), it causes unnecessary cache misses.
- **The Core Problem:** Neither mode handles deferred cache invalidation upon successful database commit.

### 3. Solution: Deferred Cache Synchronization via `TransactionSynchronizationManager`

To guarantee strong cache-database consistency, evict or update the cache only inside the `afterCommit` hook of Spring's transaction synchronization lifecycle:

```java
@Service
public class CustomerAccountService {

    private final AccountRepository accountRepo;
    private final AuditClient auditClient;
    private final CacheManager cacheManager;

    public CustomerAccountService(AccountRepository accountRepo,
                                  AuditClient auditClient,
                                  CacheManager cacheManager) {
        this.accountRepo = accountRepo;
        this.auditClient = auditClient;
        this.cacheManager = cacheManager;
    }

    @Transactional
    public Account updateAccount(AccountUpdateRequest req) {
        Account acc = accountRepo.findById(req.accountId()).orElseThrow();
        acc.updateEmail(req.email());
        Account saved = accountRepo.save(acc);

        // Risky external audit call that may trigger transaction rollback
        auditClient.publishAuditEvent(saved);

        // Register post-commit cache eviction: executes strictly AFTER DB commits
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                Cache cache = cacheManager.getCache("accounts");
                if (cache != null) {
                    // Evict on commit so next read fetches the committed DB state
                    cache.evict(saved.getId());
                }
            }
        });

        return saved;
    }

    @Transactional
    public void closeAccount(Long accountId) {
        accountRepo.markClosed(accountId);
        auditClient.publishAuditEvent(new AccountClosedEvent(accountId));

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                Cache cache = cacheManager.getCache("accounts");
                if (cache != null) {
                    cache.evict(accountId);
                }
            }
        });
    }
}
```

> [!TIP]
> Prefer **cache eviction on commit** over `@CachePut` in transactional methods. Evicting on commit is idempotent and ensures that concurrent read transactions always observe committed database state.

---

## Solution: redis-resilience-cache-error-handler - High Availability Failover with Custom CacheErrorHandler

### 1. Default Error Handling Behavior
Spring's default `SimpleCacheErrorHandler` rethrows any `RuntimeException` originating from the underlying `CacheManager` / `Cache` implementation:
```java
public class SimpleCacheErrorHandler implements CacheErrorHandler {
    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        throw exception;
    }
    // ... rethrows for put, evict, clear
}
```
When Redis goes offline or suffers connection timeouts, any `@Cacheable` method throws a `RedisConnectionFailureException`. This abruptly terminates the HTTP request with a 500 status code, defeating the purpose of caching as an optional acceleration tier.

### 2. Resilient Cache Configuration
Implement `CachingConfigurer` and provide a custom `CacheErrorHandler`:

```java
@Configuration
@EnableCaching
public class ResilientCachingConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(ResilientCachingConfig.class);

    @Override
    public CacheErrorHandler errorHandler() {
        return new ResilientCacheErrorHandler();
    }

    public static class ResilientCacheErrorHandler implements CacheErrorHandler {

        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            // Fail-open: swallow exception and log warning.
            // Spring treats this as a cache miss, proceeding to query the primary DB.
            log.warn("Redis GET failed for cache='{}', key='{}'. Degraded read to DB. Error: {}",
                cache.getName(), key, exception.getMessage());
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            // Log error with high visibility. The primary DB has the data, but cache wasn't updated.
            log.error("Redis PUT failed for cache='{}', key='{}'. Cache is cold. Error: {}",
                cache.getName(), key, exception.getMessage());
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            // Log error with critical severity.
            // WARNING: If eviction fails, stale data remains in Redis!
            log.error("CRITICAL: Redis EVICT failed for cache='{}', key='{}'. Stale cache risk! Error: {}",
                cache.getName(), key, exception.getMessage());
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            log.error("CRITICAL: Redis CLEAR failed for cache='{}'. Error: {}",
                cache.getName(), exception.getMessage());
        }
    }
}
```

### 3. Consistency Implications on Write Paths
- **Swallowing `GET` errors:** Safe and recommended. Reading directly from the database is slower but 100% correct.
- **Swallowing `PUT` errors:** Moderate risk. The write succeeded in the database, but the cache is not warmed. Subsequent reads will experience cache misses until the cache recovers.
- **Swallowing `EVICT` errors:** High risk. If Redis was temporarily reachable during a previous write, or connectivity is intermittent, failing to evict a modified record leaves stale data in the cache. When Redis connectivity resumes, subsequent reads will return stale cached data. In financial or security-critical domains, rethrow `handleCacheEvictError` or publish an outbox event to guarantee asynchronous retry of cache invalidation.
