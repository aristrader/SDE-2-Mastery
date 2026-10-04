---
order: 10
search: false
---

# Exercises

---

## Exercise: merge-trap-audit - Identifying Lost Updates in Detached Entity Merge

### Scenario
An engineer is reviewing an account update routine in a Spring Boot service. An external API payload is deserialized into a detached `Account` entity containing an existing primary key ID. The developer attempts to persist and adjust the account:

```java
@Transactional
public Account updateTierAndAudit(Account detachedAccount) {
    // Step 1: Merge detached payload into persistence context
    accountRepository.save(detachedAccount);

    // Step 2: Apply business logic mutation
    detachedAccount.setLastAuditedAt(Instant.now());
    detachedAccount.setAuditScore(95);

    // Step 3: Return entity
    return detachedAccount;
}
```

When inspecting the database after method completion, the updates to `lastAuditedAt` and `auditScore` are completely missing from the database record.

### Tasks
1. Explain why the database does not reflect the updates made in Step 2, identifying the exact lifecycle state of `detachedAccount` before and after `save()`.
2. Trace what Hibernate does internally during `accountRepository.save(detachedAccount)` when given a detached entity.
3. Provide the corrected implementation showing how the returned managed instance should be captured and mutated.

---

## Exercise: dirty-checking-boundary-analysis - Analyzing Dirty Checking Across Service Boundaries

### Scenario
A payment reconciliation workflow updates transaction statuses across multiple database records:

```java
@Service
public class ReconciliationService {

    @Autowired
    private TransactionRepository transactionRepository;

    public void reconcileTransaction(Long txnId, BigDecimal fee) {
        Transaction txn = transactionRepository.findById(txnId)
            .orElseThrow(() -> new NotFoundException("Transaction not found"));

        txn.setReconciled(true);
        txn.setFee(fee);
        // Note: No transactionRepository.save(txn) is called
    }
}
```

During testing, the developer observes that `reconcileTransaction` completes without errors, but no SQL `UPDATE` statement is issued, and the database record remains unchanged (`reconciled = false`).

### Tasks
1. Explain why dirty checking failed to flush the mutations to the database, referencing persistence context scoping and Spring transaction boundaries.
2. What would happen if `transactionRepository.save(txn)` were explicitly appended at the end of the method without `@Transactional`? Contrast the SQL overhead and persistence context lifecycle of this approach versus adding `@Transactional`.
3. Provide the idiomatic Spring Data JPA solution for this update operation.

---

## Exercise: lazy-initialization-remediation - Resolving LazyInitializationException in REST Endpoints

### Scenario
A team lead observes production alert spikes when a new endpoint `/api/v1/users/{id}/orders` is called. The domain model defines a lazy association:

```java
@Entity
public class User {
    @Id
    private Long id;
    private String name;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();
}
```

The service and controller are implemented as follows:

```java
@Service
public class UserService {
    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}/orders")
    public List<OrderResponse> getUserOrders(@PathVariable Long id) {
        User user = userService.getUser(id);
        return user.getOrders().stream() // Throws LazyInitializationException here!
            .map(OrderResponse::from)
            .toList();
    }
}
```

A junior developer proposes setting `spring.jpa.open-in-view=true` in `application.properties` to fix the issue immediately.

### Tasks
1. Identify the exact root cause of `LazyInitializationException` in the controller method.
2. Evaluate the junior developer's proposal to enable Open Session in View (OSIV) in a high-throughput microservice. Outline two significant production risks of OSIV.
3. Design two recommended production alternatives to resolve the issue:
   - Approach A: Repository query with explicit fetch join / `@EntityGraph`.
   - Approach B: Direct DTO / Record projection query.
