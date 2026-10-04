# E-commerce Checkout — entity identification and class diagrams

## Goal

Model the smallest useful in-memory checkout flow that reserves cart inventory, records payment outcome,
and keeps a retry from creating duplicate orders.

This is the agreed scope after discussing the initial [interviewer prompt](../exercise/problem_statement/) and
[candidate clarifications](../exercise/candidate_discussion/).

## Requirements

- A product has a unique SKU, name, unit price in integer minor units, and available quantity.
- A cart contains one or more line items with a SKU and positive quantity.
- Checkout creates an order containing an immutable snapshot of each purchased item's SKU, price, and quantity.
- Reserve all requested stock before attempting payment. If any item lacks stock, reject checkout without
  changing availability.
- A payment port returns either success or failure in the base implementation.
- On payment success, mark the order confirmed and keep the stock deduction.
- On payment failure, mark the order payment failed and restore all reserved stock.
- A checkout receives an idempotency key. Repeating that key returns the existing order without another stock
  reservation or payment attempt.
- Reject an empty cart, unknown SKU, non-positive quantity, and duplicate SKU line items.

## Constraints

- Keep all data in memory.
- Use integer minor units for money.
- Payment is a local simulated dependency; do not add network calls, persistence, tax, shipping, or delivery.
- The base implementation is single-threaded. Concurrent inventory reservation is a follow-up.

## Test scenarios

- Confirm a paid order and reduce stock once.
- Reject a cart with insufficient stock without reducing any product quantity.
- Release all reserved stock after a payment failure.
- Return the same order for a repeated idempotency key without a second payment attempt.
- Confirm completed order items retain their original price if the product price later changes.
- Reject invalid cart contents without creating an order.

## Interview follow-ups

- How would you make concurrent checkouts reserve stock safely without a global lock?
- How would you persist orders, reservations, and idempotency records with a real payment provider?
- How would you model an asynchronous payment state and recover after a timeout?
- How would you add coupons, taxes, shipping, cancellation, and refunds?
- What distributed-transaction boundary and compensating action would you use across inventory and payment?

## Entity identification
Product
Cart
Order
Payment

## Class diagrams

```text

CheckoutService
- cartToOrderId : Map<int, int>
- orders : Map<int, Order>
- productsBySku : Map<String, Product>
- paymentService : PaymentService

+ checkout(cart) : Order
    - check existing cartId -> return existing order
    - validate cart
    - validate SKUs
    - validate stock for ALL items
    - snapshot prices
    - reserve all stock
    - create CREATED order

+ expireOrders()
    - find CREATED orders whose reservationExpiresAt has passed
    - release stock
    - mark EXPIRED


PaymentService
- payments : Map<String, Payment>
- paymentProviderResolver : PaymentProviderResolver

+ payOrder(orderId, paymentMode) : PaymentResponse
    - ensure order is CREATED
    - ensure no existing active/paid payment
    - create Payment
    - resolve provider
    - initiate payment

+ handlePaymentCallback(paymentId, status)
    - PAID -> payment PAID, order PAID
    - FAILED -> payment FAILED, release stock, order CANCELLED


PaymentProviderResolver
+ resolveProvider(paymentMode) : PaymentProvider


interface PaymentProvider
+ originatePaymentRequest(payment) : PaymentResponse


CardPaymentProvider implements PaymentProvider

UpiPaymentProvider implements PaymentProvider


PaymentMode
- CARD
- UPI


PaymentResponse
- amount : int
- paymentId : String
- qr : String
- redirectUrl : String


Payment
- id : String
- orderId : int
- providerId : String
- paymentMode : PaymentMode
- providerName : String
- amount : int
- status : PaymentStatus


PaymentStatus
- INITIATED
- IN_PROGRESS
- PAID
- FAILED


Product
- sku : String
- price : int
- availableQuantity : int


Cart
- cartId : int
- items : List<CartItem>


CartItem
- productSKU : String
- quantity : int


Order
- orderId : int
- items : List<OrderItem>
- total : int
- reservationExpiresAt : LocalDateTime
- status : OrderStatus


OrderItem
- productSKU : String
- price : int
- quantity : int


OrderStatus
- CREATED
- PAID
- PACKED
- DELIVERED
- CANCELLED
- EXPIRED
```
