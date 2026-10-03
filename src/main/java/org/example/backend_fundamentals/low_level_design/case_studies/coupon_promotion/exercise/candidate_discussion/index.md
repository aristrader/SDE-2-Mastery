---
search: false
---

# Candidate discussion — Coupon / Promotion Rules

Start by proposing a bounded first scope:

> “I’ll model an in-memory checkout cart that accepts at most one coupon. The coupon can provide a fixed
> or percentage discount when its eligibility rules pass, and it has a simple total-usage limit.”

Then confirm the decisions that affect the model and total-calculation order.

## Questions to ask

1. Can an order apply one coupon or multiple promotions? If multiple, may they stack?
2. Which base discount kinds are required: fixed amount, percentage, free shipping, or buy-one-get-one?
3. Does eligibility depend only on cart subtotal, or also on product/category, customer, and dates?
4. Are coupon codes unique and reusable? Is there a global usage limit or a per-customer limit?
5. What should happen when a code is invalid, expired, already exhausted, or ineligible for the cart?
6. Should a percentage discount apply before or after fixed discounts, taxes, and shipping?
7. Must money avoid floating-point arithmetic?
8. Are persistence, payment, concurrent redemptions, an admin UI, and a full rules language part of this round?

## Agreed scope for this exercise

- One in-memory cart contains line items with product ID, unit price in integer minor units, and quantity.
- A checkout may apply at most one coupon code.
- A coupon is active only within its start/end time and while its global usage limit remains available.
- Support fixed-amount and percentage discounts with an optional minimum cart subtotal.
- A coupon may optionally target specific product IDs; when no target list exists, it applies to the whole cart.
- Reject an unknown, inactive, exhausted, or ineligible coupon without changing the cart total.
- Apply a valid discount to the eligible subtotal only; never produce a negative order total.
- Redeem the coupon only after successful checkout calculation; increment usage once for that checkout.
- Persistence, payment, taxes, shipping, coupon stacking, concurrent redemption, and a general rule engine are
  follow-ups.

The final [exercise](../) records the resulting requirements and test scenarios.
