---
order: 20
search: false
---

# Design notes — Payment Gateway

Use this page after modelling the exercise. The gateway owns the payment record and state transitions;
a provider supplies method-specific initiation and outcome information.

The key guard is idempotency: retain the request key before initiating a provider payment, then return that
same payment for a retry. A callback locates that record and transitions it only from pending.

## Quick recall

- Provider initiation creates a pending payment, not a completed one.
- Idempotency belongs at initiation, before provider side effects.
- A provider callback updates an existing payment by a safe provider reference.
