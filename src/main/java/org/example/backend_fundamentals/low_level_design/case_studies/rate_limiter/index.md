---
order: 180
---

# Rate Limiter LLD

Practice protecting a shared API from excessive requests while keeping client-specific state isolated.

Start with the vague [interviewer prompt](exercise/problem_statement/), lead the
[candidate discussion](exercise/candidate_discussion/), then solve the agreed [final exercise](exercise/).
Record your entities and class diagram in the
[playground worksheet](playground/entity_identification_and_class_diagrams.md). Keep implementation questions
and their corrections in [traps and learnings](playground/traps_and_learnings.md). For the step-by-step
reasoning behind the concurrent follow-up, read [concurrency intuition](playground/concurrency_intuition.md).

## Working order

1. Clarify the identity being limited, the limit behavior, and whether requests arrive concurrently.
2. State what a successful or rejected request means before choosing an algorithm.
3. Walk through one client exhausting its allowance, then recovering capacity over time.
4. Derive the per-client state and the boundary that protects a concurrent update.

## Quick recall

- A rate limit needs a subject to limit, a policy, and a permit decision.
- Limit state must be isolated per subject; one busy client must not consume another client's allowance.
- The algorithm determines the boundary trade-off between bursts, smoothness, and memory.
