# JPA Entity + Repository — Coding Exercises

## Why this matters
Every KYC event — identity checks, document uploads, status transitions — ends up persisted. Knowing the JPA annotation contracts cold means you stop second-guessing nullability, sequence strategies, and why your JDBC batch update silently broke.

## Domain model

```java
// The entity you'll build out across the exercises.
// Start with a skeleton and add annotations exercise by exercise.

// @Entity                          // Exercise 1
// @Table(name = "transactions")    // Exercise 1
public class Transaction {

    // @Id                                          // Exercise 1
    // @GeneratedValue(strategy = GenerationType.IDENTITY) // Exercise 1
    private Long id;

    // @Column(nullable = false, unique = true)     // Exercise 1
    private String referenceId;

    // @Column(nullable = false)                    // Exercise 1
    private String status;

    // @Column(precision = 19, scale = 4)           // Exercise 1
    private BigDecimal amount;

    // @Column(nullable = false, updatable = false) // Exercise 1
    private LocalDateTime createdAt;

    // @Transient                                   // Exercise 5
    // private boolean highValue; — computed, not stored

    // JPA needs this — add it with Lombok: @NoArgsConstructor
    // If using @Builder, also add @AllArgsConstructor
}
```

---

## Exercise 1: Basic entity mapping (~10 min)

**Goal:** Annotate a `Transaction` entity so JPA can map it to a `transactions` table.

**Task:**
1. Annotate the class with `@Entity` and `@Table(name = "transactions")`.
2. Annotate `id` with `@Id` and `@GeneratedValue(strategy = GenerationType.IDENTITY)`. Use `Long` (boxed), not `long`.
3. Annotate `referenceId` with `@Column(nullable = false, unique = true)`.
4. Annotate `status` with `@Column(nullable = false)`.
5. Annotate `amount` with `@Column(precision = 19, scale = 4)` — appropriate for monetary values.
6. Annotate `createdAt` with `@Column(nullable = false, updatable = false)` — set once, never changed.
7. Add `@NoArgsConstructor` (Lombok) to satisfy the JPA spec requirement for a no-arg constructor.

**Gotcha:** `@Column` defaults to `length = 255`. Fine for UUIDs or structured keys, but `description` or `notes` fields get silently truncated by some databases. Always set `length` explicitly for string fields you didn't design as short.

---

## Exercise 2: Derived queries (~10 min)

**Goal:** Write a `JpaRepository` that Spring Data generates implementations for — no SQL, no JPQL.

**Task:**
Create `TransactionRepository extends JpaRepository<Transaction, Long>` with these method signatures:
1. `List<Transaction> findByStatus(String status)`
2. `List<Transaction> findByAmountGreaterThan(BigDecimal amount)`
3. `List<Transaction> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to)`
4. `boolean existsByReferenceId(String referenceId)`
5. `List<Transaction> findTop10ByStatusOrderByCreatedAtDesc(String status)` — newest-first, limited to 10.

**Gotcha:** Spring Data parses method names at startup, not at call time. A typo (e.g., `findByRefId` when the field is `referenceId`) fails startup with `PropertyReferenceException` — loud, but the cause can be non-obvious.

---

## Exercise 3: @Query with @Modifying (~10 min)

**Goal:** Write JPQL when derived-query naming becomes unreadable, and a mutating query with proper annotations.

**Task:**
1. Add a `@Query` method to find transactions by status and minimum amount:
   ```
   @Query("SELECT t FROM Transaction t WHERE t.status = :status AND t.amount >= :minAmount")
   List<Transaction> findByStatusAndMinAmount(@Param("status") String status,
                                              @Param("minAmount") BigDecimal minAmount);
   ```
2. Add a mutating method to update status by referenceId:
   ```
   @Modifying
   @Transactional
   @Query("UPDATE Transaction t SET t.status = :newStatus WHERE t.referenceId = :refId")
   int updateStatusByReferenceId(@Param("refId") String refId,
                                  @Param("newStatus") String newStatus);
   ```
3. Note: the `int` return is the count of updated rows.

