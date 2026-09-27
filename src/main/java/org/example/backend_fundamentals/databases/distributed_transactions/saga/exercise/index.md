---
order: 10
search: false
---

# Exercise

## Exercise: delivery-workflow - Separate Saga from pub/sub

A food-delivery workflow creates an order, reserves inventory, charges payment, and assigns a courier. Inventory and
payment publish events on a broker.

1. Explain why the broker alone does not make this a Saga.
2. Choose choreography or orchestration and justify it for a flow with retries, courier assignment, and operator
   visibility.
3. Define one idempotent compensation for a payment-success/inventory-failure path.
4. State how the workflow recovers when a compensation call times out.

## Quick recall

**Q. What is the difference between a Saga and pub/sub?**

A. Pub/sub transports messages; a Saga owns the business workflow, outcome, and compensation policy.
