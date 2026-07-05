---
order: 10
---

# Derived Queries & @Query

---

## How Spring Data parses method names

At startup, Spring Data parses each method name not backed by `@Query` into a `PartTree`. The parse is deterministic and fails fast if the name is invalid or references a field that doesn't exist on the entity.

**Structure:** `<subject> + By + <predicate>`

- **Subject** controls what is returned: `find`/`read`/`get`/`query`/`stream` → entity/collection; `count` → Long; `exists` → boolean; `delete`/`remove` → void or deleted count.
- **By** is the separator between subject and predicate.
- **Predicate** is one or more conditions joined by `And`/`Or`, with optional `OrderBy` at the end.

### Subject prefix table

| Subject prefix | Return type | Example |
|---|---|---|
| `findBy` / `readBy` / `getBy` / `queryBy` / `streamBy` | entity / collection | `findByEmail` |
| `countBy` | `Long` | `countByTenantId` |
| `existsBy` | `boolean` | `existsByEmailAndTenantId` |
| `deleteBy` / `removeBy` | `void` or `long` (deleted count) | `deleteByExpiresAtBefore` |

### Predicate keyword table

| Keyword | SQL equivalent | Example |
|---|---|---|
| `And` | AND | `findByFirstNameAndLastName` |
| `Or` | OR | `findByStatusOrRole` |
| `Between` | BETWEEN ? AND ? | `findByAgeBetween` |
| `LessThan` / `LessThanEqual` | < / <= | `findBySalaryLessThan` |
| `GreaterThan` / `GreaterThanEqual` | > / >= | `findByCreatedAtGreaterThan` |
| `Like` | LIKE ? (you supply %) | `findByEmailLike` |
| `Containing` | LIKE %?% | `findByNameContaining` |
| `StartingWith` | LIKE ?% | `findByNameStartingWith` |
| `In` | IN (…) | `findByStatusIn(List<Status>)` |
| `IsNull` / `IsNotNull` | IS NULL / IS NOT NULL | `findByDeletedAtIsNull` |
| `OrderBy` | ORDER BY … ASC/DESC | `findByStatusOrderByCreatedAtDesc` |
| `Distinct` | SELECT DISTINCT | `findDistinctByLastName` |
| `Top` / `First` | LIMIT / FETCH FIRST | `findTop3ByOrderByScoreDesc` |

---

## Real examples with generated SQL

```java
// 1 — simple equality
List<User> findByEmail(String email);
// SELECT * FROM user WHERE email = ?

// 2 — compound predicate
List<Order> findByStatusAndCreatedAtGreaterThan(OrderStatus status, LocalDateTime since);
// SELECT * FROM order WHERE status = ? AND created_at > ?

// 3 — IN + null check
List<Document> findByStatusInAndDeletedAtIsNull(List<Status> statuses);
// SELECT * FROM document WHERE status IN (?,?,…) AND deleted_at IS NULL

// 4 — pagination + ordering baked in
List<Product> findTop5ByActiveTrueOrderByRatingDesc();
// SELECT * FROM product WHERE active = true ORDER BY rating DESC LIMIT 5

// 5 — existence check (no data load)
boolean existsByEmailAndTenantId(String email, Long tenantId);
// SELECT COUNT(*) > 0 FROM user WHERE email = ? AND tenant_id = ?
```

---

## Limits of derived queries

Method names get unwieldy fast. Queries with 4+ conditions, OR branches, JOINs, or aggregations produce unreadable names. Use `@Query` when:

- The predicate has more than 2–3 conditions.
- You need an OR across different fields.
- The query involves a JOIN that's not navigable by association.
- You need DB-specific functions, CTEs, window functions.

---

## @Query with JPQL

JPQL operates on **entity and field names**, not table/column names. Portable across DB vendors.

```java
// Named parameters — always prefer over positional for readability
@Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.status = :status")
List<User> findActiveByTenant(@Param("tenantId") Long tenantId,
                              @Param("status") UserStatus status);

// Positional — works but fragile if params are reordered
@Query("SELECT u FROM User u WHERE u.email = ?1")
Optional<User> findByEmail(String email);

// JOIN FETCH — avoids N+1 by loading association in one query
@Query("SELECT o FROM Order o JOIN FETCH o.lineItems WHERE o.id = :id")
Optional<Order> findWithLineItems(@Param("id") Long id);
```

**JPQL gotchas:**
- Field names must match entity field names, not column names. `u.createdAt`, not `u.created_at`.
- `JOIN FETCH` in JPQL with `Pageable` causes a Hibernate warning — it loads all results into memory then paginates in Java (not SQL). Use `countQuery` or a separate query for the count if you need pagination with fetch joins.

---

## @Query with native SQL

```java
@Query(value = "SELECT * FROM user WHERE tenant_id = :tenantId AND created_at > NOW() - INTERVAL '30 days'",
       nativeQuery = true)
List<User> findRecentByTenant(@Param("tenantId") Long tenantId);
```

