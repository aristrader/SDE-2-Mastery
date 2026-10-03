---
search: false
---

# Candidate discussion — E-commerce Checkout

Start by proposing a bounded first scope:

> “I’ll model one in-memory store. A customer checks out one cart containing multiple products. Checkout
> reserves available inventory, invokes a simulated payment result, then either confirms the order or releases
> the reservation. A repeated request with the same idempotency key returns the original order.”

Then confirm the decisions that change the state model.

## Questions to ask

1. Does a cart contain multiple products and quantities, or one product per checkout?
2. Is inventory shared across customers, and must stock be reserved before payment succeeds?
3. Which payment outcomes exist for the first version: success and failure only, or pending too?
4. What should happen to reserved stock when payment fails or the customer retries the same request?
5. Can a checkout request be repeated because the caller timed out? Is an idempotency key available?
6. Are prices copied into an order at checkout, or may completed orders read current product prices?
7. Are coupon application, taxes, shipping, cancellation, refunds, persistence, and delivery part of this round?
8. Must simultaneous checkouts prevent inventory from going negative?

## Agreed scope for this exercise

- One in-memory store holds products, available stock, carts, orders, and checkout idempotency records.
- A cart contains one or more line items with SKU and positive quantity.
- Each product has a SKU, name, unit price in integer minor units, and available quantity.
- Checkout creates an order with an immutable snapshot of item price and quantity.
- Before payment, reserve every requested item atomically; reject checkout if any item lacks stock.
- A simulated payment processor returns success or failure for the base implementation.
- On payment success, confirm the order and keep the stock deduction. On failure, release the reservation and
  mark the order as payment failed.
- A repeated request with the same idempotency key returns the original order and does not charge or reserve again.
- Coupons, taxes, shipping, cancellation/refunds, asynchronous payment, persistence, and concurrent checkout
  coordination are follow-ups.

The final [exercise](../) records the resulting requirements and test scenarios.
