---
order: 20
search: false
---

# Solutions

---

## Solution: merge-trap-audit - Identifying Lost Updates in Detached Entity Merge

### 1. Root Cause & Entity Lifecycle State
- **Before `save()`**: `detachedAccount` is in the **Detached** state because it possesses an existing database ID but was instantiated outside the current persistence context.
- **After `save()`**: `detachedAccount` **remains in the Detached state**. In JPA, `entityManager.merge(entity)` (which `SimpleJpaRepository.save()` invokes for existing entities) does not re-attach or mutate the argument in-place. Instead, it copies state into a distinct **Managed** instance managed by the current persistence context.
- **Why Updates Are Lost**: In Step 2, mutations (`setLastAuditedAt`, `setAuditScore`) are invoked on `detachedAccount` (the detached reference), which is ignored by Hibernate's dirty checking snapshot mechanism. When the `@Transactional` boundary commits, Hibernate flushes only changes present on the managed instance created during Step 1.

### 2. Internal Hibernate `merge()` Execution Flow
1. Hibernate inspects the entity identifier (`id`).
2. It checks whether the entity already exists in the current Persistence Context (Level 1 cache). If not, it executes a SQL `SELECT` to load the managed entity from the database.
3. Hibernate copies all non-null field values from the detached instance (`detachedAccount`) onto the newly retrieved managed entity instance.
4. Hibernate returns the reference to this **managed entity instance**.

### 3. Corrected Implementation
Always capture the managed entity returned by `save()` / `merge()` before applying further mutations or returning to callers:

```java
@Transactional
public Account updateTierAndAudit(Account detachedAccount) {
    // 1. Capture the managed instance returned by merge/save
    Account managedAccount = accountRepository.save(detachedAccount);

    // 2. Apply business mutations to the MANAGED instance
    managedAccount.setLastAuditedAt(Instant.now());
    managedAccount.setAuditScore(95);

    // 3. Return the managed instance (tracked by dirty checking on commit)
    return managedAccount;
}
```

---

## Solution: dirty-checking-boundary-analysis - Analyzing Dirty Checking Across Service Boundaries

### 1. Persistence Context Scoping & Dirty Checking Failure
- In Spring Data JPA, `SimpleJpaRepository.findById()` executes inside its own internal transaction (`@Transactional(readOnly = true)` on repository implementation).
- Because `ReconciliationService.reconcileTransaction` lacks `@Transactional`, the persistence context opened by `findById()` **closes immediately** once `findById` returns.
- Consequently, the returned `txn` object becomes **Detached**.
- When `txn.setReconciled(true)` and `txn.setFee(fee)` execute, no active persistence context or transaction exists to record the mutation snapshot. Hence, dirty checking never occurs, and no SQL `UPDATE` is emitted.

### 2. Explicit `save()` vs `@Transactional` Boundary
- **Appending `save(txn)` without `@Transactional`**:
  - `save(txn)` calls `merge(txn)`.
  - Spring opens a *new*, separate short-lived transaction for `save()`.
  - Hibernate executes a redundant `SELECT` query to load a managed entity in this new session, copies values from `txn`, and then issues an `UPDATE`.
  - **Overhead**: Generates unnecessary `SELECT` queries and multiple database connection checkouts.
- **Adding `@Transactional` on the service method**:
  - The persistence context spans the entire execution of `reconcileTransaction`.
  - `findById` loads `txn` into the managed state.
  - Mutations are automatically tracked via dirty checking snapshot comparisons.
  - At transaction commit, Hibernate flushes a single SQL `UPDATE` statement. No redundant `SELECT` or manual `save()` calls are needed.

### 3. Idiomatic Spring Data JPA Implementation

```java
@Service
public class ReconciliationService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public void reconcileTransaction(Long txnId, BigDecimal fee) {
        Transaction txn = transactionRepository.findById(txnId)
            .orElseThrow(() -> new NotFoundException("Transaction not found"));

        txn.setReconciled(true);
        txn.setFee(fee);
        // Clean dirty checking: Hibernate updates the row automatically at commit.
    }
}
```

---

## Solution: lazy-initialization-remediation - Resolving LazyInitializationException in REST Endpoints

### 1. Mechanism Causing `LazyInitializationException`
- `UserService.getUser()` is marked `@Transactional(readOnly = true)`.
- When `getUser()` completes and returns the `User` entity to `UserController`, the transactional persistence context closes and the Hibernate `Session` is bound/closed.
- The `orders` collection on `User` is mapped with `FetchType.LAZY`, meaning Hibernate injected a lazy proxy collection (`PersistentBag`).
- When the controller calls `user.getOrders().stream()`, the proxy attempts to initialize itself against the Hibernate Session. Since the session is closed, Hibernate throws `LazyInitializationException: could not initialize proxy - no Session`.

### 2. Evaluation of Open Session in View (OSIV)
Enabling `spring.jpa.open-in-view=true` holds the database session and underlying JDBC connection open across the entire HTTP request-response cycle (including controller execution and JSON view rendering).

**Production Risks in High-Throughput Microservices**:
1. **Connection Pool Starvation**: JDBC connections are held during slow client responses, network I/O, and external API calls, exhausting HikariCP connection pools.
2. **Hidden N+1 Queries**: Serializers navigating associations trigger unexpected, unindexed SQL queries outside transactional boundaries, degrading latency and hiding performance regressions.

### 3. Recommended Production Solutions

#### Approach A: Repository Fetch Join or `@EntityGraph`
Eagerly fetch the association inside the transaction when the caller specifically requires it:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u JOIN FETCH u.orders WHERE u.id = :id")
    Optional<User> findByIdWithOrders(@Param("id") Long id);

    // Or using EntityGraph:
    @EntityGraph(attributePaths = {"orders"})
    Optional<User> findWithOrdersById(Long id);
}
```

#### Approach B: Direct DTO / Record Projection (Best Performance)
Query only the required fields directly into an immutable DTO/record, eliminating entity tracking and proxy overhead:

```java
public record OrderSummaryDto(Long orderId, String status, BigDecimal amount) {}

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
        SELECT new com.example.dto.OrderSummaryDto(o.id, o.status, o.amount)
        FROM Order o
        WHERE o.user.id = :userId
    """)
    List<OrderSummaryDto> findOrderSummariesByUserId(@Param("userId") Long userId);
}
```
In the service:
```java
@Transactional(readOnly = true)
public List<OrderSummaryDto> getUserOrders(Long userId) {
    return orderRepository.findOrderSummariesByUserId(userId);
}
```
