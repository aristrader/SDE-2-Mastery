---
order: 50
---

# Spring Transactions

`@Transactional` defines a database transaction boundary. Focus on where to place it, how the proxy works, self-invocation, and rollback rules.

---

## How @Transactional works

Spring applies `@Transactional` using a proxy.

```text
caller
  ↓
Spring proxy starts/joins transaction
  ↓
target method runs
  ↓
proxy commits or rolls back
```

The proxy wraps the bean. That is why the call must come from outside the bean through Spring.

```java
@Service
public class OrderService {

    @Transactional
    public void placeOrder(CreateOrderRequest request) {
        orderRepository.save(new Order(request));
        paymentRepository.save(new Payment(request));
    }
}
```

Spring opens a transaction before the method, commits on success, and rolls back based on exception rules.

---

## Self-invocation trap

This does not start a new transaction for `sendConfirmation`:

```java
@Service
public class OrderService {

    public void placeOrder(Order order) {
        this.sendConfirmation(order); // bypasses Spring proxy
    }

    @Transactional
    public void sendConfirmation(Order order) {
        // transaction annotation is ignored in this call path
    }
}
```

Why: `this.method()` calls the same object directly, not the Spring proxy.

Preferred fix: move the transactional method to another Spring bean.

```java
@Service
public class OrderService {
    private final ConfirmationService confirmationService;

    public void placeOrder(Order order) {
        confirmationService.sendConfirmation(order);
    }
}

@Service
public class ConfirmationService {

    @Transactional
    public void sendConfirmation(Order order) {
        // now called through Spring proxy
    }
}
```

Also avoid `@Transactional` on private methods. Proxies cannot intercept private methods.

---

## Where to put @Transactional

Put it on service methods that represent a business use case.

```java
@Transactional
public void transferMoney(Long fromAccountId, Long toAccountId, BigDecimal amount) {
    Account from = accountRepository.findById(fromAccountId).orElseThrow();
    Account to = accountRepository.findById(toAccountId).orElseThrow();

    from.debit(amount);
    to.credit(amount);
}
```

Do not rely on separate repository transactions when the use case must commit/rollback as one unit.

---

## Propagation

Propagation tells Spring what to do if a transaction already exists. The default is `REQUIRED`.

`REQUIRED` shares the same transaction:

```java
@Transactional
public void placeOrder() {
    orderRepository.save(order);
    inventoryService.reserve(); // REQUIRED joins same transaction
}
```

If `reserve()` fails with a runtime exception, the whole transaction rolls back.

`REQUIRES_NEW` is a separate transaction. A common example is audit logging:

```java
@Transactional
public void placeOrder() {
    orderRepository.save(order);
    auditService.log("order created"); // independent transaction
    throw new RuntimeException("payment failed");
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void log(String message) {
    auditRepository.save(new AuditLog(message));
}
```

The audit log can commit even if `placeOrder()` rolls back.

---

## Rollback rules

Spring rolls back by default on:

- `RuntimeException`
- `Error`

Spring does **not** roll back by default on checked exceptions.

```java
@Transactional
public void transfer() throws InsufficientFundsException {
    throw new InsufficientFundsException();
    // checked exception: transaction commits unless configured otherwise
}
```

Fix:

```java
@Transactional(rollbackFor = InsufficientFundsException.class)
public void transfer() throws InsufficientFundsException {
    throw new InsufficientFundsException();
}
```

Most business exceptions in Spring apps are runtime exceptions partly because of this default.

---

## Common gotchas

| Scenario | What happens |
|---|---|
| `@Transactional` on private method | ignored by proxy |
| calling transactional method through `this` | bypasses proxy |
| checked exception thrown | no rollback by default |
| runtime exception thrown and caught inside same transaction | transaction may still be rollback-only |
| entity lazy field accessed after transaction | possible `LazyInitializationException` |

---

## Quick recall

**Q. How does Spring apply `@Transactional`?**  
A. Through a proxy around the Spring bean.

**Q. What is self-invocation?**  
A. Calling `this.transactionalMethod()` inside the same bean; it bypasses the proxy.

**Q. Where should transactions usually go?**  
A. On service-layer use-case methods.

**Q. Default rollback rule?**  
A. Rollback on unchecked exceptions and `Error`, not checked exceptions.

**Q. `REQUIRED` vs `REQUIRES_NEW`?**  
A. `REQUIRED` shares the current transaction. `REQUIRES_NEW` starts an independent one.
