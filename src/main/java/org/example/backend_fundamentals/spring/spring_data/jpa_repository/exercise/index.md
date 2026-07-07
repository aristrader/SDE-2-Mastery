---
order: 10
search: false
---

# JPA Repository Practice

## Domain model

```java
public class Transaction {
    private Long id;
    private String referenceId;
    private String status;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
```

## Exercise: basic-entity-mapping - Basic entity mapping

### Goal
Annotate a `Transaction` entity so JPA can map it to a `transactions` table.

### Task
1. Annotate the class with `@Entity` and `@Table(name = "transactions")`.
2. Annotate `id` with `@Id` and `@GeneratedValue(strategy = GenerationType.IDENTITY)`. Use `Long` (boxed), not `long`.
3. Annotate `referenceId` with `@Column(nullable = false, unique = true)`.
4. Annotate `status` with `@Column(nullable = false)`.
5. Annotate `amount` with `@Column(precision = 19, scale = 4)` — appropriate for monetary values.
6. Annotate `createdAt` with `@Column(nullable = false, updatable = false)` — set once, never changed.
7. Add `@NoArgsConstructor` (Lombok) to satisfy the JPA spec requirement for a no-arg constructor.

### Gotcha
`@Column` defaults to `length = 255`. Fine for UUIDs or structured keys, but `description` or `notes` fields get silently truncated by some databases.

## Exercise: derived-queries - Derived queries

### Goal
Write a `JpaRepository` that Spring Data generates implementations for — no SQL, no JPQL.

### Task
Create `TransactionRepository extends JpaRepository<Transaction, Long>` with these method signatures:
1. `List<Transaction> findByStatus(String status)`
2. `List<Transaction> findByAmountGreaterThan(BigDecimal amount)`
3. `List<Transaction> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to)`
4. `boolean existsByReferenceId(String referenceId)`
5. `List<Transaction> findTop10ByStatusOrderByCreatedAtDesc(String status)` — newest-first, limited to 10.

### Gotcha
Spring Data parses method names at startup, not at call time. A typo fails startup with `PropertyReferenceException`.

## Exercise: query-modifying - @Query with @Modifying

### Goal
Write JPQL when derived-query naming becomes unreadable, and a mutating query with proper annotations.

### Task
1. Add a `@Query` method to find transactions by status and minimum amount.
2. Add a mutating method to update status by referenceId using `@Modifying` and `@Query("UPDATE ...")`.
3. Note: the `int` return is the count of updated rows.

### Gotcha
`@Modifying` without `@Transactional` throws `TransactionRequiredException` at runtime.

## Exercise: calling-from-service - Calling from a service

### Goal
Wire the repository into a service via constructor injection and use it in realistic ways.

### Task
1. Create `TransactionService` with `TransactionRepository` injected via constructor (`@RequiredArgsConstructor` is fine).
2. Implement `List<Transaction> getPendingTransactions()` — calls `findByStatus("PENDING")`, logs the count with `@Slf4j`.
3. Implement `Transaction createTransaction(String referenceId, BigDecimal amount)` — builds a `Transaction`, sets `status = "PENDING"` and `createdAt = LocalDateTime.now()`, calls `repository.save(...)`, returns the saved entity. Annotate with `@Transactional`.
4. Implement `int flagTransaction(String referenceId, String newStatus)` — calls the `@Modifying` query. Annotate with `@Transactional`.

### Gotcha
After `save()`, the returned entity is the JPA-managed version with the generated `id` populated. `@Modifying` repository methods require an active transaction; Spring's default `REQUIRED` propagation wraps the whole service call when the service method is transactional.

## Exercise: transient-vs-transient - @Transient vs transient

### Goal
Understand the two different meanings of "transient" in a JPA entity context.

### Task
1. Add a method `boolean isHighValue()` to `Transaction` that returns `amount != null && amount.compareTo(new BigDecimal("10000")) > 0`.
2. Add a field `private boolean highValueCache` annotated `@Transient`. Set it in a `@PostLoad` lifecycle method. The field lives on the object but is never stored as a column.
3. Add a `private transient String tempWorkingBuffer` (plain Java `transient` keyword, no annotation). This excludes the field from Java serialization, but JPA ignores the `transient` keyword.
4. Write a comment block: "`@Transient` = skip JPA column mapping. `transient` keyword = skip Java serialization. They are orthogonal and independent."

### Gotcha
The Java `transient` keyword alone does NOT prevent JPA mapping. Always use `@Transient` for JPA exclusion.
