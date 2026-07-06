---
order: 50
---

# Spring Transactions — @Transactional Deep Dive

---

## How @Transactional works — the proxy mechanism

Spring wraps every `@Transactional` bean in a **proxy** (JDK dynamic proxy if the bean implements an interface; CGLIB subclass proxy otherwise). The proxy intercepts every method call:

```
Caller → Proxy (begin TX) → Target bean method → Proxy (commit or rollback)
```

The bean object is untouched. The proxy is what Spring registers in the application context; the caller never holds a direct reference to the bean.

Under the hood: the proxy calls `PlatformTransactionManager.getTransaction()` (creates or joins a TX based on propagation), runs the method, then `commit()` or `rollback()` based on whether an exception was thrown.

---

## The self-invocation trap

```java
@Service
public class OrderService {

    public void placeOrder(Order order) {
        // calls a @Transactional method on the same class
        this.sendConfirmation(order);   // BYPASSES THE PROXY
    }

    @Transactional
    public void sendConfirmation(Order order) { ... }  // @Transactional is IGNORED
}
```

`this.sendConfirmation(order)` calls the method directly on the target object, skipping the proxy. Spring cannot intercept it, so the transaction is never started.

**Fix options:**

1. **Inject self** — `@Autowired private OrderService self;` then `self.sendConfirmation(order)`. The injected reference is the proxy.
2. **Restructure** — move `sendConfirmation` to a separate service bean. Cleaner; preferred.
3. **AspectJ weaving** — compile-time or load-time weaving bypasses the proxy model. Rarely used outside specialised setups.

---

## Propagation types

Propagation tells Spring what to do when a `@Transactional` method is called with or without an existing transaction.

| Propagation | If TX exists | If no TX exists |
|---|---|---|
| `REQUIRED` (default) | Join the existing TX | Create a new TX |
| `REQUIRES_NEW` | Suspend existing TX; open a new independent TX | Create a new TX |
| `NESTED` | Create a savepoint inside the existing TX | Create a new TX |
| `SUPPORTS` | Join the existing TX | Run non-transactionally |
| `NOT_SUPPORTED` | Suspend existing TX; run non-transactionally | Run non-transactionally |
| `MANDATORY` | Join the existing TX | Throw `IllegalTransactionStateException` |
| `NEVER` | Throw `IllegalTransactionStateException` | Run non-transactionally |

### REQUIRED vs REQUIRES_NEW — the interview question

```java
@Transactional                        // REQUIRED (default)
public void outer() {
    doSomeWork();
    inner();                          // joins outer TX
}

@Transactional(propagation = Propagation.REQUIRED)
public void inner() {
    // shares outer's TX — if inner throws, the ENTIRE TX is marked rollback-only
    riskyWork();
}
```

```java
@Transactional
public void outer() {
    doSomeWork();
    auditService.log(event);          // REQUIRES_NEW — independent TX
    // if log() commits, that commit stands even if outer() rolls back
}

// AuditService
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void log(AuditEvent event) {
    // runs in its own TX; outer TX is suspended while this runs
}
```

Key difference: with `REQUIRED`, a rollback in the inner method marks the shared transaction as rollback-only — the outer cannot commit even if it catches the exception (`UnexpectedRollbackException`). With `REQUIRES_NEW`, the inner TX commits or rolls back independently; the outer is unaffected.

### NESTED vs REQUIRES_NEW

`NESTED` uses a JDBC savepoint inside the existing transaction. Inner rollback goes back only to the savepoint — the outer continues. If the outer rolls back, it takes the inner with it (same physical transaction). `REQUIRES_NEW` creates a separate physical transaction.

```java
@Transactional
public void processPayment(Payment p) {
    chargeCard(p);
    try {
        notificationService.sendSms(p);   // NESTED — failure here rolls back SMS only
    } catch (SmsException e) {
        log.warn("SMS failed, payment still committed");
    }
    // outer TX commits the payment regardless of SMS outcome
}

@Transactional(propagation = Propagation.NESTED)
public void sendSms(Payment p) { ... }
```

Note: `NESTED` requires JDBC savepoint support. PostgreSQL has it; some NoSQL-backed JPA providers don't.

---

## Isolation levels

Isolation controls what one transaction can see from concurrent transactions.

| Level | Dirty Read | Non-Repeatable Read | Phantom Read | Notes |
|---|---|---|---|---|
| `READ_UNCOMMITTED` | Possible | Possible | Possible | Rarely used; dangerous |
| `READ_COMMITTED` | No | Possible | Possible | PostgreSQL default |
| `REPEATABLE_READ` | No | No | Possible | MySQL InnoDB default |
| `SERIALIZABLE` | No | No | No | Highest contention; row-level locking or MVCC |

**Dirty read:** reading a row another uncommitted TX has modified — you see data that may be rolled back.

**Non-repeatable read:** reading the same row twice within a TX and getting different values — another TX committed between reads.

