---
order: 220
---

# BookMyShow LLD

Practice modelling movie-show discovery, temporary seat holds, booking confirmation, and payment outcomes
without treating a concurrent reservation system as a simple cart.

Start with the vague [interviewer prompt](exercise/problem_statement/), lead the
[candidate discussion](exercise/candidate_discussion/), then solve the agreed [final exercise](exercise/).
Record entities and class diagrams in the
[playground worksheet](playground/entity_identification_and_class_diagrams.md).

## Working order

1. Clarify whether seats are held before payment and when an unused hold expires.
2. Separate immutable venue layout from one show's seat availability.
3. Define booking states and exactly when a hold becomes booked or available again.
4. Make duplicate booking requests safe before discussing multi-instance locking.

## Quick recall

- A seat is part of a screen layout; show-seat availability belongs to one specific show.
- A hold protects seats for a short time while payment is attempted.
- A booking confirms seats only after successful payment.
