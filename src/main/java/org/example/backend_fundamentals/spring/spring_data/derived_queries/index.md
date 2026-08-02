---
order: 30
---

# Derived Queries & @Query

Spring Data can create queries from repository method names. For interviews, know when derived queries are clean, when to switch to `@Query`, and the basics of pagination/projections.

---

## Derived queries

Spring parses repository method names at application startup.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmailAndTenantId(String email, Long tenantId);

    List<User> findByStatusOrderByCreatedAtDesc(UserStatus status);
}
```

Common prefixes:

- `findBy...` / `getBy...` / `readBy...`
- `existsBy...`
- `countBy...`
- `deleteBy...`

Common keywords:

- `And`, `Or`
- `Between`
- `LessThan`, `GreaterThan`
- `In`
- `IsNull`, `IsNotNull`
- `Containing`, `StartingWith`
- `OrderBy`
- `Top`, `First`

---

## When derived queries are good

Use derived queries for simple predicates:

```java
List<Order> findByStatus(OrderStatus status);

List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

boolean existsByEmail(String email);
```

They are readable, short, and fail fast at startup if the field name is wrong.

Example mistake:

```java
List<User> findByEmailAddress(String email);
```

If the entity field is `email`, not `emailAddress`, startup fails with a property reference error.

---

## When to use @Query

Use `@Query` when the method name becomes hard to read.

```java
@Query("""
    select o
    from Order o
    where o.customer.id = :customerId
      and o.status = :status
      and o.createdAt >= :from
    """)
List<Order> findRecentOrders(Long customerId, OrderStatus status, Instant from);
```

Switch to `@Query` for:

- 3+ conditions where the method name becomes long
- joins/fetch joins
- aggregations
- custom sorting/filtering logic
- queries that need to be read by humans later

JPQL uses entity names and field names, not table/column names.

---

## Native queries

Use native SQL only when JPQL cannot express the query cleanly.

```java
@Query(
    value = """
        select *
        from users
        where tenant_id = :tenantId
          and created_at > now() - interval '30 days'
        """,
    nativeQuery = true
)
List<User> findRecentUsers(Long tenantId);
```

Use cases:

- database-specific functions
- window functions
- CTEs
- vendor-specific JSON operations

Trade-off: native SQL is less portable and uses table/column names.

---

## @Modifying

Use `@Modifying` for update/delete queries.

```java
@Modifying
@Transactional
@Query("update User u set u.status = :status where u.tenantId = :tenantId")
int updateStatusForTenant(UserStatus status, Long tenantId);
```

Important points:

- It needs a transaction.
- Return `int` if you want affected row count.
- Bulk updates bypass managed entity state.

If entities were already loaded in the same persistence context, use `clearAutomatically = true` to avoid stale in-memory data.

```java
@Modifying(clearAutomatically = true)
@Query("delete from Session s where s.expiresAt < :now")
int deleteExpiredSessions(Instant now);
```

---

## Projections

Use projections when you need only a few fields instead of full entities.

Interface projection:

```java
public interface UserSummary {
    Long getId();
    String getEmail();
}

List<UserSummary> findByTenantId(Long tenantId);
```

DTO projection:

```java
public record UserSummaryDto(Long id, String email) {
}

@Query("select new com.example.UserSummaryDto(u.id, u.email) from User u where u.tenantId = :tenantId")
List<UserSummaryDto> findSummaries(Long tenantId);
```

Use projections for read-only screens/APIs where loading the full entity graph is unnecessary.

---

## Page vs Slice

```java
Page<User> findByTenantId(Long tenantId, Pageable pageable);

Slice<User> findByTenantId(Long tenantId, Pageable pageable);
```

| Return type | What it gives | Cost |
|---|---|---|
| `Page<T>` | content + total elements/pages | runs count query |
| `Slice<T>` | content + has next page | avoids count query |

Use `Page` when UI needs total pages. Use `Slice` for “load more” or infinite scroll.

---

## Quick recall

**Q. When are derived queries good?**  
A. Simple, readable predicates like `findByEmail` or `existsByEmailAndTenantId`.

**Q. When should you switch to `@Query`?**  
A. When the method name becomes long, needs joins, aggregation, or custom logic.

**Q. JPQL uses table names or entity names?**  
A. Entity and field names.

**Q. Why use `@Modifying`?**  
A. For update/delete queries; otherwise Spring treats the query like a select.

**Q. Why use projections?**  
A. To fetch only needed fields for read-only use cases.

**Q. `Page` vs `Slice`?**  
A. `Page` runs a count query and knows totals. `Slice` only knows whether there is a next page.
