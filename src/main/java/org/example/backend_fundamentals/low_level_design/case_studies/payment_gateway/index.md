---
order: 215
---

# Payment Gateway LLD

Practice payment initiation, provider routing, callbacks, idempotent retries, and refunds without handling
real card data.

Start with the vague [interviewer prompt](exercise/problem_statement/), lead the
[candidate discussion](exercise/candidate_discussion/), then solve the agreed [final exercise](exercise/).
Record entities and class diagrams in the
[playground worksheet](playground/entity_identification_and_class_diagrams.md).

## Working order

1. Clarify payment methods, provider selection, and whether outcomes are synchronous.
2. Model payment state transitions before selecting provider abstractions.
3. Define idempotency before allowing a caller to retry initiation.
4. Keep provider-specific requests and callbacks outside the core payment record.

## Quick recall

- Initiation creates a payment attempt; it does not mean payment succeeded.
- Idempotency prevents a retry from creating a second charge.
- A callback updates an existing payment; it does not create another one.