- Operates on **table and column names**. Breaks if table/column is renamed.
- Necessary for DB-specific functions (`NOW() - INTERVAL`, `ILIKE`, `jsonb_` ops), CTEs, window functions, `RETURNING`, anything JPQL can't express.
- Spring Data still maps results to entity types (by column name) or projections.
- Pagination requires a separate `countQuery`:

```java
@Query(value = "SELECT * FROM user WHERE tenant_id = :id",
       countQuery = "SELECT COUNT(*) FROM user WHERE tenant_id = :id",
       nativeQuery = true)
Page<User> findByTenant(@Param("id") Long id, Pageable pageable);
```

---

## @Modifying

Required for any `@Query` that issues an UPDATE or DELETE. Without it, Spring Data treats the query as a SELECT and throws.

```java
@Modifying
@Transactional
@Query("UPDATE User u SET u.status = :status WHERE u.tenantId = :tenantId")
int deactivateAllByTenant(@Param("status") UserStatus status,
                          @Param("tenantId") Long tenantId);
```

- **Must be combined with `@Transactional`** (on the method or calling service).
- Returns `int` (affected row count) or `void`.
- `clearAutomatically = true` evicts modified entities from the persistence context after the update. Without it, in-memory entities don't reflect the bulk update — you'll read stale data if you load the same entities in the same transaction.

```java
@Modifying(clearAutomatically = true)
@Transactional
@Query("DELETE FROM Session s WHERE s.expiresAt < :now")
int deleteExpiredSessions(@Param("now") LocalDateTime now);
```

---

## Projections

Loading full entities when you only need 2–3 fields is wasteful. Projections fix that.

### Interface-based (Spring generates proxy)

```java
public interface UserSummary {
    String getFirstName();
    String getLastName();
    String getEmail();

    // Computed field via SpEL
    @Value("#{target.firstName + ' ' + target.lastName}")
    String getFullName();
}

List<UserSummary> findByTenantId(Long tenantId);
// SELECT first_name, last_name, email FROM user WHERE tenant_id = ?
// (only declared fields fetched — cheaper than SELECT *)
```

Spring generates a JDK proxy implementing the interface. `target` in `@Value` SpEL refers to the backing object with all fields.

### Class-based (DTO via JPQL constructor expression)

```java
public record UserDto(String firstName, String email) {}

@Query("SELECT new com.example.UserDto(u.firstName, u.email) FROM User u WHERE u.tenantId = :id")
List<UserDto> findDtosByTenant(@Param("id") Long tenantId);
```

No proxy overhead — Hibernate calls the constructor directly. More explicit: you know exactly what's fetched.

---

## Pagination — Page vs Slice

Both accept a `Pageable` parameter:

```java
Page<User>  findByTenantId(Long tenantId, Pageable pageable);
Slice<User> findByTenantId(Long tenantId, Pageable pageable);
```

| | `Page<T>` | `Slice<T>` |
|---|---|---|
| Count query | Yes — fires `SELECT COUNT(*)` | No |
| Knows total pages | Yes (`getTotalPages()`, `getTotalElements()`) | No — only `hasNext()` |
| Cost | Higher (2 queries) | Lower (1 query) |
| Use case | Paginated UI with page numbers | Infinite scroll, cursor-based load more |

```java
// Caller
Pageable page = PageRequest.of(0, 20, Sort.by("createdAt").descending());
Page<User> result = userRepo.findByTenantId(tenantId, page);
```

---

## Quick recall

**Q. What subject prefixes does Spring Data recognise in a derived query?**
A. `find`/`read`/`get`/`query`/`stream` (data), `count` (Long), `exists` (boolean), `delete`/`remove` (void or deleted count).

**Q. What's the difference between `Like` and `Containing` keywords?**
A. `Like` passes the value as-is (you include `%`); `Containing` wraps the value in `%…%` automatically.

**Q. Why use `@Modifying(clearAutomatically = true)`?**
A. Bulk UPDATE/DELETE bypasses Hibernate's cache — `clearAutomatically` evicts affected entities so subsequent reads in the same TX don't return stale state.

**Q. Interface projection vs DTO projection — which is cheaper?**
A. DTO (class-based with `new` in JPQL) avoids proxy creation overhead; interface projection generates a JDK proxy per row. DTO is faster at high row counts.

**Q. `Page` vs `Slice` — when does `Slice` win?**
A. Infinite scroll / load-more UIs — `Slice` skips the `COUNT(*)` query; `Page` fires it every time, which is expensive on large tables.

**Q. Why can't you use `JOIN FETCH` with `Pageable` in JPQL?**
A. Hibernate can't push pagination into SQL when a fetch join multiplies rows; it fetches all rows and paginates in memory. Use a separate count query or avoid fetch joins with pagination.
