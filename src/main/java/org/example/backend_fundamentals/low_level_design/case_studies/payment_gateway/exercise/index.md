---
order: 10
search: false
---

# Final exercise — Payment Gateway

## Exercise: payment-gateway-lld - Idempotent Provider-Routed Payment

### Goal

Model the smallest useful in-memory payment gateway that routes one payment attempt to a provider,
records an asynchronous outcome, and avoids duplicate initiation on retries.

This is the agreed scope after discussing the initial [interviewer prompt](problem_statement/) and
[candidate clarifications](candidate_discussion/).

### Requirements

- A payment request has merchant reference, positive amount in integer minor units, payment method, and
  idempotency key.
- Support `CARD` and `UPI` methods. Each method resolves to one configured provider.
- Initiating a payment creates a `PENDING` payment record and returns provider initiation data.
- The provider callback identifies an existing pending payment and changes it to `SUCCEEDED` or `FAILED`.
- A callback for an unknown or already-final payment must not create or alter another payment.
- Reusing an idempotency key returns the existing payment without another provider initiation.
- A refund is allowed once for a successful payment and transitions it to `REFUNDED`.
- Never store card or bank account details; only safe provider references and required payment metadata.

### Constraints

- Keep all data in memory.
- Use integer minor units for money.
- Providers are local simulated dependencies; real HTTP calls and webhook signature verification are follow-ups.
- The base implementation is single-threaded.

### Test scenarios

- Initiate a card and a UPI payment through their configured providers.
- Return the original payment for a repeated idempotency key.
- Change a pending payment to succeeded or failed using a callback.
- Reject a callback for an unknown or final payment.
- Refund a successful payment once and reject a second refund.
- Reject a non-positive amount or unsupported payment method without initiating a payment.

### Interview follow-ups

- How would provider selection fail over safely while retaining the idempotency guarantee?
- How would you authenticate provider callbacks and safely process duplicate webhooks?
- How would partial refund, authorization, capture, and settlement states change the model?
- How would you persist payment state and recover after a provider timeout?
- How would concurrent initiation and callback requests protect one payment's state transition?
