---
order: 10
search: false
---

# Final exercise — Parking Lot Design

## Exercise: parking-lot-mvp - Spot Allocation And Release

### Goal

Build the smallest useful Parking Lot LLD model.

This is the agreed scope after discussing the initial [interviewer prompt](problem_statement/) and
[candidate clarifications](candidate_discussion/).

### Final agreed requirements

Implement:

- `Vehicle` with registration number and vehicle type.
- `ParkingSpot` with compatible vehicle type, current vehicle, reserve, release, and availability checks.
- `ParkingFloor` that can reserve the first matching available spot and count availability.
- `ParkingLot` that searches floors, rejects full capacity, releases a spot, and reports availability.
- `Ticket` and an in-memory ticket repository that preserve the assigned spot, floor, entry gate, and entry time.
- An hourly fee calculator selected behind `FeeCalculator`.
- A payment service that logs a successful stub collection before the service closes the ticket and releases the spot.
- `ParkingLotRun` that demonstrates entry, full capacity, exit quote, payment, and released capacity.

### Constraints

- Keep all runnable Java under `playground/`.
- Keep persistence in memory; do not add a real database or payment gateway.
- JVM-local synchronization is enough for allocation in this exercise; do not claim it solves multi-instance allocation.
- Throw a clear exception when no compatible spot exists.
- Preserve the exit order: quote, collect payment, close ticket, then release the spot.

### Acceptance criteria

- Parking one car prints the allocated spot id.
- A second car is rejected when the only car spot is already occupied.
- The exit quote rounds a partial hour up to one hour at that vehicle type's configured rate.
- Completing exit logs a successful stub payment and makes the first car's spot available again.
- `mvn -q exec:java -Dexec.mainClass="org.example.backend_fundamentals.low_level_design.case_studies.parking_lot.playground.ParkingLotRun"` runs successfully.
