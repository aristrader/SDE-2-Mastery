---
order: 20
search: false
---

# Answers: Federation Boundaries

## Solution: customer-dashboard - Choose the read boundary

Use a BFF/API aggregator when the screen can tolerate a few service calls and needs fresh service-owned data. It calls service APIs rather than raw databases, applies timeouts, and can return the profile/orders with an explicit unavailable payment-status field. Its independently timed calls do not create one cross-service consistent snapshot. Use a materialized read model when this high-volume screen needs one fast prejoined view; it is predictably stale while asynchronous projection catches up.

## Solution: checkout-write - Separate reads from writes

Federation can combine query results but cannot turn independent service databases into one local ACID boundary. 2PC can coordinate participants but holds distributed resources and can block during coordinator failure. A Saga commits short local transactions and compensates completed business actions after a later failure; it trades atomic visibility for availability and requires idempotent retries. A refund, for example, is a new auditable action, not an invisible rollback.
