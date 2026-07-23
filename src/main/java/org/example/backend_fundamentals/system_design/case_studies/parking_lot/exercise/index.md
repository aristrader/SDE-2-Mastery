---
order: 10
search: false
---

# Parking Lot Design Exercise

## Exercise: parking-lot-mvp - Spot Allocation And Release

### Goal

Build the smallest useful Parking Lot LLD model.

### Scope

Implement:

- `Vehicle` with registration number and vehicle type.
- `ParkingSpot` with compatible vehicle type, current vehicle, reserve, release, and availability checks.
- `ParkingFloor` that can reserve the first matching available spot and count availability.
- `ParkingLot` that searches floors, rejects full capacity, releases a spot, and reports availability.
- `ParkingLotRun` that demonstrates the main flow.

### Constraints

- Keep all runnable Java under `playground/`.
- Do not add database, Spring, REST APIs, or threads.
- Throw a clear exception when no compatible spot exists.
- Keep payment and pricing out of the runnable MVP unless you also add a complete exit/payment flow.

### Acceptance criteria

- Parking one car prints the allocated spot id.
- A second car is rejected when the only car spot is already occupied.
- Releasing the first car makes that car spot available again.
- `mvn -q exec:java -Dexec.mainClass="org.example.backend_fundamentals.system_design.case_studies.parking_lot.playground.ParkingLotRun"` runs successfully.
