# Coupon / Promotion Rules — entity identification and class diagrams

## Goal

Model the smallest useful in-memory coupon system that validates one coupon against a cart and calculates
its discount safely.

## Constraints

- Keep all data in memory.
- Use integer minor units for money.
- Do not build a generic rules engine for the base implementation.

## Test scenarios

- Apply a valid fixed coupon to a qualifying cart.
- Apply a percentage coupon to a product-restricted eligible subtotal.
- Reject expired, exhausted, unknown, and below-minimum-subtotal coupons.
- Cap a fixed discount at the eligible subtotal.
- Increment usage only after successful checkout.

## Interview follow-ups

- How would you support multiple stackable promotions with explicit ordering?
- How would you enforce per-customer limits and concurrent redemption?
- How would you add shipping, tax, and buy-one-get-one promotions?
- How would persistence and idempotent payment retries change redemption?

## Requirements

- A cart has line items with product ID, unit price in integer minor units, and positive quantity.
- A coupon has a unique code, active time window, global usage limit, optional minimum subtotal, and usage count.
- Support fixed-amount and percentage coupons.
- A coupon may target specific product IDs; without a target list, use the complete cart subtotal.
- A checkout applies at most one coupon and increments usage exactly once after a successful calculation.
- Reject unknown, inactive, exhausted, and ineligible coupons without mutating cart total or coupon usage.
- Cap the discount so the final total cannot become negative.

## Entity identification
Cart
Coupon
Item

## Class diagrams

CouponService
- coupons : Map<string (couponCode), Coupon>
- resolveDiscount(discountType) : Discount
+ applyCoupon(Cart cart, String couponCode)

Coupon
- code : string
- minimumSubtotal ? : int
- redemptionCount : int
- redemptionLimit : int
- startTime : LocalDateTime
- endTime : LocalDateTime
- productIds ? : Set<int>
- discountType : DiscountType
- discountValue : int

Cart
- items : List<Item>

Item
- productId : int
- price : int
- quantity : int

DiscountType
- Flat
- Percentage

interface Discount
- calculateDiscount(int eligibleSubTotal, int discountValue) : int , also coupon service does all validation if that coupon was applicable or not if minimum cart value was there or not , etc, since that is the responsibility of the coupon service, discount should just apply the discount

FlatDiscount implements Discount
PercentageDiscount implements Discount