**Phantom read:** running the same range query twice and getting a different row count — another TX inserted/deleted rows in the range.

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public List<Order> getOpenOrders(Long tenantId) { ... }
```

**Interview nuance:** setting `isolation` in Spring only works if the JDBC driver and database honour it — actual enforcement is at the DB, not Spring. Changing isolation has no effect with `readOnly = true` in some setups — the read-replica's isolation is fixed by DB config.

---

## Rollback rules

Spring's defaults:

- **Rolls back** on `RuntimeException` (unchecked) and `Error`.
- **Does NOT roll back** on checked exceptions (`Exception` subclasses that aren't `RuntimeException`).

Common bug source — throwing a checked exception from a `@Transactional` method commits the transaction.

```java
// Rolls back only on RuntimeException (default)
@Transactional
public void transfer(Transfer t) throws InsufficientFundsException {
    // if InsufficientFundsException is thrown, TX COMMITS (data is persisted up to the throw)
}

// Fix — explicitly include the checked exception
@Transactional(rollbackFor = InsufficientFundsException.class)
public void transfer(Transfer t) throws InsufficientFundsException { ... }

// Roll back on all exceptions
@Transactional(rollbackFor = Exception.class)
public void transfer(Transfer t) throws Exception { ... }

// Commit even on a RuntimeException (e.g. idempotency — already-processed is fine)
@Transactional(noRollbackFor = AlreadyProcessedException.class)
public void process(Event e) { ... }
```

---

## readOnly = true

```java
@Transactional(readOnly = true)
public List<User> getUsers(Long tenantId) { ... }
```

`readOnly = true` is a **hint**, not enforcement. What actually happens:

1. **Hibernate skips dirty checking** — no snapshot comparison at flush time. Faster for read-heavy methods with many entities.
2. **Hibernate skips acquiring write locks** on the session.
3. **JDBC driver may route to a read replica** — if the connection pool is configured for routing (e.g., AWS Aurora read/write splitting, or `AbstractRoutingDataSource`). The most impactful benefit in production.
4. **Does NOT prevent calling `save()`** at the JPA level — Hibernate may silently ignore the persist, or throw, depending on flush mode and provider version. Treat it as a contract, not a guard.

---

## Transaction timeout

```java
@Transactional(timeout = 30)   // 30 seconds
public void longRunningImport(List<Record> records) { ... }
```

If the transaction is still open after 30 seconds, `PlatformTransactionManager` marks it for rollback and throws `TransactionTimedOutException` (a `RuntimeException`). Useful for protecting against runaway queries or unresolved deadlocks.

---

## Common interview gotchas

| Scenario | What happens |
|---|---|
| `@Transactional` on a `private` method | Proxy can't intercept — annotation is silently ignored |
| Calling `@Transactional` method via `this` | Same as above — bypasses proxy |
| Inner `REQUIRED` method throws checked exception | TX is NOT rolled back (Spring default); outer may commit stale partial data |
| Inner `REQUIRED` method throws RuntimeException, outer catches it | TX is marked rollback-only; outer's eventual commit throws `UnexpectedRollbackException` |
| Using `@Transactional` on a Spring bean's method called from `@Async` executor | A new independent TX is created for the async thread — not part of the caller's TX |
| `LazyInitializationException` after method returns | Entity was loaded inside TX; session closed when TX committed; lazy collection accessed outside TX |

---

## Quick recall

**Q. How does Spring apply @Transactional — bytecode modification or proxy?**
A. Proxy — either JDK dynamic proxy (interface) or CGLIB subclass. The proxy intercepts calls, manages TX boundaries.

**Q. What is the self-invocation trap and how do you fix it?**
A. Calling `this.method()` inside the same bean bypasses the proxy — @Transactional on that method is ignored. Fix: inject self or extract to a separate bean.

**Q. REQUIRED vs REQUIRES_NEW — if the inner method rolls back, what happens to the outer?**
A. REQUIRED: they share the TX — inner rollback marks it rollback-only, outer cannot commit (throws `UnexpectedRollbackException`). REQUIRES_NEW: inner TX is independent; outer continues unaffected.

**Q. Does Spring roll back on a checked exception by default?**
A. No — only on `RuntimeException` and `Error`. Use `rollbackFor = Exception.class` to include checked exceptions.

**Q. What does `readOnly = true` actually do?**
A. Hints Hibernate to skip dirty checking/write locks, and hints the connection pool to route to a read replica. It does NOT enforce read-only at the DB level.

**Q. `NESTED` vs `REQUIRES_NEW` — key difference?**
A. `NESTED` is a savepoint inside the same physical TX (outer rollback takes inner with it); `REQUIRES_NEW` is a separate physical TX (fully independent lifecycle).

**Q. `@Transactional` on a private method — does it work?**
A. No — CGLIB/JDK proxies can't intercept private methods. The annotation is silently ignored.

