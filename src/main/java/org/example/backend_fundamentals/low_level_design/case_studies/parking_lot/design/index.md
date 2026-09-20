---
order: 20
search: false
---

# Parking Lot Design

## The invariant and one working flow

At most one vehicle occupies a spot. A vehicle may use only an available spot of its exact type. In the runnable example, an entry claims a spot before a ticket is saved; an exit charges before the ticket is closed and the spot is released.

1. `ParkingService.enterVehicle` asks `ParkingLot.allocateSpot` for a `ParkingAssignment`.
2. The lot scans floors in order; a floor reserves its first available compatible `ParkingSpot`.
3. The service creates and stores a ticket with the spot, floor, entry gate, and entry time.
4. On exit, the service calculates the hourly fee, invokes the in-memory payment stub, closes the ticket, then releases the ticket's spot.

If no matching spot can be reserved, allocation throws `NoValidSpotFoundException`. The run class demonstrates a rejected second car and the released capacity after exit.

## Why each object exists

| Object | Responsibility in the flow |
| --- | --- |
| `Vehicle` / `VehicleType` | Identify the incoming vehicle and its required spot type. |
| `ParkingSpot` | Own its occupancy and reject an incompatible or already occupied reservation. |
| `ParkingFloor` | Search its own fixed spot collection. |
| `ParkingLot` | Coordinate the cross-floor first-fit search, release, and availability total. |
| `ParkingAssignment` | Return both selected spot and floor without leaking the search loop to the service. |
| `Ticket` / `TicketRepository` | Connect entry to a later quote and exit in the in-memory exercise. |
| `HourlyFeeCalculator` | Vary the price calculation independently from allocation; the resolver currently selects the one hourly policy. |
| `PaymentService` | Isolate the payment boundary; it currently logs a successful collection rather than calling a gateway. |

## Correctness boundary

`ParkingLot.allocateSpot` and `ParkingFloor.reserveSpot` are synchronized, so the practice model prevents two threads using those same in-memory objects from taking its final spot. That is not distributed coordination: the ticket repository is a `HashMap`, and separate service instances have separate locks and state.

For multiple app instances, do not add more Java locks. Persist the spot and ticket state, claim the spot atomically, and make the payment operation idempotent. See [Parking Lot Database and Concurrency](../database_concurrency/).

## Deliberate choices and extensions

- **First fit:** the current order is deterministic and simple. A nearest-spot policy is a separate allocation strategy only when distance or priority is required.
- **Exact matching:** `CAR`, `BIKE`, and `TRUCK` match only their own spot type. If a larger spot may accept a smaller vehicle, replace that rule with one explicit capacity policy.
- **In-memory payment:** the demo charges before release but has no external-failure recovery. A real gateway needs durable payment state and an idempotency key.
- **Availability scan:** counting spots is correct for the small model. Add maintained counters only when the read load justifies their transactional consistency cost.

## Production extension

For database tables, multi-instance concurrency, payment idempotency, and race-condition handling, read [Parking Lot Database and Concurrency](../database_concurrency/).

The unreferenced `assets/ParkingLot.drawio` is a preserved learner worksheet, not a diagram of this runnable model. Do not use it as design evidence: the Mermaid diagrams on the database page are the maintained visuals for the proposed durable design.

## Quick recall

- `ParkingSpot` owns occupancy; floors and lots own progressively wider searches.
- Ticket, pricing, and payment vary independently from allocation, so they stay outside `ParkingLot`.
- A synchronized object graph is sufficient only for this one-JVM exercise.
- Production allocation is an atomic shared-state claim, not a read followed by a write.
