# Payment Gateway — entity identification and class diagrams

## Goal

Model the smallest useful in-memory payment gateway that routes one payment attempt to a provider,
records an asynchronous outcome, and avoids duplicate initiation on retries.

## Requirements

- Requests contain merchant reference, positive minor-unit amount, payment method, and idempotency key.
- Support card and UPI methods, each routed to one configured provider.
- Initiation creates a pending payment and provider-specific initiation data.
- A callback changes an existing pending payment to succeeded or failed.
- Idempotent retries return the existing payment without another initiation.
- Refund a successful payment once; do not store sensitive card/bank details.

## Constraints

- Keep data in memory and use integer minor units.
- Providers are local simulated dependencies.
- The base implementation is single-threaded.

## Test scenarios

- Initiate card and UPI payments through their providers.
- Return the existing payment for an idempotent retry.
- Process successful and failed callbacks.
- Reject unknown or already-final callbacks.
- Refund a successful payment once.

## Interview follow-ups

- How would provider failover preserve idempotency?
- How would you verify and deduplicate provider webhooks?
- How would partial refunds, authorization, capture, and settlement fit in?
- How would persistence, timeouts, and concurrent state transitions change the design?

## Entity identification

## Class diagrams
