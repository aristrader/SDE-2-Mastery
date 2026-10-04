---
order: 10
search: false
---

# Exercises: Spring Transactions

Practice diagnosing and fixing common Spring declarative transaction bugs, proxy pitfalls, propagation boundaries, and rollback traps encountered in senior backend engineering interviews.

---

## Exercise: proxy-self-invocation-fix - Fixing Self-Invocation and Proxy Bypass

### Problem Statement
In an e-commerce checkout flow, `OrderProcessingService.processOrder()` performs initial validation and calls `this.applyLoyaltyDiscount()` and `this.deductInventory()`. Both sub-methods are annotated with `@Transactional(propagation = Propagation.REQUIRES_NEW)`. During testing, an exception in `deductInventory()` does not isolate `applyLoyaltyDiscount()`, and neither method runs within an independent transaction boundary.

```java
@Service
public class OrderProcessingService {

    public void processOrder(Order order) {
        applyLoyaltyDiscount(order);
        deductInventory(order);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void applyLoyaltyDiscount(Order order) {
        // updates customer loyalty balance
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deductInventory(Order order) {
        // updates stock count
    }
}
```

### Requirements
1. Identify the exact architectural reason why `@Transactional` and `Propagation.REQUIRES_NEW` are ignored.
2. Refactor the design into idiomatic Spring architecture without circular dependencies or anti-patterns (such as injecting `ApplicationContext` or self-injection workarounds).
3. Ensure each operation executes through its own proxy in an isolated transaction.

---

## Exercise: unexpected-rollback-handling - Silent Catch and UnexpectedRollbackException

### Problem Statement
A banking service attempts to notify an external audit system during funds transfer. If the audit logging fails with a `RuntimeException`, the developer catches and logs the exception so the primary transfer can still complete:

```java
@Service
public class TransferService {
    private final AccountRepository accountRepo;
    private final AuditService auditService;

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        accountRepo.debit(fromId, amount);
        accountRepo.credit(toId, amount);

        try {
            auditService.recordAuditLog(fromId, toId, amount);
        } catch (RuntimeException e) {
            log.warn("Audit logging failed, continuing transfer", e);
        }
    }
}

@Service
public class AuditService {
    private final AuditRepository auditRepo;

    @Transactional // Propagation.REQUIRED (default)
    public void recordAuditLog(Long fromId, Long toId, BigDecimal amount) {
        auditRepo.save(new AuditLog(fromId, toId, amount));
    }
}
```

During execution, when `auditRepo.save()` throws a `DataAccessException`, the catch block logs the warning, but `transfer()` ultimately crashes with `org.springframework.transaction.UnexpectedRollbackException: Transaction rolled back because it has been marked as rollback-only`.

### Requirements
1. Explain why catching the exception in `TransferService` fails to prevent the rollback of `transfer()`.
2. Provide two viable architectural solutions to allow the transfer to succeed even if audit logging fails.

---

## Exercise: checked-exception-rollback-trap - Checked Exception Rollback Semantics

### Problem Statement
A payment gateway service processes merchant refunds. If an invalid state occurs, it throws a custom business exception `PaymentProcessingException`:

```java
public class PaymentProcessingException extends Exception {
    public PaymentProcessingException(String message) {
        super(message);
    }
}

@Service
public class RefundService {
    private final RefundRepository refundRepo;
    private final MerchantAccountRepository merchantRepo;

    @Transactional
    public void processRefund(Long refundId, BigDecimal amount) throws PaymentProcessingException {
        RefundRecord record = refundRepo.findById(refundId).orElseThrow();
        record.setStatus(RefundStatus.FAILED);
        refundRepo.save(record);

        merchantRepo.deductReserve(record.getMerchantId(), amount);

        if (record.isFraudRisk()) {
            throw new PaymentProcessingException("Fraud risk triggered for refund: " + refundId);
        }
    }
}
```

When fraud risk is detected, `PaymentProcessingException` is thrown, but the database changes (`deductReserve` and `status = FAILED`) are unexpectedly committed to the database.

### Requirements
1. Explain the default Spring transaction rollback behavior regarding checked vs. unchecked exceptions.
2. Fix the `@Transactional` annotation configuration to ensure `PaymentProcessingException` triggers a rollback.
3. State best practices for exception hierarchy design in enterprise Spring applications regarding transaction management.
