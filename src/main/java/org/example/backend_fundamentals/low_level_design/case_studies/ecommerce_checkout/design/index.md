---
order: 20
search: false
---

# Design notes — E-commerce Checkout

Use this page after attempting the exercise. Checkout is a small workflow with real side effects: reducing
inventory and initiating payment. The design must make its normal path and its undo path equally explicit.

## Start from one checkout request

The caller sends a cart and an idempotency key. The checkout service returns an earlier order immediately when
that key was already processed. For a new request, it validates the complete cart, reserves all items together,
creates an order snapshot, and then attempts payment.

```mermaid
sequenceDiagram
    participant Caller
    participant Checkout
    participant Inventory
    participant Orders
    participant Payment as Payment port

    Caller->>Checkout: checkout(cart, idempotencyKey)
    Checkout->>Orders: find by idempotency key
    alt duplicate request
        Orders-->>Caller: existing order
    else new request
        Checkout->>Inventory: validate and reserve all line items
        alt stock unavailable
            Inventory-->>Caller: reject; no stock changed
        else reserved
            Checkout->>Orders: create order with price snapshot
            Checkout->>Payment: attempt payment
            alt payment succeeds
                Checkout->>Orders: mark confirmed
            else payment fails
                Checkout->>Inventory: release full reservation
                Checkout->>Orders: mark payment failed
            end
        end
    end
```

The reader question is: **which state must be restored when payment fails?** The entire successful inventory
reservation, not merely the item whose check happened last.

## Responsibilities and invariants

| Responsibility | Own it here | Why |
| --- | --- | --- |
| Mutable shopping intent | `Cart` and `CartItem` | A cart can change before checkout. |
| Sellable stock and reservation | product/inventory component | It owns available quantity and rejects an all-or-nothing reservation. |
| Historical purchase facts | `Order` and `OrderItem` | A completed order must retain the SKU, price, and quantity used at checkout. |
| Payment outcome | payment port | The checkout flow needs success/failure, not provider-specific details. |
| Retry deduplication | checkout service/order store | It maps one idempotency key to one order and side-effect sequence. |

Two invariants drive the implementation:

1. A stock failure changes no product quantity. Validate every line item before deducting any stock.
2. One idempotency key creates at most one order and at most one payment attempt.

## Why snapshot order items

Do not let a completed order read today's product price. A product can later be repriced or renamed, but an
order is evidence of what the customer agreed to buy. Copy each requested SKU, unit price, and quantity into an
immutable order item at checkout time.

## Reservation and compensation

For the base scope, reservation is synchronous and local:

1. Validate that every requested SKU exists and quantity is positive.
2. Confirm every product has enough availability.
3. Deduct every quantity as one reservation.
4. Attempt payment.
5. Keep the deduction if payment succeeds; restore every deducted quantity if it fails.

The naive alternative—deducting one product and failing halfway through another—leaves a cart rejection with
partially missing inventory. The two-pass validation makes that failure impossible in the in-memory exercise.

## Idempotency is part of the normal path

A timeout does not tell a client whether the server completed checkout. Retrying without a key can reserve stock
twice or charge twice. Therefore lookup by idempotency key occurs before new reservation or payment work.

In production, persist the idempotency record atomically with order creation and define how long a key remains
valid. The base exercise keeps it in memory and single-threaded, so it models the behaviour without database
locking details.

## Follow-up boundaries

An asynchronous provider introduces a `PENDING` order/payment state and a timeout recovery path. Concurrent
checkouts require an inventory-level atomic reservation. Coupons, tax, shipping, cancellation, and refunds are
separate business policies; do not hide them inside the base reservation code.

## Quick recall

- Why is an order item a snapshot? Product data can change after purchase.
- What must a payment failure undo? The entire stock reservation.
- Where is idempotency checked? Before a new reservation or payment attempt.
