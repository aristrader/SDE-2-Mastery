---
order: 20
search: false
---

# Answers: Transactions and Isolation

## Solution: transfer-and-retry - Protect a balance transfer

```sql
BEGIN;
UPDATE accounts
SET balance = balance - 100
WHERE id = :fromAccount AND balance >= 100;
-- Require exactly one affected row; otherwise ROLLBACK.
UPDATE accounts
SET balance = balance + 100
WHERE id = :toAccount;
COMMIT;
```

Require each update to affect exactly one row; otherwise roll back. The debit predicate makes the no-overdraft rule part of the write, so two callers cannot both spend the same last ₹100 through stale Java calculations. Both updates belong in one transaction because a missing destination or a failure after the debit must undo that debit. In that same transaction, insert or claim a transfer row whose client request ID has a database `UNIQUE` constraint. A duplicate-key result loads the already-recorded outcome instead of applying a second transfer. If the database aborts the transaction for a deadlock or serialization conflict, retry from `BEGIN`, not only the credit.

## Solution: on-call-invariant - Protect a cross-row rule

The naïve flow is unsafe because A can read B on call while B reads A on call; each then turns itself off. There is no same-row write collision to force one to wait.

One valid answer is a serializable transaction: read the relevant on-call rows, validate that another clinician remains, update the caller's row, and commit. The engine either makes the result equivalent to some serial order or aborts one transaction. The cost is lower throughput and possible aborts under contention; on an abort, retry the complete idempotent operation with bounded backoff.

An equally valid answer is to lock a stable coordination record for the rota, then read the clinicians and recheck the rule while holding that lock. It is more explicit, but all competing flows must acquire the same lock first. A one-row `UPDATE` on the caller cannot protect a rule that depends on another row.

## Quick recall

**Q. Atomicity versus isolation?**
A. Atomicity prevents a half-committed operation; isolation controls interference among concurrent operations.

**Q. Is a deadlock a partial business update?**
A. No. The victim transaction rolls back; retry the whole operation if it is safe and transient.
