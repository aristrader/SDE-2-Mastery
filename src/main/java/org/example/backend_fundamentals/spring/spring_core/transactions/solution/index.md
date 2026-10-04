---
order: 20
search: false
---

# Solutions: Spring Transactions

Concise architectural solutions and explanations for Spring declarative transaction exercises.

---

## Solution: proxy-self-invocation-fix - Fixing Self-Invocation and Proxy Bypass

### Root Cause
Spring's declarative transactions rely on AOP dynamic proxies (CGLIB or JDK dynamic proxies). When calling `this.applyLoyaltyDiscount()` or `this.deductInventory()`, execution occurs directly on the underlying target instance, bypassing the Spring proxy wrapper. Consequently, transaction interceptors are never triggered, and `Propagation.REQUIRES_NEW` is completely ignored.

### Solution
Extract the distinct business operations into dedicated collaborator beans:

```java
@Service
public class OrderProcessingService {
    private final LoyaltyService loyaltyService;
    private final InventoryService inventoryService;

    public OrderProcessingService(LoyaltyService loyaltyService, InventoryService inventoryService) {
        this.loyaltyService = loyaltyService;
        this.inventoryService = inventoryService;
    }

    public void processOrder(Order order) {
        loyaltyService.applyLoyaltyDiscount(order);
        inventoryService.deductInventory(order);
    }
}

@Service
public class LoyaltyService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyLoyaltyDiscount(Order order) {
        // executes in an independent physical transaction through Spring proxy
    }
}

@Service
public class InventoryService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deductInventory(Order order) {
        // executes in an independent physical transaction through Spring proxy
    }
}
```

---

## Solution: unexpected-rollback-handling - Silent Catch and UnexpectedRollbackException

### Root Cause
Both `transfer()` and `recordAuditLog()` execute within the same shared physical transaction because `recordAuditLog()` uses default `Propagation.REQUIRED`. When `auditRepo.save()` throws a `DataAccessException` (a `RuntimeException`), the Spring transaction aspect intercepts it and marks the shared transaction as **rollback-only** (`setRollbackOnly()`) before propagating the exception up.

Even though `TransferService` catches the exception in a `try-catch` block and attempts to commit upon return, `PlatformTransactionManager` detects that the transaction was flagged as rollback-only. It refuses to commit and throws `UnexpectedRollbackException` to signal data integrity violation.

### Solution Options

#### Option 1: Isolate Audit Logging with `REQUIRES_NEW`
Run `recordAuditLog` in an independent physical transaction so failures do not taint the outer transaction:

```java
@Service
public class AuditService {
    private final AuditRepository auditRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAuditLog(Long fromId, Long toId, BigDecimal amount) {
        auditRepo.save(new AuditLog(fromId, toId, amount));
    }
}
```

#### Option 2: Decouple via Spring Application Events (`@TransactionalEventListener`)
Publish an event that logs the audit record after the main transaction commits successfully:

```java
@Service
public class TransferService {
    private final AccountRepository accountRepo;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        accountRepo.debit(fromId, amount);
        accountRepo.credit(toId, amount);
        eventPublisher.publishEvent(new TransferCompletedEvent(fromId, toId, amount));
    }
}

@Component
public class AuditEventListener {
    private final AuditRepository auditRepo;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransferCompleted(TransferCompletedEvent event) {
        auditRepo.save(new AuditLog(event.fromId(), event.toId(), event.amount()));
    }
}
```

---

## Solution: checked-exception-rollback-trap - Checked Exception Rollback Semantics

### Root Cause
By default, Spring declarative transactions follow EJB conventions:
- **Rollback triggered on:** `RuntimeException` (unchecked exceptions) and `Error`.
- **Commit proceeds on:** Checked exceptions (`java.lang.Exception` subclasses other than `RuntimeException`).

Because `PaymentProcessingException extends Exception` is a checked exception, Spring considers it an expected application result rather than a system failure, committing all pending database mutations unless explicitly instructed otherwise.

### Fix
Specify `rollbackFor` on the `@Transactional` annotation:

```java
@Transactional(rollbackFor = PaymentProcessingException.class)
public void processRefund(Long refundId, BigDecimal amount) throws PaymentProcessingException {
    RefundRecord record = refundRepo.findById(refundId).orElseThrow();
    record.setStatus(RefundStatus.FAILED);
    refundRepo.save(record);

    merchantRepo.deductReserve(record.getMerchantId(), amount);

    if (record.isFraudRisk()) {
        throw new PaymentProcessingException("Fraud risk triggered for refund: " + refundId);
    }
}
```

Alternatively, configure `@Transactional(rollbackFor = Exception.class)` at the class level if all checked exceptions in the service should trigger rollbacks.

### Best Practice
In modern Spring architecture, prefer subclassing `RuntimeException` for custom business exceptions (e.g., `PaymentProcessingException extends RuntimeException`), eliminating boilerplate `throws` signatures and ensuring default rollback behavior.
