---
order: 90
---

# Multi-Step Processes

Use this pattern when a business action crosses services or waits on people/external dependencies: payment plus inventory, KYC onboarding, order fulfillment, or repeated ride offers. A single synchronous request and database transaction is better when the whole action fits inside one service and transaction.

## Start with an explicit state machine

```text
PENDING -> VALIDATING -> PROCESSING -> COMPLETED
                         |              
                         -> FAILED -> COMPENSATING -> COMPENSATED
```

Persist the workflow state and each transition. Publish follow-up work through an outbox so a committed transition is not lost before the next step is triggered.

## Coordination choices

| Complexity | Suitable approach | What must still be true |
|---|---|---|
| Few steps in one service | State machine plus scheduled retry/reconciliation | Transitions and effects are idempotent |
| Services react independently | Choreography through events | Ownership, observability, and compensation remain clear |
| Many steps, timers, retries, human waiting | Orchestrator or durable workflow engine | External side effects remain idempotent |

Do not claim exactly-once delivery across normal queues and external systems. Delivery is usually at least once; use idempotency keys, persisted effect records, and conditional state transitions so repeated messages are harmless.

## Timeouts, retries, and compensation

- Persist deadlines rather than relying on an in-memory timer.
- Retry only failures that can safely be retried, with bounded exponential backoff and jitter.
- Compensate a completed business step when later steps fail, such as releasing inventory after payment failure. Compensation is a new idempotent action, not a database rollback across services.
- Reconcile workflows stuck beyond their expected state deadline.

## Quick recall

**Q. When do I need a workflow engine?**
A. When durable timers, retries, many dependent steps, and recovery logic overwhelm a simple persisted state machine; it is not the first default.

**Q. What makes a distributed workflow safe under retry?**
A. Idempotent side effects, durable state transitions, and reconciliation for partial failures.
