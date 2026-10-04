---
order: 20
search: false
---

# Solutions: Derived Queries, @Query & Pagination

## Solution: query-strategy-refactor - Refactoring Complex Repository Signatures

### 1. Analysis and JPQL Refactor of Method A

#### Issues with the derived signature
* **Poor readability & maintainability:** At 88 characters, `findByCustomerIdAndStatusAndCreatedAtBetweenAndShippingAddressCountryOrderByCreatedAtDesc` is difficult to parse during code reviews. Refactoring property names requires cascading method renames across callers.
* **Nested property ambiguity:** Derived queries parsing nested paths (such as `ShippingAddressCountry` mapping to `shippingAddress.country`) risk collision if `Order` contains properties like `shippingAddressCountry`.
* **Inflexibility:** Derived methods cannot include query hints, dynamic fetch joins, or formatted multi-line conditions.

#### Refactored JPQL Query
```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        select o
        from Order o
        where o.customerId = :customerId
          and o.status = :status
          and o.createdAt between :from and :to
          and o.shippingAddress.country = :country
        order by o.createdAt desc
        """)
    List<Order> findCustomerOrdersByFilter(
        @Param("customerId") Long customerId,
        @Param("status") OrderStatus status,
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("country") String country
    );
}
```

---

### 2. Analysis and DTO Projection for Method B

#### Performance issue
Method B returns full `Order` managed entities. This causes two main performance issues:
1. **Unnecessary memory & data transfer:** Fetches all entity columns and places full entity snapshots into the JPA Persistence Context (1st-level cache).
2. **Lazy loading hazards & N+1 queries:** Accessing lazy collections (`items`, `statusHistory`) outside an open transaction triggers `LazyInitializationException`. If accessed in a transaction loop during DTO mapping, it causes N+1 SQL queries.

#### Record-based DTO Solution
Define an immutable DTO record and select it using a JPQL constructor expression:

```java
public record OrderSummaryDto(
    Long orderId,
    String orderNumber,
    BigDecimal totalAmount
) {}
```

```java
@Query("""
    select new com.example.dto.OrderSummaryDto(
        o.id,
        o.orderNumber,
        o.totalAmount
    )
    from Order o
    where o.customerId = :customerId
    """)
List<OrderSummaryDto> findSummariesByCustomerId(@Param("customerId") Long customerId);
```

---

### 3. Bulk Update Implementation for Method C

#### Why Method C fails
`cancelAllPendingOrdersForTenant` does not match any Spring Data derived query prefix (`findBy`, `countBy`, `deleteBy`, `existsBy`). Spring Data attempts to parse `cancelAllPendingOrdersForTenant` as an entity property at application bootstrap and throws a `PropertyReferenceException`.

#### Correct `@Modifying` Query Implementation
```java
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Transactional
@Query("""
    update Order o
    set o.status = :cancelledStatus
    where o.tenantId = :tenantId
      and o.status = :pendingStatus
    """)
int cancelAllPendingOrdersForTenant(
    @Param("tenantId") Long tenantId,
    @Param("pendingStatus") OrderStatus pendingStatus,
    @Param("cancelledStatus") OrderStatus cancelledStatus
);
```

#### Key requirements
* `@Modifying`: Tells Spring Data JPA to execute the query as an `executeUpdate()` DML statement rather than `executeQuery()`.
* `return int`: Returns the count of affected database rows.
* `@Transactional`: Modifying queries require an active transaction; otherwise Spring throws `TransactionRequiredException`.
* `clearAutomatically = true`: Clears the `EntityManager` cache post-execution to prevent stale first-level entity cache reads.

---

## Solution: modifying-cache-inconsistency - Diagnosing First-Level Cache Drift in Bulk Operations

### 1. Output and Persistence Context State

#### Output in Step 3
`refreshedAccount.getBalance()` prints **`100.00`** (the stale pre-update balance).

#### Explanation
1. `accountRepository.findById(accountId)` in Step 1 loads the `Account` entity into the current thread's `EntityManager` Persistence Context (1st-level cache) with `balance = 100.00`.
2. Step 2 executes a bulk JPQL `@Modifying` update. Bulk JPQL queries translate directly into an SQL `UPDATE` statement issued directly to the database. They **bypass the Persistence Context** and do not update in-memory managed entity instances. The DB now has `75.00`, but the cache holds `100.00`.
3. In Step 3, `accountRepository.findById(accountId)` evaluates the Persistence Context first. Finding the entity with `id = accountId` already managed, Hibernate returns the in-memory cached instance immediately without issuing a `SELECT` SQL query to the database.

---

### 2. Dirty Checking Overwrite Hazard

