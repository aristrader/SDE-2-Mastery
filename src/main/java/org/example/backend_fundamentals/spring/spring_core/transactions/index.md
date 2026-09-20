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

## Transaction boundary versus concurrency control

`@Transactional` makes one request atomic; it does not serialize two requests that read, calculate, and write the
same record. The transaction boundary must cover the whole business operation, but a concurrent read-modify-write
path also needs a concurrency strategy. See [database transactions](../../../databases/transactions/) for the
lost-update interleaving and the atomic-SQL alternative.

For a lock-based path, acquire the lock, read, calculate, and write inside the same service transaction. A repository
method that obtains a lock is not useful if its transaction ends before the later calculation and save.

## JPA locking: pessimistic or optimistic

Use a **pessimistic** lock when contention on a short coordination path is common or a retry would be expensive. In
Spring Data JPA, a repository query can request the standard JPA lock mode:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select g from GroupEntity g where g.id = :id")
Optional<GroupEntity> findByIdForUpdate(Long id);
```

The provider asks the database for the corresponding write lock; the database keeps it until the surrounding
transaction commits or rolls back. Keep the locked transaction short.

Use **optimistic** locking when conflicts are uncommon. Add a provider-managed version field to the entity:

```java
@Version
private Long version;
```

Conceptually, the update includes the version that was read. If another transaction changed the row first, the update
matches no current version and JPA reports an optimistic-lock failure at flush or commit. Reload, revalidate, and
retry only a bounded, idempotent command; otherwise return a conflict to the caller. Optimistic locking detects a
conflict rather than holding a long-lived database lock.

| Mode | Meaning | Boundary |
|---|---|---|
| `PESSIMISTIC_READ` | Ask the database to protect a read from conflicting writes. | Reader compatibility is database/provider specific. |
| `PESSIMISTIC_WRITE` | Take exclusive coordination before a read-modify-write decision. | Competing work waits, fails on timeout, or can be a deadlock victim. |
| `PESSIMISTIC_FORCE_INCREMENT` | Take the pessimistic write-style lock and advance a versioned entity's version. | Useful only when that version advance is meaningful to the model. |
| `OPTIMISTIC_FORCE_INCREMENT` | Validate optimistically and force a version advance. | No long-lived database lock; a stale update still fails. |

| Choose | When it fits | Caller-visible consequence |
|---|---|---|
| `PESSIMISTIC_WRITE` | Short, contended coordination such as a group-balance update | A competing request waits, times out, or becomes a deadlock victim. |
| `@Version` | Conflicts are rare and retrying the whole decision is acceptable | A stale writer fails explicitly; it must reload/retry or report conflict. |

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
| lock acquired outside the service transaction | later reads/writes are not protected by that lock |
| remote call or user wait while holding a pessimistic lock | unnecessary contention, timeout, or deadlock risk |
| lock timeout, deadlock, or optimistic conflict | the transaction can abort; retry only at a safe idempotent command boundary |

## Further reading

- [Spring Framework: declarative transaction implementation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-decl-explained.html)
- [Spring Data JPA: locking](https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html)
- [Jakarta Persistence: `LockModeType`](https://jakarta.ee/specifications/persistence/4.0/apidocs/jakarta.persistence/jakarta/persistence/lockmodetype)

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

**Q. Does `@Transactional` prevent a lost update?**
A. No. It gives atomic commit/rollback; use a pessimistic lock or `@Version` when concurrent requests can change the same state.
