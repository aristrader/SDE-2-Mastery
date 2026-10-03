---
order: 200
---

# Coupon / Promotion Rules LLD

Practice modelling cart discounts without mixing eligibility, discount calculation, and redemption state.

Start with the vague [interviewer prompt](exercise/problem_statement/), lead the
[candidate discussion](exercise/candidate_discussion/), then solve the agreed [final exercise](exercise/).
Record entities and class diagrams in the
[playground worksheet](playground/entity_identification_and_class_diagrams.md).

## Working order

1. Clarify whether one order can apply one or many promotions and how conflicts are resolved.
2. Separate eligibility checks from the calculation that changes the cart total.
3. Keep coupon redemption state distinct from a reusable promotion definition.
4. Make the base ordering rule deterministic before discussing a configurable rules engine.

## Quick recall

- A promotion defines a discount rule; a coupon is a redeemable code that may have usage limits.
- Eligibility answers whether a rule applies; calculation answers how much it discounts.
- Explicit stacking and ordering rules prevent totals from depending on implementation order.
