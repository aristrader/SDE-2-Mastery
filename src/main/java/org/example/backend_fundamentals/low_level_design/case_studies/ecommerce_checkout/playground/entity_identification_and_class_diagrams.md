# E-commerce Checkout — entity identification and class diagrams

## Goal

Model the smallest useful in-memory checkout flow that reserves cart inventory, records payment outcome,
and keeps a retry from creating duplicate orders.

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

## Requirements

- Products have SKU, name, unit price in integer minor units, and available quantity.
- Carts contain one or more SKU/quantity line items.
- Checkout creates an immutable order-item price and quantity snapshot.
- Reserve all stock before payment; reject without changing availability when any item lacks stock.
- Confirm the order after successful payment; release stock after failed payment.
- A repeated idempotency key returns the existing order without another reservation or payment attempt.
- Reject empty carts, unknown SKU, non-positive quantity, and duplicate SKU line items.

## Entity identification
Product
Cart
Order
Payment

## Class diagrams

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