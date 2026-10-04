---
order: 10
search: false
---

# Exercises: Spring Caching Abstraction

Practice diagnosing and resolving Spring Cache proxy interception pitfalls, cache stampede mitigations, transactional consistency anomalies, and distributed cache infrastructure failover.

---

## Exercise: cache-proxy-bypass-and-sync - Diagnose Proxy Self-Invocation and Stampede Deadlock

### Problem Statement
In an inventory management system, `WarehouseService.getAvailableStock(Long productId)` checks for inventory availability and delegates to `this.fetchStockFromWarehouse(productId)`. The `fetchStockFromWarehouse` method is annotated with `@Cacheable(cacheNames = "inventory", key = "#productId")`.

```java
@Service
public class WarehouseService {

    private final InventoryRepository inventoryRepo;

    public WarehouseService(InventoryRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    public AvailableStock getAvailableStock(Long productId) {
        // Business validation & logging
        return this.fetchStockFromWarehouse(productId);
    }

    @Cacheable(cacheNames = "inventory", key = "#productId")
    public AvailableStock fetchStockFromWarehouse(Long productId) {
        return inventoryRepo.queryWarehouseStock(productId);
    }
}
```

During flash-sale testing:
1. Developers notice the database is hit on every call to `getAvailableStock`, despite Redis containing valid cached keys for products.
2. To mitigate an intense thundering herd on a newly released product, a developer attempts to modify the annotation:
   ```java
   @Cacheable(cacheNames = "inventory", key = "#productId", sync = true, unless = "#result == null")
   public AvailableStock fetchStockFromWarehouse(Long productId) { ... }
   ```
   Upon deployment, every request throws:
   `java.lang.IllegalArgumentException: @Cacheable(sync=true) does not support unless attribute`.

### Requirements
1. Identify the exact architectural mechanism that causes `fetchStockFromWarehouse` to bypass the Spring cache when invoked from `getAvailableStock`.
2. Explain why Spring's `CacheAspectSupport` explicitly forbids combining `sync = true` with the `unless` attribute.
3. Refactor `WarehouseService` into idiomatic, clean Spring architecture that:
   - Eliminates the self-invocation proxy bypass without circular references or `ApplicationContext` injection.
   - Properly achieves cache stampede protection on high-concurrency misses without triggering runtime exceptions.

---

## Exercise: cache-put-evict-transactional-boundary - Write Consistency with @CachePut and Transaction Rollback

### Problem Statement
A banking service `CustomerAccountService` updates customer profile information and handles account termination. Both operations are wrapped in Spring declarative transactions:

```java
@Service
public class CustomerAccountService {

    private final AccountRepository accountRepo;
    private final AuditClient auditClient;

    public CustomerAccountService(AccountRepository accountRepo, AuditClient auditClient) {
        this.accountRepo = accountRepo;
        this.auditClient = auditClient;
    }

    @Transactional
    @CachePut(cacheNames = "accounts", key = "#result.id")
    public Account updateAccount(AccountUpdateRequest req) {
        Account acc = accountRepo.findById(req.accountId()).orElseThrow();
        acc.updateEmail(req.email());
        Account saved = accountRepo.save(acc);
        auditClient.publishAuditEvent(saved); // May throw NetworkException
        return saved;
    }

    @Transactional
    @CacheEvict(cacheNames = "accounts", key = "#accountId")
    public void closeAccount(Long accountId) {
        accountRepo.markClosed(accountId);
        auditClient.publishAuditEvent(new AccountClosedEvent(accountId)); // May throw NetworkException
    }
}
```

In production, intermittent network failures with `auditClient` cause transactions to roll back:
1. When `updateAccount` throws a `NetworkException`, the database transaction rolls back, but Redis already contains the updated `Account` written by `@CachePut`. Subsequent calls to `findById` return dirty, uncommitted data from Redis!
2. When `closeAccount` throws a `NetworkException`, the database transaction rolls back. Because `@CacheEvict` uses `beforeInvocation = false` by default, the cache entry is not evicted. However, a junior developer changed it to `beforeInvocation = true`, which evicted the active account from Redis even though the account was never closed in the database.

### Requirements
1. Explain the order of execution between Spring's `TransactionInterceptor` and `CacheInterceptor` by default, and why `@CachePut` inside `@Transactional` causes dirty reads when transactions roll back.
2. Analyze the consistency trade-off of `@CacheEvict(beforeInvocation = true)` vs `beforeInvocation = false` in transactional write paths.
3. Refactor `CustomerAccountService` to guarantee that Redis cache updates or invalidations are executed **only after** the database transaction successfully commits.

---

## Exercise: redis-resilience-cache-error-handler - High Availability Failover with Custom CacheErrorHandler

### Problem Statement
An online bookstore uses Redis via `RedisCacheManager` to cache book catalog details. During peak hours, an AWS elastic network interface failover causes a 30-second disruption in connectivity between Spring Boot application pods and the Redis cluster.

Even though the primary PostgreSQL database was running at only 15% CPU and could easily handle the read traffic:
- Every request to `@Cacheable(cacheNames = "books", key = "#isbn")` crashed with:
  `org.springframework.data.redis.RedisConnectionFailureException: Unable to connect to Redis`.
- The entire storefront returned HTTP 500 errors to end users during the Redis outage.

### Requirements
1. Explain the behavior of Spring's default `SimpleCacheErrorHandler` and why it causes cascading outages when caching infrastructure degrades.
2. Implement a production-ready `ResilientCacheErrorHandler` by implementing Spring's `CacheErrorHandler` interface and registering it via `CachingConfigurer`:
   - Cache `GET` operations must fail open: log a warning with the cache name and key, swallow the exception, and allow execution to fall back to the PostgreSQL database.
   - Cache `PUT`, `EVICT`, and `CLEAR` operations must log structured error details with key and cache context.
3. Detail the consistency implications of swallowing errors on write paths (`PUT` and `EVICT`) versus letting them fail.
