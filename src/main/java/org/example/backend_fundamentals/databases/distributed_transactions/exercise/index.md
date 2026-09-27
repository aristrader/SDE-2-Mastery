---
order: 10
search: false
---

# Exercise

## Exercise: checkout-recovery - Choose the distributed boundary

An order service, inventory service, and payment provider own separate stores. The product can show a pending order
for up to two minutes, but it must never silently charge a customer twice or leave a failed reservation hidden.

1. Choose 2PC/XA or a Saga. Explain the naive failure and the trade-off your choice accepts.
2. Give the normal path from `POST /orders` through payment and inventory, including the caller-visible status.
3. Payment succeeds, inventory rejects the reservation, and the first refund request times out. Define the durable
   retry/recovery rule.
4. Explain the dual-write problem after `PaymentCharged` and the transactional-outbox repair.
5. Name the idempotency key or processed-message record that prevents a retry from charging twice.

## Quick recall

**Q. What does a Saga replace a global transaction with?**

A. Local transactions plus durable workflow state, compensations/forward corrections, retries, and a visible
eventual-consistency contract.
