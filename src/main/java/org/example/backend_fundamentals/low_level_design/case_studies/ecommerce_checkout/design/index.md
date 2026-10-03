---
order: 20
search: false
---

# Design notes — E-commerce Checkout

Use this page after modelling the exercise. The important flow is deliberate: validate the cart, reserve every
item, attempt payment, then either confirm the order or release the entire reservation.

The reservation is the consistency boundary. A payment failure must restore all stock; an idempotency retry
must return the original order before doing either operation again.

## Quick recall

- Cart items are mutable intent; order items are price-and-quantity snapshots.
- Reserve all required stock or none of it.
- Store and check the idempotency key before repeating a checkout side effect.
