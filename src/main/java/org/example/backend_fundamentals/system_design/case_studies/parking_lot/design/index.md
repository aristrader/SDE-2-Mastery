---
order: 20
search: false
---

# Parking Lot Design

## Requirements

- Allocate a spot for a vehicle if a compatible spot is free.
- Reject allocation when all compatible spots are occupied.
- Release a spot when the vehicle exits.
- Report available spot count by vehicle type.
- Keep payment, pricing, and gate orchestration outside the MVP implementation.

## Core entities

- `Vehicle`: registration number plus `VehicleType`.
- `VehicleType`: supported categories such as `CAR`, `BIKE`, and `TRUCK`.
- `ParkingSpot`: owns compatibility and current occupancy.
- `ParkingFloor`: owns a group of spots and local spot search.
- `ParkingLot`: owns cross-floor allocation, release, and availability.
- `Ticket`: useful extension point for entry/exit history.
- `Payment`: useful extension point once pricing is in scope.

## Main flows

### Park

1. `ParkingLot.allocateSpot(vehicle)` scans floors in order.
2. Each floor asks for the first available spot matching `vehicle.getVehicleType()`.
3. `ParkingSpot.reserve(vehicle)` marks the spot occupied.
4. If no floor can reserve a spot, the lot throws a clear full-capacity exception.

### Exit

1. `ParkingLot.releaseSpot(spotId)` asks each floor to release that spot.
2. `ParkingSpot.release()` clears the current vehicle.
3. If the spot id does not exist, the lot throws a clear unknown-spot exception.

### Availability

1. Each floor counts available spots for the requested vehicle type.
2. The lot sums those counts across floors.

## Tradeoffs

- The playground uses first-fit allocation because it is simple and interview-friendly.
- A production design would likely add entry gates, exit gates, pricing, payment status, ticket lifecycle, and concurrency control.
- Spot type matching is exact in the MVP. If truck spots can accept cars, replace `canFit` with a capacity/rank rule.
- In-memory repositories are enough for the exercise. Persistence is a separate system-design concern.

## Production extension

For database tables, multi-instance concurrency, payment idempotency, and race-condition handling, read [Parking Lot Database and Concurrency](../database_concurrency/).

## Quick recall

- Put local invariants on the object that owns the state: `ParkingSpot.reserve` and `release`.
- Put search at the aggregate level: `ParkingFloor` searches spots; `ParkingLot` searches floors.
- Make full-capacity and unknown-spot cases explicit.
- Add payment only after ticket lifecycle and release flow are clear.
