---
order: 20
search: false
---

# Solution

## Solution: delivery-workflow - Separate Saga from pub/sub

The broker is transport only. It becomes a Saga when the workflow defines the completed local steps, the next action
for each outcome, and compensations. Choose orchestration here: the workflow branches, has retries, and needs one
durable place to inspect `order-123` rather than reconstructing a long event trail.

After payment succeeds but inventory fails, the orchestrator records `RefundPayment(order-123)` and invokes the
provider with the same idempotency key on every retry. A timeout leaves that compensation `PENDING`, not “failed” or
“complete”; a worker retries, then moves repeated failure to an alert/DLQ path for reconciliation. A refund is an
auditable business action, not an invisible database rollback.
