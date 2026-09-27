---
order: 20
search: false
---

# Solution

## Solution: checkout-recovery - Choose the distributed boundary

Use a Saga: the product accepts a short `PENDING` state, and holding 2PC prepared locks across a payment provider and
independent services is a poor availability fit. `POST /orders` creates an order plus an outbox row in one local
transaction, returns the order ID, then the workflow charges with idempotency key `order-123:charge`, reserves stock,
and marks the order `CONFIRMED` only after both local actions succeed.

If inventory rejects after payment succeeds, record `RefundPayment(order-123)` as pending before attempting it. A
timeout is not evidence that the refund failed: retry the same idempotency key, query the provider when needed, and
leave the order visibly `CANCELLATION_PENDING` until a durable worker records the outcome. Escalate repeated failure
to a DLQ/operations workflow rather than hiding it.

The outbox prevents the local dual write: the payment/order state change and `PaymentCharged` intent commit in one
database transaction; a relay or CDC process publishes later. It can publish twice after a crash, so every consumer
deduplicates by immutable event ID or makes its state transition idempotent.
