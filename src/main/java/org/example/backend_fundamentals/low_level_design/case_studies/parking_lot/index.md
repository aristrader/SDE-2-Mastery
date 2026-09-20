---
order: 20
---

# Parking Lot Design

Design a small parking lot service that assigns a compatible spot, issues a ticket, quotes an hourly fee, collects a stub payment, and releases the spot on exit.

Start with the vague [interviewer prompt](exercise/problem_statement/), lead the
[candidate discussion](exercise/candidate_discussion/), then solve the agreed [final exercise](exercise/).

The runnable in-memory model currently supports:

- One parking lot has one or more floors.
- Each floor has fixed spots for specific vehicle types.
- A vehicle can park only in an available spot of the same type.
- Entry creates a ticket with the assigned spot, floor, gate, and time.
- Exit calculates a per-vehicle-type hourly fee, logs a successful stub payment, closes the ticket, and releases the spot.
- Availability is reported by vehicle type.

The first-fit policy, exact type matching, in-memory ticket store, and JVM-local synchronization are deliberate practice constraints. Reservations, real payment integration, durability, multiple service instances, and a nearest-spot policy are extensions; the [database and concurrency page](database_concurrency/) explains the boundary where those concerns change the design.

## Quick recall

- A spot's occupancy invariant belongs in `ParkingSpot`; cross-floor search belongs in `ParkingLot`.
- A ticket connects entry to exit; payment must succeed before this demo releases the spot.
- JVM locks do not protect a horizontally scaled service; use an atomic database claim there.
