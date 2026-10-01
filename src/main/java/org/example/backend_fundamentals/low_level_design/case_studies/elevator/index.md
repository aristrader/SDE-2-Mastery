---
order: 190
---

# Elevator LLD

Practice modelling elevator state, request assignment, queued stops, and movement without turning a
small object-design exercise into a real-time dispatch system.

Start with the vague [interviewer prompt](exercise/problem_statement/), lead the
[candidate discussion](exercise/candidate_discussion/), then solve the agreed [final exercise](exercise/).
Record entities and class diagrams in the
[playground worksheet](playground/entity_identification_and_class_diagrams.md).

## Working order

1. Clarify the building size, request types, and whether multiple cars are required.
2. Separate building-wide assignment from an individual car's stop queue and movement state.
3. Define what makes a car eligible before choosing the simplest assignment rule.
4. Model one movement step correctly before discussing real-time scheduling and concurrency.

## Quick recall

- A hall request belongs to the controller; a cabin destination belongs to its assigned elevator.
- The controller assigns work; an elevator owns its current floor, direction, and pending stops.
- A simple deterministic assignment rule is enough for the base interview scope.
