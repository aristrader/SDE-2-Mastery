---
search: false
---

# Candidate discussion — Payment Gateway

Start by proposing a bounded first scope:

> “I’ll model an in-memory gateway that accepts a payment request with an idempotency key, routes it to a
> provider based on payment method, records a pending payment attempt, and processes a later success or failure
> callback. It will support refunds only for successful payments.”

Then confirm the decisions that change the state model.

## Questions to ask

1. Which methods are required: card, UPI, wallet, bank transfer, or all of these?
2. Does the caller select the provider, or should the gateway choose from configured providers?
3. Are provider outcomes synchronous, asynchronous callbacks, or both?
4. Can the caller retry after a timeout? Is an idempotency key supplied with every initiation request?
5. What payment states should callers see: pending, succeeded, failed, cancelled, refunded?
6. Are partial refunds, multiple refunds, payment capture, and authorization/settlement required?
7. Must the gateway store card or bank details, or only provider-safe references and payment metadata?
8. Are provider retries, webhook signature validation, persistence, fraud checks, and concurrent callbacks in scope?

## Agreed scope for this exercise

- Keep one in-memory payment gateway with card and UPI payment methods.
- A request includes merchant reference, positive amount in integer minor units, payment method, and idempotency key.
- The gateway routes a method to one configured provider through a resolver.
- Initiation creates one `PENDING` payment and returns provider-specific initiation data, such as a redirect URL or
  UPI QR reference.
- A provider callback identifies the existing payment and changes it to `SUCCEEDED` or `FAILED`.
- Reusing an idempotency key returns the existing payment and never initiates another provider payment.
- A refund is allowed only once for a successful payment and transitions it to `REFUNDED`.
- Do not store sensitive card or bank details. Provider routing rules, persistence, webhook signatures, provider
  retries, partial refunds, and concurrency are follow-ups.

The final [exercise](../) records the resulting requirements and test scenarios.
