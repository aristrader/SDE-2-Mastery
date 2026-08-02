---
order: 10
---

# Saga vs Publish-Subscribe

## Core Difference

Saga and Publish-Subscribe solve different problems but are often confused because Saga frequently uses Pub/Sub underneath.

| Feature | Saga | Publish-Subscribe |
|--------|--------|--------|
| **Pattern Type** | Distributed transaction pattern | Messaging pattern |
| **Purpose** | Coordinates a business workflow | Delivers messages between services |
| **Focus** | Consistency | Communication |
| **Failure Handling** | Has success/failure handling (compensation) | Just sends events |
| **Business Logic** | Knows the business process | Doesn't know the business process |

## How it works

Consider a food delivery app with four steps: Create Order, Reserve Inventory, Charge Payment, Assign Delivery Partner.

**If using Pub/Sub only:** The `Order Service` publishes `OrderPlaced`, Inventory receives it and publishes `InventoryReserved`, Payment receives it and fails.
*Result:* Inventory remains reserved. The system becomes inconsistent. Pub/Sub does not know what should happen next, how to rollback, or which services are involved.

**If using Saga:** The Saga defines the workflow and explicit **compensation actions** for failures (e.g., `Reserve Inventory` -> `Release Inventory`, `Charge Payment` -> `Refund Payment`).
*Result:* If payment fails, the Saga executes the `Release Inventory` compensation. The system returns to a consistent state.

Compensation is a **business undo**, not a database rollback. If card charging succeeds and inventory reservation fails, the compensation is a refund. That refund may be visible to the customer. Some actions, like sending an email or calling an external webhook, may not be perfectly undoable.

## Comparisons within Java/Architecture

There are two ways to coordinate a Saga:

### Choreography Saga
Services react to events independently without a central coordinator.
- **Flow:** `OrderCreated` -> `Inventory Service` -> `InventoryReserved` -> `Payment Service` -> `PaymentCharged`.
- **Misconception:** This looks exactly like Pub/Sub. The difference is that Pub/Sub is only the transport mechanism; the collection of events and specific compensating actions forms the Saga semantics.

### Orchestration Saga
A central coordinator controls the entire transaction.
- **Flow:** `Saga Orchestrator` sends commands to `Inventory`, `Payment`, and `Delivery` services.
- If payment fails, the Orchestrator explicitly issues a `Release Inventory` command.
- Pub/Sub may not even be involved; this can be implemented using REST, gRPC, or message queues.

The orchestrator is different from a 2PC coordinator. It stores workflow progress and retries/resumes after a crash, but it does not keep remote database rows locked in a prepared state.

Use orchestration when the flow has many steps, branching, retries, visibility needs, or tricky compensation. Use choreography for small linear flows where event tracing stays simple.

## Saga and outbox

Saga steps often update local state and publish an event. That creates the dual-write problem: DB commit succeeds but event publish fails, or event publish succeeds but DB commit fails.

The usual fix is transactional outbox: write the business change and an outbox event row in the same local DB transaction, then publish the event asynchronously through a worker or CDC.

## Gotchas / Pitfalls / Trick questions

- **Confusion:** Saga and Pub/Sub appear similar because both involve events.
  - **Reality:** Pub/Sub answers "How do services communicate?" while Saga answers "How do multiple services safely complete one business transaction?"
- **Confusion:** Choreography Saga looks identical to Pub/Sub.
  - **Reality:** The event transport is Pub/Sub. The business workflow plus the compensation logic (which must be carefully handled) is the Saga.
- **Compensation Failures:** If a compensation step (e.g., `Release Inventory`) fails in a Saga, it must be retried with idempotency. Pub/Sub alone offers no framework for this.
- **External side effects:** Some compensations are imperfect. Refunds, emails, and third-party webhooks need explicit retry and audit handling.

## Quick recall

**Q: What is the core difference between Saga and Pub/Sub?**
A: Pub/Sub is a communication pattern; Saga is a distributed transaction pattern designed to maintain business consistency.

**Q: Does Pub/Sub provide rollback capabilities?**
A: No, Pub/Sub only delivers messages. Rollbacks require explicitly defined compensating actions in a Saga.

**Q: What is a choreography Saga?**
A: An event-driven Saga where each service reacts to events independently, typically using Pub/Sub as the transport.

**Q: What is an orchestration Saga?**
A: A Saga controlled by a central orchestrator that actively commands services and handles failures, potentially using REST or gRPC instead of events.

**Q: Can a Saga be implemented without Pub/Sub?**
A: Yes, orchestration Sagas often use synchronous protocols like REST or gRPC to command downstream services.

**Q: Why is a Saga orchestrator not the same as a 2PC coordinator?**
A: It resumes from stored workflow state and retries business steps. It does not hold remote DB locks waiting for a global commit.

**Q: Why does Saga often need outbox?**
A: Each local transaction usually must also publish the next event. Outbox makes the DB change and publish intent commit atomically.

