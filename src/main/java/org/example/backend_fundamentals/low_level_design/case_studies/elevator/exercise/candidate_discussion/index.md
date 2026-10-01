---
search: false
---

# Candidate discussion — Elevator

Start by proposing a bounded first scope:

> “I’ll model one in-memory building with a fixed set of elevator cars. A controller will accept hall
> and cabin requests, assign a hall request to an eligible car using a simple deterministic rule, and
> advance one car by one floor at a time.”

Then confirm the decisions that change the model or dispatch behavior.

## Questions to ask

1. Is this one building with a fixed number of floors and elevator cars?
2. Do we need both hall requests (floor plus direction) and cabin destination requests?
3. Should the controller select a car, or is there only one car?
4. What is the expected assignment rule: nearest idle car, nearest eligible car already moving in the
   requested direction, or a production dispatch algorithm?
5. Should a car serve stops in its current direction before reversing?
6. Do we need to model door timing, passenger capacity, weight limits, maintenance mode, or failures?
7. Must requests arrive concurrently, persist across process restarts, or work across buildings?

## Agreed scope for this exercise

- Model one in-memory building with a fixed positive floor count and two or more elevator cars.
- A hall request contains an origin floor and desired direction. A cabin request contains only a
  destination floor and belongs to an already assigned car.
- Each car owns its identifier, current floor, travel direction, door state, and pending stops.
- The controller receives hall requests and assigns each to the nearest eligible car. Prefer an idle car;
  otherwise choose a car that will pass the origin while travelling in the requested direction. Resolve
  ties by lower car identifier.
- A car keeps servicing pending stops in its current direction. It reverses only when no pending stop
  remains ahead in that direction.
- Movement is simulated one floor at a time. On reaching a requested stop, the car opens its doors,
  serves the stop, closes its doors, and updates direction if needed.
- Treat capacity, door timers, emergency handling, maintenance, persistence, concurrent requests, and
  advanced dispatch optimisation as follow-ups.

The final [exercise](../) records the resulting requirements and test scenarios.
