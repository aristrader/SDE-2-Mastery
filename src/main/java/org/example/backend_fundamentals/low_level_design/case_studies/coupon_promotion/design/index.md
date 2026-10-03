---
order: 20
search: false
---

# Design notes — Coupon / Promotion Rules

Use this page after modelling the exercise. Keep the first version small: a cart calculates its eligible
subtotal, a coupon validates its own availability, and a discount type determines the discount amount.

The important boundary is that validation must happen before redemption state changes. A rejected coupon
must not consume usage.

## Quick recall

- Store money in minor units.
- Validate before mutating usage count.
- Put stacking and ordering rules in the requirements before implementing more than one promotion.
