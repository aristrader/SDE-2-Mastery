---
order: 210
---

# E-commerce Checkout LLD

Practice modelling the checkout boundary: cart items become an order only after inventory reservation and
payment outcome are handled consistently.

Start with the vague [interviewer prompt](exercise/problem_statement/), lead the
[candidate discussion](exercise/candidate_discussion/), then solve the agreed [final exercise](exercise/).
Record entities and class diagrams in the
[playground worksheet](playground/entity_identification_and_class_diagrams.md).

## Working order

1. Clarify cart scope, inventory ownership, payment outcomes, and retry behaviour.
2. Derive the order and payment states before choosing classes.
3. Decide exactly when stock is reserved, confirmed, and released.
4. Make a repeated checkout request safe before discussing distributed transactions.

## Quick recall

- An order records the business outcome; a cart is mutable shopping intent.
- Reserve stock before payment confirmation, and release it after a failed payment.
- An idempotency key makes a customer retry return the original outcome rather than create another order.
