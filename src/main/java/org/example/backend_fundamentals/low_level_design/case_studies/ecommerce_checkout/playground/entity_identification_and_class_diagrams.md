# E-commerce Checkout — entity identification and class diagrams

## Goal

Model the smallest useful in-memory checkout flow that reserves cart inventory, records payment outcome,
and keeps a retry from creating duplicate orders.

## Requirements

- Products have SKU, name, unit price in integer minor units, and available quantity.
- Carts contain one or more SKU/quantity line items.
- Checkout creates an immutable order-item price and quantity snapshot.
- Reserve all stock before payment; reject without changing availability when any item lacks stock.
- Confirm the order after successful payment; release stock after failed payment.
- A repeated idempotency key returns the existing order without another reservation or payment attempt.
- Reject empty carts, unknown SKU, non-positive quantity, and duplicate SKU line items.

## Constraints

- Keep data in memory and money in integer minor units.
- Use a local simulated payment result.
- The base implementation is single-threaded.

## Test scenarios

- Confirm a paid order and reduce stock once.
- Reject insufficient stock without mutating any product quantity.
- Release reserved stock after payment failure.
- Return the existing order for a repeated idempotency key.
- Retain order-item price if product price changes later.

## Interview follow-ups

- How would concurrent checkouts reserve stock safely?
- How would you persist orders, reservations, and idempotency records?
- How would asynchronous payment and timeout recovery change the model?
- How would coupons, taxes, shipping, cancellation, and refunds fit in?

## Entity identification


## Class diagrams
