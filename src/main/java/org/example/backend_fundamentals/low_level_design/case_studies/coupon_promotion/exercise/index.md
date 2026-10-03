---
order: 10
search: false
---

# Final exercise — Coupon / Promotion Rules

## Exercise: coupon-promotion-lld - Single Coupon Checkout

### Goal

Model the smallest useful in-memory coupon system that validates one coupon against a cart and calculates
its discount safely.

This is the agreed scope after discussing the initial [interviewer prompt](problem_statement/) and
[candidate clarifications](candidate_discussion/).

### Requirements

- A cart has line items with product ID, unit price in integer minor units, and positive quantity.
- A coupon has a unique code, active time window, global usage limit, optional minimum subtotal, and usage count.
- Support a fixed-amount coupon and a percentage coupon.
- A coupon may be restricted to a set of product IDs; without that restriction, use the complete cart subtotal.
- A checkout may apply at most one coupon.
- Reject unknown, inactive, exhausted, and ineligible coupons without changing the cart total or usage count.
- Apply the discount only to the eligible subtotal and cap it so the final total cannot become negative.
- A successful checkout increments the coupon's usage once. Reusing an exhausted coupon must fail.

### Constraints

- Keep all data in memory.
- Represent money in integer minor units; do not use floating point for prices or totals.
- Use one clear discount calculation model. Do not create a generic rules engine for the base implementation.

### Test scenarios

- Apply a valid fixed coupon to a qualifying cart.
- Apply a valid percentage coupon to a product-restricted eligible subtotal.
- Reject an expired, exhausted, or below-minimum-subtotal coupon.
- Cap a fixed discount at the eligible subtotal.
- Confirm coupon usage increments only after a successful checkout.
- Confirm an unknown code does not mutate cart or coupon state.

### Interview follow-ups

- How would you support multiple stackable promotions with an explicit ordering rule?
- How would you enforce a per-customer usage limit and concurrent redemption safely?
- How would you add shipping, tax, and buy-one-get-one promotions without coupling them to every coupon class?
- How would you persist redemption state and make payment retries idempotent?
- When would a general rules engine be justified over typed promotion classes?
