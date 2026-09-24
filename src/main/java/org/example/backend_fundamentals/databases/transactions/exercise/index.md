---
order: 10
search: false
---

# Practice: Transactions and Isolation

Use the invariant, not the acronym, to choose the protection. State what a retry repeats and what makes that retry safe.

## Exercise: transfer-and-retry - Protect a balance transfer

An API moves ₹100 from A to B. A must never become negative, and two transfers may arrive at once.

1. Write the smallest SQL condition that prevents an overdraft without a prior Java read.
2. Explain why both account updates belong in one transaction.
3. If a deadlock victim is chosen, say exactly what the service retries and what prevents a client retry from creating a second transfer.

## Exercise: on-call-invariant - Protect a cross-row rule

Two clinicians may independently go off call, but at least one must remain on call.

1. Trace how both can commit under a naïve read-then-update flow even though each updates a different row.
2. Choose either serializable isolation or one explicit coordination lock. Explain the mechanism, its cost, and the recovery path.
3. State why an atomic `UPDATE clinician SET on_call = false WHERE id = ?` alone is not enough.

## Quick recall

**Q. What is the first sentence of a transaction design answer?**
A. State the invariant and its database boundary before naming a level or lock.

**Q. What must a retry repeat after a serialization failure?**
A. The entire transaction, including its reads and validation.
