---
order: 10
search: false
---

# Practice: Federation Boundaries

## Exercise: customer-dashboard - Choose the read boundary

A dashboard needs customer profile, recent orders, and payment status from independently owned stores.

1. Explain why a cross-database SQL join is not the default microservice answer.
2. Choose a BFF/API aggregator or a materialized read model; state the freshness and failure trade-off.
3. Define the partial-response policy when payment status is unavailable.

## Exercise: checkout-write - Separate reads from writes

Creating an order and charging a card span two services.

1. Explain why federation cannot make this one local transaction.
2. Compare 2PC with a Saga in terms of locks, availability, and recovery.
3. State why compensation is a new business action rather than a database rollback.
