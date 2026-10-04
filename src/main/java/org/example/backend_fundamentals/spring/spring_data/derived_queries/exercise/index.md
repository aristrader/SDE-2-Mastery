---
order: 10
search: false
---

# Exercises: Derived Queries, @Query & Pagination

## Exercise: query-strategy-refactor - Refactoring Complex Repository Signatures

### Context
A junior engineer submitted a pull request for an e-commerce platform with the following repository method signatures in `OrderRepository`:

```java
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Method A: Multi-filter order retrieval
    List<Order> findByCustomerIdAndStatusAndCreatedAtBetweenAndShippingAddressCountryOrderByCreatedAtDesc(
        Long customerId,
        OrderStatus status,
        Instant from,
        Instant to,
        String country
    );

    // Method B: Customer dropdown summary for admin dashboard
    List<Order> findByCustomerId(Long customerId);

    // Method C: Bulk cancellation
    void cancelAllPendingOrdersForTenant(Long tenantId);
}
```

### Task
1. **Analyze Method A:** Identify readability, maintainability, and query generation risks of this derived query signature. Rewrite it using explicit JPQL `@Query` with named parameters.
2. **Analyze Method B:** The admin dashboard UI only displays `orderId`, `orderNumber`, and `totalAmount`, but `Order` has multiple lazy collections (`items`, `statusHistory`). Explain the performance issue with the current signature and provide a record-based DTO projection solution.
3. **Analyze Method C:** Method C fails at runtime or application startup because Spring Data cannot derive a query from `cancelAllPendingOrdersForTenant`. Write the correct `@Modifying` and `@Query` JPQL implementation to bulk-update `status = 'CANCELLED'` for pending orders belonging to the specified tenant, including necessary annotations and return types.

---

## Exercise: modifying-cache-inconsistency - Diagnosing First-Level Cache Drift in Bulk Operations

### Context
A payment settlement service executes the following transaction workflow:

```java
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void applyPenaltyAndVerify(Long accountId) {
        // Step 1: Load managed entity
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        System.out.println("Initial Balance: " + account.getBalance()); // e.g., 100.00

        // Step 2: Execute bulk update via repository
        accountRepository.deductPenalty(accountId, new BigDecimal("25.00"));

        // Step 3: Fetch balance again
        Account refreshedAccount = accountRepository.findById(accountId).orElseThrow();
        System.out.println("Refreshed Balance: " + refreshedAccount.getBalance());

        // Step 4: Method ends and transaction commits
    }
}
```

Repository definition:
```java
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Modifying
    @Query("update Account a set a.balance = a.balance - :penalty where a.id = :id")
    int deductPenalty(@Param("id") Long id, @Param("penalty") BigDecimal penalty);
}
```

### Task
1. **Explain Output & State:** What balance will `refreshedAccount.getBalance()` print in Step 3 (100.00 or 75.00), and why? Explain how the JPA Persistence Context (1st-level cache) interacts with bulk `@Modifying` queries.
2. **Dirty Checking Overwrite Hazard:** If Step 1 also modified `account.setLastAccessedAt(Instant.now())` in Java before Step 2, what critical data integrity bug occurs upon transaction commit in Step 4?
3. **Remediation:** Provide the exact annotation configuration on `@Modifying` required to prevent both stale reads and dirty-checking overwrite bugs.

---

## Exercise: pagination-architecture-tradeoffs - Designing High-Volume API Pagination

### Context
You are reviewing the architecture of a social activity feed API with over 80 million records in the `activities` table. The existing endpoint repository signature is:

```java
Page<Activity> findByUserIdAndCreatedAtAfter(
    Long userId,
    Instant cutoffTime,
    Pageable pageable
);
```

As the table grew, database CPU spiked to 95% on page requests, and p99 latency exceeded 4 seconds, even though index `idx_user_created (user_id, created_at)` exists.

### Task
1. **Root Cause Analysis:** Explain why `Page<T>` causes severe latency on large tables despite an index on `(user_id, created_at)`. Identify the hidden query executed by Spring Data.
2. **`Page` vs `Slice` Trade-off:** Explain why changing the return type to `Slice<Activity>` resolves the database CPU spike. Describe how `Slice` internally determines `hasNext()` without executing a `COUNT(*)` query.
3. **Keyset (Cursor) Pagination:** For high-throughput infinite scrolling with deep paging (e.g., page 5,000), explain why `LIMIT / OFFSET` degrades and write the repository method signature for a Keyset/Cursor-based query using derived queries or JPQL.
