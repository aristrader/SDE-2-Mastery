---
order: 10
search: false
---

# Final exercise — Elevator LLD

## Exercise: elevator-lld - Multi-car Request Dispatch

### Goal

Model the smallest useful in-memory elevator system that assigns hall requests and moves cars through
their pending stops.

This is the agreed scope after discussing the initial [interviewer prompt](problem_statement/) and
[candidate clarifications](candidate_discussion/).

### Requirements

- A building has a fixed positive floor count and two or more elevator cars.
- A hall request identifies an origin floor and direction. A cabin request identifies a destination floor
  for one assigned car.
- Each elevator tracks its identifier, current floor, current direction, door state, and pending stops.
- The controller assigns a hall request to the nearest eligible car. Prefer idle cars, then cars already
  travelling toward and able to pass the request floor; break ties by lower car identifier.
- An elevator may accept cabin destinations after it is assigned a hall request.
- A car moves one floor per simulation step toward a pending stop in its current direction.
- At a requested stop, the car serves the stop before continuing. It reverses only when no pending stop
  remains ahead in its current direction.
- Reject invalid floors, a hall request whose direction points outside the building, and a cabin request
  for the car's current floor.

### Constraints

- Keep all state in memory.
- Model movement deterministically; real-time clocks, threads, and persistence are not part of the base
  implementation.
- Use one explicit assignment rule; do not introduce a dispatch strategy hierarchy until a requirement
  needs multiple policies.

### Test scenarios

- Assign a hall request to the nearest idle elevator.
- Prefer an eligible elevator travelling toward the requested floor over one travelling away from it.
- Add a cabin stop and service it while continuing in the current direction.
- Reverse only after the car has no stop ahead in its current direction.
- Resolve equal assignment candidates using the lower car identifier.
- Reject invalid floors and invalid boundary directions.

### Interview follow-ups

- How would you support concurrent hall requests without assigning the same car inconsistently?
- How would you add capacity, overload, door-obstruction, emergency, and maintenance states?
- How would you evolve nearest-eligible assignment into a cost-based dispatch policy?
- What changes for multiple buildings, a central dispatcher, and persistent request state?
- How would you prevent starvation for a request repeatedly bypassed by moving cars?