**Gotcha:** `@Modifying` without `@Transactional` throws `TransactionRequiredException` at runtime. The repository-method `@Transactional` is separate from any service-level `@Transactional` — both are needed if you want the repository method to be independently transactional.

---

## Exercise 4: Calling from a service (~10 min)

**Goal:** Wire the repository into a service via constructor injection and use it in realistic ways.

**Task:**
1. Create `TransactionService` with `TransactionRepository` injected via constructor (`@RequiredArgsConstructor`).
2. Implement `List<Transaction> getPendingTransactions()` — calls `findByStatus("PENDING")`, logs the count with `@Slf4j`.
3. Implement `Transaction createTransaction(String referenceId, BigDecimal amount)` — builds a `Transaction`, sets `status = "PENDING"` and `createdAt = LocalDateTime.now()`, calls `repository.save(...)`, returns the saved entity. Annotate with `@Transactional`.
4. Implement `int flagTransaction(String referenceId, String newStatus)` — calls the `@Modifying` query from Exercise 3. Annotate with `@Transactional`.

**Transactional boundary rule:** `@Modifying` repository methods require an active transaction. With `@Transactional` on both the service and repository, Spring's default `REQUIRED` propagation wraps the whole service call in one transaction. Without `@Transactional` on the service, each repository call runs in its own short transaction. For mutation-heavy operations, annotate the service method so all repository calls share one transaction and fail atomically.

**Gotcha:** After `save()`, the returned entity is the JPA-managed version with the generated `id` populated. The object you passed in may not have `id` set yet (depends on flush timing) — always use the returned instance.

---

## Exercise 5: @Transient vs transient (~5 min)

**Goal:** Understand the two different meanings of "transient" in a JPA entity context.

**Task:**
1. Add a method `boolean isHighValue()` to `Transaction` that returns `amount != null && amount.compareTo(new BigDecimal("10000")) > 0`.
2. Add a field `private boolean highValueCache` annotated `@Transient`. Set it in a `@PostLoad` lifecycle method. The field lives on the object but is never stored as a column.
3. Add a `private transient String tempWorkingBuffer` (plain Java `transient` keyword, no annotation). This excludes the field from Java serialization, but JPA ignores the `transient` keyword — it will still try to map the field unless you also add `@Transient`.
4. Write a comment block: "`@Transient` = skip JPA column mapping. `transient` keyword = skip Java serialization. They are orthogonal and independent."

**Gotcha:** The Java `transient` keyword alone does NOT prevent JPA mapping. You get a schema column or a mapping error. Always use `@Transient` for JPA exclusion.

---

## Quick recall

**Q.** Why does a JPA entity need a no-arg constructor?
**A.** The JPA provider (Hibernate) instantiates entities via reflection using `Class.newInstance()` — it must have a no-arg constructor (public or protected).

**Q.** `@GeneratedValue(strategy = IDENTITY)` vs `SEQUENCE` — what does IDENTITY break?
**A.** IDENTITY requires the DB to assign the key after each INSERT, which prevents JDBC batch inserts. SEQUENCE pre-allocates keys in blocks, enabling batching.

**Q.** What does `@GeneratedValue(strategy = AUTO)` actually do?
**A.** Hibernate inspects the dialect and picks IDENTITY, SEQUENCE, or TABLE. In practice it often picks TABLE (a slow lock-based approach), so prefer SEQUENCE or IDENTITY explicitly.

**Q.** A derived query method is named `findByReferenceNo` but the field is `referenceId` — when does this fail?
**A.** At application startup, with `PropertyReferenceException` — not at query time.

**Q.** What happens if you omit `@Transactional` from a `@Modifying` repository method?
**A.** Spring throws `TransactionRequiredException` at runtime when the method is called outside an active transaction.

**Q.** Does `@Transient` (JPA annotation) and `transient` (Java keyword) do the same thing?
**A.** No. `@Transient` tells JPA to skip column mapping. `transient` tells Java serialization to skip the field. JPA ignores the `transient` keyword — you need the annotation for JPA exclusion.