If Step 1 modified `account.setLastAccessedAt(Instant.now())`:
1. The managed `Account` entity is marked as **dirty** in the Persistence Context with `balance = 100.00` and updated `lastAccessedAt`.
2. At Step 4, when `@Transactional` completes, Hibernate's automatic dirty checking flushes all managed dirty entities to the database.
3. Hibernate generates and executes an update for the entire entity snapshot:
   ```sql
   UPDATE accounts SET balance = 100.00, last_accessed_at = '2026-10-03 ...' WHERE id = 1;
   ```
4. **Data corruption:** The flush overwrites the database balance back to `100.00`, silently destroying the penalty deduction executed in Step 2.

---

### 3. Remediation

Configure `@Modifying` with both `flushAutomatically = true` and `clearAutomatically = true`:

```java
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Account a set a.balance = a.balance - :penalty where a.id = :id")
    int deductPenalty(@Param("id") Long id, @Param("penalty") BigDecimal penalty);
}
```

* **`flushAutomatically = true`:** Flushes pending in-memory entity changes (like `lastAccessedAt`) to the database *before* the bulk query runs, preventing lost updates.
* **`clearAutomatically = true`:** Evicts all entities from the Persistence Context *after* the bulk query executes. Subsequent calls to `findById` are forced to execute a fresh SQL `SELECT` from the database, returning the correct balance (`75.00`).

---

## Solution: pagination-architecture-tradeoffs - Designing High-Volume API Pagination

### 1. Root Cause Analysis of `Page<T>` Latency

`Page<T>` executes two SQL queries per request:
1. **Data Query:** `SELECT * FROM activities WHERE user_id = ? AND created_at > ? ORDER BY created_at DESC LIMIT 20 OFFSET 0;`
2. **Count Query:** `SELECT COUNT(*) FROM activities WHERE user_id = ? AND created_at > ?;`

#### Why `COUNT(*)` spikes CPU on large tables
Even with an index on `(user_id, created_at)`, `COUNT(*)` cannot return in $O(1)$ time in MVCC relational databases (like PostgreSQL/MySQL InnoDB). The database engine must scan every matching index entry across visibility maps to count live qualifying rows. For active users with thousands of activity rows, calculating the exact total on every page request causes heavy random disk I/O and saturates CPU.

---

### 2. `Page` vs `Slice` Trade-off

#### Why `Slice<Activity>` resolves CPU spikes
`Slice<T>` **does not execute a `COUNT(*)` query**. It only executes the data query, eliminating 50% of query volume and 90%+ of pagination execution time on large datasets.

#### How `Slice` determines `hasNext()`
Spring Data JPA automatically increases the SQL `LIMIT` clause by 1:
$$\text{SQL LIMIT} = \text{pageSize} + 1$$

* If the user requests `pageSize = 20`, Spring Data runs `LIMIT 21`.
* If the database returns **21 rows**: `hasNext()` returns `true`. Spring Data discards the 21st record and returns the first 20 items to the caller.
* If the database returns **$\le 20$ rows**: `hasNext()` returns `false`.

---

### 3. Keyset (Cursor-Based) Pagination for Deep Paging

#### Why `LIMIT / OFFSET` degrades on deep pages
`OFFSET 100000 LIMIT 20` forces the database engine to scan and traverse 100,020 rows in the index, construct intermediate result sets, and discard the first 100,000 rows. Query response time degrades linearly ($O(N)$) as the offset increases.

#### Keyset / Cursor Pagination Strategy
Keyset pagination uses indexed column values (`createdAt`, `id`) from the last item of the previous page as a filter predicate. The query jumps directly to the target position using an $O(\log N)$ B-Tree index lookup.

```java
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Query("""
        select a from Activity a
        where a.userId = :userId
          and (
              a.createdAt < :lastCreatedAt
              or (a.createdAt = :lastCreatedAt and a.id < :lastId)
          )
        order by a.createdAt desc, a.id desc
        """)
    List<Activity> findNextFeedPage(
        @Param("userId") Long userId,
        @Param("lastCreatedAt") Instant lastCreatedAt,
        @Param("lastId") Long lastId,
        Pageable pageable // PageRequest.of(0, pageSize)
    );
}
```

```java
// First page retrieval (cursor is null)
@Query("""
    select a from Activity a
    where a.userId = :userId
    order by a.createdAt desc, a.id desc
    """)
List<Activity> findInitialFeedPage(
    @Param("userId") Long userId,
    Pageable pageable
);
```

#### Benefits
1. **Constant-time execution ($O(1)$ seek time):** Performance on page 5,000 is identical to page 1.
2. **Stable pagination:** New inserts at the top of the feed do not cause duplicate items or skipped elements across page boundaries.
