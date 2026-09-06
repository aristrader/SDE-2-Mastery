---
order: 20
---

# Parking Lot Design

Design a small parking lot service that can allocate a compatible spot, reject a vehicle when no spot is available, release a spot on exit, and report availability by vehicle type.

Scope for this MVP:

- One parking lot has one or more floors.
- Each floor has fixed spots for specific vehicle types.
- A vehicle can park only in an available spot of the same type.
- Exit releases the spot by spot id.
- Payment, pricing, gates, reservations, and concurrency are discussed as extensions, not implemented in the runnable playground.

## Quick recall

- Start with spot allocation and release before adding payment.
- Keep spot ownership in `ParkingSpot`; keep cross-floor search in `ParkingLot`.
- Reject full capacity explicitly instead of returning `null`.
